package com.financialbeacon.recomendaciones;

import com.financialbeacon.dto.InversionDTOs.OpcionInversionDTO;
import com.financialbeacon.dto.InversionDTOs.PlanInversionDTO;
import com.financialbeacon.dto.InversionDTOs.ProyeccionDTO;
import com.financialbeacon.recomendaciones.MotorRecomendaciones.DeudaInfo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Motor de reglas del módulo de inversión. Igual que MotorRecomendaciones, es
 * Java puro (sin Spring) para poder probarlo y explicarlo de forma aislada.
 *
 * No recomienda productos, empresas ni instituciones concretas: orienta sobre
 * QUÉ TIPO de instrumento conviene según la situación del usuario, siguiendo
 * un orden de prioridades:
 *   1) tener excedente libre -> 2) pagar deudas caras -> 3) fondo de emergencia -> 4) invertir.
 *
 * Es información educativa, no asesoría financiera.
 */
public final class MotorInversion {

    public enum Perfil { CONSERVADOR, MODERADO, AGRESIVO }

    // ---- Umbrales (heurísticas simples; ajustables) ----
    static final double TASA_DEUDA_PRIORITARIA = 10.0; // % anual: sobre esto conviene pagar la deuda antes de invertir
    public static final int MESES_FONDO_EMERGENCIA = 6; // ingresos variables -> 6 meses de gastos esenciales
    static final int HORIZONTE_CORTO = 12;              // meses: menos de esto, solo instrumentos de bajo riesgo
    static final int HORIZONTE_MEDIO = 36;              // meses: menos de esto, sin renta variable
    static final int HORIZONTE_APV = 60;                // meses: desde aquí se menciona el APV
    static final double MARGEN_BAJO = 2.0;              // puntos porcentuales bajo el escenario base
    static final double MARGEN_ALTO = 2.0;              // puntos porcentuales sobre el escenario base
    static final int[] ANIOS_PROYECCION = {1, 3, 5, 10};

    // Rentabilidad nominal anual SUPUESTA por clase (%). Es ilustrativa, no una promesa ni un dato de mercado:
    // conviene actualizarla cuando cambien las tasas.
    static final double RENT_LIQUIDEZ = 3.5;
    static final double RENT_RENTA_FIJA = 4.5;
    static final double RENT_BALANCEADO = 6.0;
    static final double RENT_RENTA_VARIABLE = 8.0;

    static final String AVISO =
            "Información educativa; no es asesoría financiera ni una recomendación personalizada. "
                    + "Las rentabilidades no están garantizadas, pueden ser negativas y las tasas cambian. "
                    + "Antes de invertir revisa riesgos, comisiones y condiciones con tu institución o con un asesor autorizado.";

    public record Entrada(
            double ingreso,
            double gastosEsenciales,   // fijos + variables (sin gastos hormiga)
            double ahorroMensual,      // ingreso - todos los gastos del mes
            List<DeudaInfo> deudas,
            double fondoEmergenciaActual,
            Perfil perfil,
            int horizonteMeses,
            int mesesFondo) {}

    private MotorInversion() {}

    public static PlanInversionDTO generar(Entrada e) {
        double cuotas = e.deudas().stream().mapToDouble(DeudaInfo::cuotaMinima).sum();
        double excedente = e.ahorroMensual() - cuotas; // lo que queda libre tras gastos y cuotas mínimas
        double meta = e.gastosEsenciales() * e.mesesFondo();

        if (e.ingreso() <= 0 || e.gastosEsenciales() <= 0) {
            return plan(e, "SIN_DATOS", "Primero registra tus ingresos y gastos",
                    "Para decirte cuánto podrías destinar a inversión necesitamos tu ingreso del mes y tus gastos fijos y variables. "
                            + "Regístralos en el Resumen y vuelve aquí.",
                    0, excedente, meta, List.of(), List.of(), 0);
        }

        if (excedente <= 0) {
            String causa = e.ahorroMensual() <= 0
                    ? "Este mes tus gastos igualan o superan tu ingreso."
                    : "Después de tus gastos te quedan " + clp(e.ahorroMensual())
                            + ", pero tus cuotas mínimas de deuda suman " + clp(cuotas) + ".";
            return plan(e, "SIN_EXCEDENTE", "Aún no hay excedente para invertir",
                    causa + " Invertir sin margen obliga a rescatar en mal momento. "
                            + "Primero equilibra tu presupuesto (mira las recomendaciones de ahorro).",
                    0, excedente, meta, List.of(), List.of(), 0);
        }

        // Deuda más cara que lo que razonablemente rinde una inversión: pagarla es un "rendimiento" seguro.
        DeudaInfo cara = e.deudas().stream()
                .filter(d -> d.montoTotal() > 0 && d.tasaAnual() >= TASA_DEUDA_PRIORITARIA)
                .max(Comparator.comparingDouble(DeudaInfo::tasaAnual))
                .orElse(null);
        if (cara != null) {
            String colchon = e.fondoEmergenciaActual() < e.gastosEsenciales()
                    ? " Mientras tanto, procura mantener al menos 1 mes de gastos esenciales (" + clp(e.gastosEsenciales()) + ") como colchón."
                    : "";
            return plan(e, "PAGAR_DEUDA", "Antes de invertir, paga «" + cara.nombre() + "»",
                    "«" + cara.nombre() + "» cuesta " + num(cara.tasaAnual()) + "% anual. Pagarla equivale a una rentabilidad segura de ese nivel, "
                            + "difícil de superar con inversiones de riesgo razonable. Destina tu excedente de " + clp(excedente)
                            + " al mes a ella; cuando la termines, el siguiente paso es el fondo de emergencia." + colchon,
                    0, excedente, meta, List.of(), List.of(), 0);
        }

        if (e.fondoEmergenciaActual() < meta) {
            double falta = meta - e.fondoEmergenciaActual();
            int meses = (int) Math.ceil(falta / excedente);
            List<OpcionInversionDTO> opciones = List.of(opcion("LIQUIDEZ", 100, excedente));
            return plan(e, "FONDO_EMERGENCIA", "Primero arma tu fondo de emergencia",
                    "Se recomienda tener " + e.mesesFondo() + " meses de gastos esenciales ahorrados (con ingresos variables conviene más margen): "
                            + "tu meta es " + clp(meta) + " y llevas " + clp(e.fondoEmergenciaActual()) + ". "
                            + "Con " + clp(excedente) + " al mes lo completarías en aproximadamente " + meses(meses)
                            + ". Este dinero debe estar disponible rápido y sin riesgo de pérdida, por eso va en instrumentos líquidos.",
                    excedente, excedente, meta, opciones, List.of(), RENT_LIQUIDEZ);
        }

        return planInvertir(e, excedente, meta);
    }

    // ------------------------------------------------------------------
    // Etapa final: reparto del excedente por perfil y horizonte
    // ------------------------------------------------------------------
    private static PlanInversionDTO planInvertir(Entrada e, double excedente, double meta) {
        int[] reparto = repartoPorPerfil(e.perfil(), e.horizonteMeses()); // {liquidez, renta fija, balanceado, renta variable}
        String[] claves = {"LIQUIDEZ", "RENTA_FIJA", "BALANCEADO", "RENTA_VARIABLE"};
        double[] rent = {RENT_LIQUIDEZ, RENT_RENTA_FIJA, RENT_BALANCEADO, RENT_RENTA_VARIABLE};

        List<OpcionInversionDTO> opciones = new ArrayList<>();
        double rentabilidad = 0;
        for (int i = 0; i < claves.length; i++) {
            if (reparto[i] > 0) {
                opciones.add(opcion(claves[i], reparto[i], excedente));
                rentabilidad += reparto[i] / 100.0 * rent[i];
            }
        }
        if (e.horizonteMeses() >= HORIZONTE_APV) {
            opciones.add(opcion("APV", 0, 0)); // informativo: es un "vehículo", no suma al reparto
        }

        List<ProyeccionDTO> proyecciones = new ArrayList<>();
        for (int anios : ANIOS_PROYECCION) {
            int n = anios * 12;
            proyecciones.add(new ProyeccionDTO(
                    anios,
                    Math.round(excedente * n),
                    Math.round(valorFuturo(excedente, rentabilidad - MARGEN_BAJO, n)),
                    Math.round(valorFuturo(excedente, rentabilidad, n)),
                    Math.round(valorFuturo(excedente, rentabilidad + MARGEN_ALTO, n))));
        }

        String msg = "Ya cubres tu fondo de emergencia y tienes un excedente libre de " + clp(excedente) + " al mes. "
                + "Para un perfil " + e.perfil().name().toLowerCase() + " y un horizonte de " + horizonte(e.horizonteMeses())
                + ", una distribución de referencia sería la siguiente. "
                + (e.horizonteMeses() < HORIZONTE_MEDIO
                ? "Como el plazo es corto, se evita la renta variable: puede perder valor justo cuando necesites el dinero."
                : "A mayor plazo, el riesgo de cada instrumento pesa menos, pero nunca desaparece.");

        return plan(e, "INVERTIR", "Ya puedes empezar a invertir", msg,
                excedente, excedente, meta, opciones, proyecciones, rentabilidad);
    }

    /** Porcentajes {liquidez, renta fija, balanceado, renta variable}; siempre suman 100. */
    static int[] repartoPorPerfil(Perfil perfil, int horizonteMeses) {
        int[] r = switch (perfil) {
            case CONSERVADOR -> new int[]{50, 40, 10, 0};
            case MODERADO -> new int[]{20, 35, 25, 20};
            case AGRESIVO -> new int[]{10, 20, 25, 45};
        };
        if (horizonteMeses < HORIZONTE_CORTO) {
            return new int[]{70, 30, 0, 0};
        }
        if (horizonteMeses < HORIZONTE_MEDIO) {
            r[1] += r[3];              // la renta variable pasa a renta fija
            r[3] = 0;
            int mitad = r[2] / 2;      // y la mitad de lo balanceado también
            r[1] += mitad;
            r[2] -= mitad;
        }
        return r;
    }

    // ------------------------------------------------------------------
    // Cálculo: valor futuro de aportes mensuales iguales (interés compuesto)
    // ------------------------------------------------------------------
    public static double valorFuturo(double aporteMensual, double tasaAnualPct, int meses) {
        if (aporteMensual <= 0 || meses <= 0) return 0;
        double r = Math.pow(1 + tasaAnualPct / 100.0, 1.0 / 12) - 1;
        if (Math.abs(r) < 1e-12) return aporteMensual * meses;
        return aporteMensual * (Math.pow(1 + r, meses) - 1) / r;
    }

    // ------------------------------------------------------------------
    // Descripción de cada clase de instrumento (genérica: sin marcas ni productos)
    // ------------------------------------------------------------------
    private static OpcionInversionDTO opcion(String clave, int pct, double excedente) {
        double monto = Math.round(excedente * pct / 100.0);
        return switch (clave) {
            case "LIQUIDEZ" -> new OpcionInversionDTO(clave, "Liquidez y renta fija de muy corto plazo", "BAJO",
                    "Alta: se puede rescatar en pocos días", pct, monto,
                    "Fondo de emergencia y metas a menos de 1 año.",
                    "Rentabilidad baja; a veces rinde poco más que la inflación. Revisa comisiones y que permita rescates rápidos.",
                    "Cuenta de ahorro o depósito a plazo en un banco; fondos mutuos de deuda de corto plazo en una administradora de fondos.");
            case "RENTA_FIJA" -> new OpcionInversionDTO(clave, "Renta fija (depósitos a plazo, bonos, fondos de deuda)", "BAJO",
                    "Media: según el plazo elegido", pct, monto,
                    "Metas de 1 a 5 años con poca tolerancia a perder dinero.",
                    "Rinde más que la liquidez a cambio de plazo. Los instrumentos en UF protegen del alza de precios; los fondos de deuda pueden oscilar en el corto plazo.",
                    "Depósitos a plazo en bancos; bonos mediante una corredora de bolsa; fondos mutuos de deuda.");
            case "BALANCEADO" -> new OpcionInversionDTO(clave, "Fondos balanceados (mezcla de renta fija y acciones)", "MEDIO",
                    "Media: rescate en algunos días", pct, monto,
                    "Metas de 3 años o más, con diversificación automática en un solo producto.",
                    "Pueden caer en el corto plazo. Compara comisiones y costos totales entre fondos similares.",
                    "Administradoras de fondos mutuos, directamente o por tu banco o corredora.");
            case "RENTA_VARIABLE" -> new OpcionInversionDTO(clave, "Renta variable diversificada (fondos accionarios o ETF)", "ALTO",
                    "Media: se vende en bolsa o se rescata en días", pct, monto,
                    "Metas de 5 años o más con dinero que no vas a necesitar pronto.",
                    "Puede perder valor varios años seguidos. Prefiere fondos o índices amplios en vez de acciones sueltas y no concentres todo en una sola empresa o país.",
                    "Fondos mutuos accionarios o ETF mediante una corredora de bolsa o plataforma de inversión autorizada.");
            case "APV" -> new OpcionInversionDTO(clave, "APV (Ahorro Previsional Voluntario)", "VARIABLE",
                    "Baja: pensado para la jubilación", 0, 0,
                    "Ahorro de largo plazo con beneficio tributario según el régimen (A o B).",
                    "Retirar antes de jubilar puede implicar perder beneficios o pagar impuestos. Revisa las condiciones vigentes y qué régimen te conviene. El riesgo depende del fondo que elijas.",
                    "AFP, bancos, administradoras de fondos mutuos, aseguradoras y corredoras autorizadas.");
            default -> throw new IllegalArgumentException("Clave desconocida: " + clave);
        };
    }

    private static PlanInversionDTO plan(Entrada e, String etapa, String titulo, String mensaje,
                                         double ahorroMensualAInvertir, double excedente, double meta,
                                         List<OpcionInversionDTO> opciones, List<ProyeccionDTO> proyecciones,
                                         double rentabilidad) {
        return new PlanInversionDTO(etapa, titulo, mensaje,
                Math.round(e.ahorroMensual()), Math.round(Math.max(excedente, 0)), Math.round(meta),
                Math.round(e.fondoEmergenciaActual()), e.perfil().name(), e.horizonteMeses(),
                opciones, proyecciones, Math.round(rentabilidad * 10) / 10.0, AVISO);
    }

    // ------------------------------------------------------------------
    // Utilidades
    // ------------------------------------------------------------------
    private static String clp(double v) {
        return MotorRecomendaciones.clp(v);
    }

    private static String num(double v) {
        return v == Math.rint(v) ? String.valueOf((long) v) : String.format(java.util.Locale.forLanguageTag("es-CL"), "%.1f", v);
    }

    private static String meses(int n) {
        return n == 1 ? "1 mes" : n + " meses";
    }

    private static String horizonte(int mesesTotales) {
        if (mesesTotales < 12) return meses(mesesTotales);
        int anios = mesesTotales / 12;
        return anios == 1 ? "1 año" : anios + " años";
    }
}
