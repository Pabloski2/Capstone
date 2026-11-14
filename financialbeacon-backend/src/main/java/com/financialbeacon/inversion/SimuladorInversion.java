package com.financialbeacon.inversion;

/**
 * Cálculos para proyectar una inversión. Es Java puro para poder probarlo de forma aislada.
 * Son proyecciones simples con interés compuesto mensual: no consideran comisiones ni impuestos.
 */
public final class SimuladorInversion {

    private SimuladorInversion() {}

    /**
     * Valor al final del plazo, con un monto inicial y un aporte al final de cada mes.
     * La rentabilidad anual se convierte a su equivalente mensual compuesta.
     */
    public static double valorFuturo(double monto, double aporteMensual, int meses, double rentabilidadAnualPct) {
        if (meses <= 0) {
            return monto;
        }
        double anual = Math.max(rentabilidadAnualPct, -99.0) / 100.0;
        double mensual = Math.pow(1 + anual, 1.0 / 12.0) - 1;
        if (Math.abs(mensual) < 1e-12) {
            return monto + aporteMensual * meses;
        }
        double factor = Math.pow(1 + mensual, meses);
        return monto * factor + aporteMensual * ((factor - 1) / mensual);
    }

    /** Lo que se aporta en total: el monto inicial más los aportes mensuales. */
    public static double capitalAportado(double monto, double aporteMensual, int meses) {
        return monto + aporteMensual * Math.max(meses, 0);
    }

    /** Rentabilidad real aproximada: lo que rinde por sobre la inflación, en porcentaje. */
    public static double rentabilidadReal(double rentabilidadNominalPct, double inflacionPct) {
        return ((1 + rentabilidadNominalPct / 100.0) / (1 + inflacionPct / 100.0) - 1) * 100.0;
    }
}
