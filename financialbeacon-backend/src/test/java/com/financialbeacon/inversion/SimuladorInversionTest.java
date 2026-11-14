package com.financialbeacon.inversion;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimuladorInversionTest {

    @Test
    void unAnioAl12PorCiento_rinde12Porciento() {
        assertEquals(1_120_000.0, SimuladorInversion.valorFuturo(1_000_000, 0, 12, 12), 0.01);
    }

    @Test
    void sinRentabilidad_soloSumaLosAportes() {
        assertEquals(700_000.0, SimuladorInversion.valorFuturo(100_000, 50_000, 12, 0), 0.001);
    }

    @Test
    void conAportesMensuales_coincideConLaSimulacionMesAMes() {
        double rentabilidadAnual = 12;
        double mensual = Math.pow(1 + rentabilidadAnual / 100, 1.0 / 12) - 1;
        double v = 0;
        for (int i = 0; i < 24; i++) {
            v = v * (1 + mensual) + 100_000;
        }
        assertEquals(v, SimuladorInversion.valorFuturo(0, 100_000, 24, rentabilidadAnual), 0.001);
    }

    @Test
    void conRentabilidadNegativa_elValorBaja() {
        assertEquals(900_000.0, SimuladorInversion.valorFuturo(1_000_000, 0, 12, -10), 0.01);
    }

    @Test
    void sinPlazoDevuelveElMontoInicial() {
        assertEquals(500_000.0, SimuladorInversion.valorFuturo(500_000, 10_000, 0, 8), 0.0001);
    }

    @Test
    void unaRentabilidadImposibleNoGeneraNaN() {
        double v = SimuladorInversion.valorFuturo(1_000_000, 0, 12, -150);
        assertTrue(!Double.isNaN(v) && v >= 0);
    }

    @Test
    void calculaLaRentabilidadRealRespectoDeLaInflacion() {
        assertEquals(0.8646, SimuladorInversion.rentabilidadReal(5.0, 4.1), 0.001);
        assertEquals(0.0, SimuladorInversion.rentabilidadReal(4.1, 4.1), 1e-9);
        assertTrue(SimuladorInversion.rentabilidadReal(3.0, 4.1) < 0);
    }

    @Test
    void calculaElCapitalAportado() {
        assertEquals(1_300_000.0, SimuladorInversion.capitalAportado(100_000, 100_000, 12), 0.0001);
    }
}
