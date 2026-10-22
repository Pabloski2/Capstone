package com.financialbeacon.dto;

import java.util.List;

/**
 * Respuesta del módulo de inversión: una etapa (qué toca hacer primero),
 * las clases de instrumento sugeridas y una proyección ilustrativa.
 */
public final class InversionDTOs {

    private InversionDTOs() {}

    /**
     * Una clase de instrumento (no un producto ni una empresa concreta).
     * riesgo: BAJO, MEDIO, ALTO o VARIABLE (depende del fondo elegido).
     */
    public record OpcionInversionDTO(
            String clave,
            String nombre,
            String riesgo,
            String liquidez,
            int porcentaje,
            double montoMensual,
            String paraQue,
            String considera,
            String dondeSeContrata) {}

    /** Valor estimado tras N años aportando el monto mensual (escenarios bajo, base y alto). */
    public record ProyeccionDTO(int anios, double aportado, double bajo, double base, double alto) {}

    /**
     * etapa: SIN_DATOS, SIN_EXCEDENTE, PAGAR_DEUDA, FONDO_EMERGENCIA o INVERTIR.
     * opciones y proyecciones solo vienen con datos en las etapas FONDO_EMERGENCIA e INVERTIR.
     */
    public record PlanInversionDTO(
            String etapa,
            String titulo,
            String mensaje,
            double ahorroMensual,
            double excedenteMensual,
            double metaFondoEmergencia,
            double fondoEmergenciaActual,
            String perfil,
            int horizonteMeses,
            List<OpcionInversionDTO> opciones,
            List<ProyeccionDTO> proyecciones,
            double rentabilidadSupuestaAnual,
            String aviso) {}
}
