package com.financialbeacon.recomendaciones;

import com.financialbeacon.dto.InversionDTOs.PlanInversionDTO;
import com.financialbeacon.recomendaciones.MotorInversion.Entrada;
import com.financialbeacon.recomendaciones.MotorInversion.Perfil;
import com.financialbeacon.recomendaciones.MotorRecomendaciones.DeudaInfo;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MotorInversionTest {

    private static Entrada entrada(double ingreso, double esenciales, double ahorro,
                                   List<DeudaInfo> deudas, double fondo, Perfil perfil, int horizonte) {
        return new Entrada(ingreso, esenciales, ahorro, deudas, fondo, perfil, horizonte,
                MotorInversion.MESES_FONDO_EMERGENCIA);
    }

    @Test
    void sinIngreso_pideRegistrarDatos() {
        PlanInversionDTO p = MotorInversion.generar(entrada(0, 0, 0, List.of(), 0, Perfil.MODERADO, 60));
        assertEquals("SIN_DATOS", p.etapa());
    }

    @Test
    void gastaMasDeLoQueGana_noHayExcedente() {
        PlanInversionDTO p = MotorInversion.generar(entrada(500_000, 450_000, -50_000, List.of(), 0, Perfil.MODERADO, 60));
        assertEquals("SIN_EXCEDENTE", p.etapa());
    }

    @Test
    void cuotasMinimasCopanElAhorro_noHayExcedente() {
        List<DeudaInfo> deudas = List.of(new DeudaInfo("Tarjeta", 300_000, 40, 80_000));
        PlanInversionDTO p = MotorInversion.generar(entrada(800_000, 500_000, 60_000, deudas, 0, Perfil.MODERADO, 60));
        assertEquals("SIN_EXCEDENTE", p.etapa());
    }

    @Test
    void deudaCara_sePagaAntesDeInvertir() {
        List<DeudaInfo> deudas = List.of(new DeudaInfo("Tarjeta", 300_000, 36, 20_000));
        PlanInversionDTO p = MotorInversion.generar(entrada(1_000_000, 500_000, 300_000, deudas, 5_000_000, Perfil.AGRESIVO, 120));
        assertEquals("PAGAR_DEUDA", p.etapa());
        assertTrue(p.opciones().isEmpty());
    }

    @Test
    void deudaBarata_noImpideInvertir() {
        List<DeudaInfo> deudas = List.of(new DeudaInfo("Crédito", 2_000_000, 6, 50_000));
        PlanInversionDTO p = MotorInversion.generar(entrada(1_000_000, 500_000, 300_000, deudas, 3_000_000, Perfil.MODERADO, 60));
        assertEquals("INVERTIR", p.etapa());
    }

    @Test
    void sinFondoDeEmergencia_priorizaLiquidez() {
        PlanInversionDTO p = MotorInversion.generar(entrada(1_000_000, 500_000, 200_000, List.of(), 0, Perfil.AGRESIVO, 120));
        assertEquals("FONDO_EMERGENCIA", p.etapa());
        assertEquals(3_000_000.0, p.metaFondoEmergencia(), 0.001);
        assertEquals(1, p.opciones().size());
        assertEquals("LIQUIDEZ", p.opciones().get(0).clave());
    }

    @Test
    void repartoSiempreSuma100() {
        for (Perfil perfil : Perfil.values()) {
            for (int h : new int[]{6, 12, 24, 36, 60, 120}) {
                int suma = 0;
                for (int v : MotorInversion.repartoPorPerfil(perfil, h)) suma += v;
                assertEquals(100, suma, perfil + " / " + h + " meses");
            }
        }
    }

    @Test
    void horizonteCorto_noIncluyeRentaVariable() {
        PlanInversionDTO p = MotorInversion.generar(entrada(1_000_000, 500_000, 300_000, List.of(), 3_000_000, Perfil.AGRESIVO, 6));
        assertEquals("INVERTIR", p.etapa());
        assertTrue(p.opciones().stream().noneMatch(o -> o.clave().equals("RENTA_VARIABLE")));
    }

    @Test
    void perfilAgresivoLargoPlazo_incluyeRentaVariableYApv() {
        PlanInversionDTO p = MotorInversion.generar(entrada(1_000_000, 500_000, 300_000, List.of(), 3_000_000, Perfil.AGRESIVO, 120));
        assertTrue(p.opciones().stream().anyMatch(o -> o.clave().equals("RENTA_VARIABLE") && o.porcentaje() == 45));
        assertTrue(p.opciones().stream().anyMatch(o -> o.clave().equals("APV")));
        assertEquals(4, p.proyecciones().size());
    }

    @Test
    void valorFuturo_sinInteres_esLaSumaDeAportes() {
        assertEquals(120_000, MotorInversion.valorFuturo(10_000, 0, 12), 0.001);
    }

    @Test
    void valorFuturo_conInteres_superaLosAportes() {
        assertTrue(MotorInversion.valorFuturo(100_000, 6, 120) > 100_000 * 120);
    }
}
