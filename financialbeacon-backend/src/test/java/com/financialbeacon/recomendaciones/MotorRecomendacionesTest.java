package com.financialbeacon.recomendaciones;

import com.financialbeacon.dto.RecomendacionDTO;
import com.financialbeacon.recomendaciones.MotorRecomendaciones.DeudaInfo;
import com.financialbeacon.recomendaciones.MotorRecomendaciones.Entrada;
import com.financialbeacon.recomendaciones.MotorRecomendaciones.GastoInfo;
import com.financialbeacon.recomendaciones.MotorRecomendaciones.PagoSugerido;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MotorRecomendacionesTest {

    private static Entrada entrada(double ingreso, double fijos, double variables, double hormiga,
                                   Double meta, List<GastoInfo> gastos,
                                   List<DeudaInfo> deudas, List<PagoSugerido> orden) {
        return new Entrada(ingreso, fijos, variables, hormiga, meta, gastos, deudas, "AVALANCHA", orden);
    }

    private static boolean hay(List<RecomendacionDTO> lista, String nivel, String fragmentoTitulo) {
        return lista.stream().anyMatch(r -> r.getNivel().equals(nivel) && r.getTitulo().contains(fragmentoTitulo));
    }

    @Test
    void mesesParaPagar_sinInteres_divideExacto() {
        assertEquals(12, MotorRecomendaciones.mesesParaPagar(1200, 0, 100));
    }

    @Test
    void mesesParaPagar_pagoQueNoCubreIntereses_devuelveMenosUno() {
        // 24% anual sobre 1.000.000 = 20.000 de interés al mes; pagar 10.000 nunca baja la deuda
        assertEquals(-1, MotorRecomendaciones.mesesParaPagar(1_000_000, 24, 10_000));
    }

    @Test
    void sinIngreso_pideRegistrarloYNadaMas() {
        List<RecomendacionDTO> r = MotorRecomendaciones.generar(
                entrada(0, 0, 0, 0, null, List.of(), List.of(), List.of()));
        assertEquals(1, r.size());
        assertEquals("INFO", r.get(0).getNivel());
    }

    @Test
    void gastarMasDeLoQueGana_generaAlertaYVaPrimero() {
        List<GastoInfo> gastos = List.of(
                new GastoInfo("Arriendo", 300_000, "FIJO"),
                new GastoInfo("Salidas", 250_000, "VARIABLE"),
                new GastoInfo("Café", 60_000, "HORMIGA"));
        List<RecomendacionDTO> r = MotorRecomendaciones.generar(
                entrada(500_000, 300_000, 250_000, 60_000, null, gastos, List.of(), List.of()));

        assertEquals("ALERTA", r.get(0).getNivel());
        assertTrue(r.get(0).getTitulo().contains("más de lo que ganas"));
    }

    @Test
    void metaNoAlcanzada_generaPlanConRecortes() {
        // Ahorro = 800.000 - 600.000 = 200.000; meta 350.000 -> faltan 150.000
        List<GastoInfo> gastos = List.of(
                new GastoInfo("Arriendo", 300_000, "FIJO"),
                new GastoInfo("Supermercado", 200_000, "VARIABLE"),
                new GastoInfo("Delivery", 70_000, "HORMIGA"),
                new GastoInfo("Café", 30_000, "HORMIGA"));
        List<RecomendacionDTO> r = MotorRecomendaciones.generar(
                entrada(800_000, 300_000, 200_000, 100_000, 350_000.0, gastos, List.of(), List.of()));

        RecomendacionDTO plan = r.stream().filter(x -> x.getTitulo().contains("meta de ahorro")).findFirst().orElseThrow();
        assertEquals("AVISO", plan.getNivel());
        assertTrue(plan.getMensaje().contains("gastos hormiga"));
        assertTrue(plan.getMensaje().contains("Delivery"));
        assertTrue(plan.getAhorroPotencial() > 0);
    }

    @Test
    void metaCumplida_generaMensajePositivo() {
        List<GastoInfo> gastos = List.of(new GastoInfo("Arriendo", 300_000, "FIJO"));
        List<RecomendacionDTO> r = MotorRecomendaciones.generar(
                entrada(800_000, 300_000, 0, 0, 200_000.0, gastos, List.of(), List.of()));
        assertTrue(hay(r, "POSITIVO", "sobre tu meta"));
    }

    @Test
    void detectaStreamingPorDescripcion() {
        List<GastoInfo> gastos = List.of(
                new GastoInfo("Netflix", 9_000, "VARIABLE"),
                new GastoInfo("Plan Entel", 20_000, "FIJO"));
        List<RecomendacionDTO> r = MotorRecomendaciones.generar(
                entrada(800_000, 20_000, 9_000, 0, null, gastos, List.of(), List.of()));
        assertTrue(hay(r, "INFO", "streaming"));
        assertTrue(hay(r, "INFO", "plan móvil"));
    }

    @Test
    void deudas_priorizaLaPrimeraYAvisaTasaAlta() {
        List<GastoInfo> gastos = List.of(new GastoInfo("Arriendo", 300_000, "FIJO"));
        List<DeudaInfo> deudas = List.of(
                new DeudaInfo("Tarjeta Falabella", 500_000, 45, 50_000),
                new DeudaInfo("Crédito de consumo", 1_200_000, 22, 80_000));
        // Ahorro 500.000 - cuotas 130.000 = 370.000 de excedente para la prioritaria
        List<PagoSugerido> orden = List.of(
                new PagoSugerido("Tarjeta Falabella", 500_000, 45, 420_000),
                new PagoSugerido("Crédito de consumo", 1_200_000, 22, 80_000));

        List<RecomendacionDTO> r = MotorRecomendaciones.generar(
                entrada(800_000, 300_000, 0, 0, null, gastos, deudas, orden));

        assertTrue(hay(r, "INFO", "Paga primero «Tarjeta Falabella»"));
        assertTrue(hay(r, "AVISO", "tasa alta"));
    }

    @Test
    void deudas_ahorroNoAlcanzaParaMinimas_generaAlerta() {
        List<GastoInfo> gastos = List.of(new GastoInfo("Arriendo", 450_000, "FIJO"));
        List<DeudaInfo> deudas = List.of(
                new DeudaInfo("Tarjeta", 500_000, 45, 50_000),
                new DeudaInfo("Crédito", 1_200_000, 22, 80_000));
        List<RecomendacionDTO> r = MotorRecomendaciones.generar(
                entrada(500_000, 450_000, 0, 0, null, gastos, deudas, List.of()));

        assertTrue(hay(r, "ALERTA", "no alcanza para las cuotas mínimas"));
    }

    @Test
    void deudas_recortarHormigaAceleraElPago() {
        List<GastoInfo> gastos = List.of(
                new GastoInfo("Arriendo", 300_000, "FIJO"),
                new GastoInfo("Delivery", 200_000, "HORMIGA"));
        List<DeudaInfo> deudas = List.of(new DeudaInfo("Tarjeta", 3_000_000, 30, 60_000));
        // Ahorro = 800.000 - 500.000 = 300.000 -> pago prioritario = 300.000 (~12 meses).
        // Recortando 30% de hormiga (+60.000) se paga en ~10 meses.
        List<PagoSugerido> orden = List.of(new PagoSugerido("Tarjeta", 3_000_000, 30, 300_000));

        List<RecomendacionDTO> r = MotorRecomendaciones.generar(
                entrada(800_000, 300_000, 0, 200_000, null, gastos, deudas, orden));

        assertTrue(hay(r, "INFO", "acelera tu deuda"));
    }
}
