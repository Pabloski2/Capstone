package com.financialbeacon.recomendaciones;

import com.financialbeacon.dto.RecomendacionDTO;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Motor de reglas que convierte los datos financieros del mes en mensajes
 * concretos: en qué recortar para ahorrar y qué hacer con cada deuda.
 *
 * Es Java puro (sin Spring) a propósito: se puede probar de forma aislada y
 * cada regla se puede explicar y ajustar cambiando los umbrales de abajo.
 */
public final class MotorRecomendaciones {

    // ---- Umbrales (heurísticas simples; ajustables) ----
    static final double UMBRAL_HORMIGA = 0.05;      // gastos hormiga sobre el ingreso
    static final double UMBRAL_VARIABLES = 0.30;    // gastos variables sobre el ingreso
    static final double UMBRAL_FIJOS = 0.50;        // referencia: regla 50/30/20
    static final double AHORRO_SALUDABLE = 0.20;
    static final double AHORRO_BAJO = 0.10;
    static final double CARGA_DEUDA_ALTA = 0.40;    // cuotas mínimas sobre el ingreso
    static final double TASA_ALTA = 30.0;           // % anual
    static final double RECORTE_HORMIGA = 0.30;     // recorte realista sugerido
    static final double RECORTE_VARIABLES = 0.15;
    static final int MAX_MESES = 600;               // más de 50 años = en la práctica no se paga

    private static final Pattern STREAMING = Pattern.compile(
            "\\b(netflix|spotify|disney|hbo|prime video|amazon prime|youtube|paramount|crunchyroll|streaming|suscripcion|apple tv|deezer)\\b");
    private static final Pattern MOVIL = Pattern.compile(
            "\\b(plan movil|plan celular|plan de celular|celular|entel|movistar|wom|claro|virgin mobile)\\b");

    // ---- Datos de entrada (registros simples, independientes de la base de datos) ----
    public record GastoInfo(String descripcion, double monto, String categoria) {}

    public record DeudaInfo(String nombre, double montoTotal, double tasaAnual, double cuotaMinima) {}

    /** Primera deuda del orden de pago = la prioritaria; "pago" incluye la cuota mínima más el excedente. */
    public record PagoSugerido(String nombre, double montoTotal, double tasaAnual, double pago) {}

    public record Entrada(
            double ingreso, double fijos, double variables, double hormiga,
            Double metaAhorro,
            List<GastoInfo> gastos,
            List<DeudaInfo> deudas,
            String estrategia,
            List<PagoSugerido> ordenDePago) {}

    private MotorRecomendaciones() {}

    public static List<RecomendacionDTO> generar(Entrada e) {
        List<RecomendacionDTO> out = new ArrayList<>();

        if (e.ingreso() <= 0) {
            out.add(rec("AHORRO", "INFO", "Registra tu ingreso de este mes",
                    "Para calcular cuánto puedes ahorrar y darte recomendaciones, primero ingresa tu sueldo del mes en el Dashboard.",
                    null));
            return out;
        }

        double ahorro = e.ingreso() - (e.fijos() + e.variables() + e.hormiga());

        if (e.gastos().isEmpty()) {
            out.add(rec("AHORRO", "INFO", "Registra tus gastos del mes",
                    "Aún no hay gastos registrados este mes. Agrega tus gastos fijos, variables y hormiga para ver en qué puedes ahorrar.",
                    null));
        } else {
            reglasDeAhorro(e, ahorro, out);
        }
        reglasDeDeuda(e, ahorro, out);

        if (out.isEmpty()) {
            out.add(rec("AHORRO", "POSITIVO", "Todo en orden",
                    "Por ahora no detectamos alertas. Sigue registrando tus gastos para mantener el control.", null));
        }

        // Orden estable: primero lo urgente; dentro de cada nivel se conserva el orden de las reglas.
        out.sort(Comparator.comparingInt(r -> ordenNivel(r.getNivel())));
        return out;
    }

    // ------------------------------------------------------------------
    // Reglas de ahorro
    // ------------------------------------------------------------------
    private static void reglasDeAhorro(Entrada e, double ahorro, List<RecomendacionDTO> out) {
        double ingreso = e.ingreso();
        List<GastoInfo> hormigas = e.gastos().stream()
                .filter(g -> "HORMIGA".equals(g.categoria()))
                .sorted(Comparator.comparingDouble(GastoInfo::monto).reversed())
                .toList();

        boolean hayPlanDeMeta = false;

        // 1) Gasta más de lo que gana
        if (ahorro < 0) {
            double deficit = -ahorro;
            double controlable = e.hormiga() + e.variables();
            StringBuilder sb = new StringBuilder("Este mes tus gastos superan tu ingreso en " + clp(deficit) + ". ");
            if (controlable > 0) {
                List<String> partes = new ArrayList<>();
                if (e.hormiga() > 0) partes.add("gastos hormiga (" + clp(e.hormiga()) + ")");
                if (e.variables() > 0) partes.add("gastos variables (" + clp(e.variables()) + ")");
                sb.append("Lo más rápido para equilibrarlo es recortar lo que puedes controlar de inmediato: ")
                        .append(String.join(" y ", partes)).append(". ");
                if (controlable < deficit) {
                    sb.append("Aun recortándolos por completo no alcanza, así que revisa también tus gastos fijos: arriendo, planes y suscripciones.");
                }
            } else {
                sb.append("Todos tus gastos son fijos, así que revisa arriendo, planes y suscripciones que puedas renegociar o cambiar.");
            }
            out.add(rec("AHORRO", "ALERTA", "Estás gastando más de lo que ganas",
                    sb.toString().trim(), potencial(Math.min(deficit, controlable))));
        }

        // 2) Meta de ahorro
        if (e.metaAhorro() != null && e.metaAhorro() > 0) {
            double meta = e.metaAhorro();
            if (ahorro >= meta) {
                out.add(rec("AHORRO", "POSITIVO", "Vas sobre tu meta de ahorro",
                        "Tu meta es ahorrar " + clp(meta) + " este mes y vas en " + clp(ahorro)
                                + ". Si mantienes este ritmo, en un año juntarías cerca de " + clp(ahorro * 12) + ".",
                        null));
            } else {
                hayPlanDeMeta = true;
                double brecha = meta - ahorro;
                double restante = brecha;
                double total = 0;
                List<String> pasos = new ArrayList<>();

                double rH = Math.min(restante, e.hormiga() * RECORTE_HORMIGA);
                if (rH > 0.5) {
                    String top = detalleTop(hormigas, 2);
                    pasos.add("• Recorta " + clp(rH) + " de tus gastos hormiga"
                            + (top.isEmpty() ? "" : " (los más altos: " + top + ")") + ".");
                    restante -= rH;
                    total += rH;
                }
                double rV = Math.min(restante, e.variables() * RECORTE_VARIABLES);
                if (rV > 0.5) {
                    pasos.add("• Ajusta " + clp(rV) + " de tus gastos variables.");
                    restante -= rV;
                    total += rV;
                }
                // Solo suscripciones marcadas como FIJO: las que estén como variable u hormiga ya cuentan en los recortes de arriba.
                double rS = Math.min(restante, sumaSuscripciones(e.gastos(), "FIJO"));
                if (rS > 0.5) {
                    pasos.add("• Pausa o comparte suscripciones de streaming por hasta " + clp(rS) + ".");
                    restante -= rS;
                    total += rS;
                }
                if (restante > 0.5) {
                    pasos.add("• Aun así te faltarían " + clp(restante)
                            + ": considera aumentar tus ingresos o revisar tus gastos fijos.");
                }

                String estado = ahorro >= 0
                        ? "hoy vas en " + clp(ahorro)
                        : "hoy tus gastos superan tu ingreso en " + clp(-ahorro);
                String cabecera = "Tu meta es ahorrar " + clp(meta) + " y " + estado + ". Te faltan "
                        + clp(brecha) + " este mes. Un plan posible:\n";
                out.add(rec("AHORRO", "AVISO", "Te faltan " + clp(brecha) + " para tu meta de ahorro",
                        cabecera + String.join("\n", pasos), potencial(total)));
            }
        }

        // 3) Gastos hormiga
        double shareH = e.hormiga() / ingreso;
        if (e.hormiga() > 0 && shareH >= UMBRAL_HORMIGA) {
            if (!hayPlanDeMeta) {
                double liberable = e.hormiga() * RECORTE_HORMIGA;
                String top = detalleTop(hormigas, 3);
                String msg = "Suman " + clp(e.hormiga()) + ", un " + pct(shareH) + " de tu ingreso."
                        + (top.isEmpty() ? "" : " Los más altos: " + top + ".")
                        + " Si los reduces en un 30%, liberarías cerca de " + clp(liberable)
                        + " al mes (" + clp(liberable * 12) + " al año).";
                out.add(rec("AHORRO", "AVISO", "Tus gastos hormiga son un " + pct(shareH) + " de tu ingreso",
                        msg, potencial(liberable)));
            }
        } else if (e.hormiga() > 0 && ahorro >= 0) {
            out.add(rec("AHORRO", "POSITIVO", "Tus gastos hormiga están bajo control",
                    "Representan un " + pct(shareH) + " de tu ingreso, un nivel manejable. Sigue anotándolos para que no se acumulen.",
                    null));
        }

        // 4) Gastos variables
        double shareV = e.variables() / ingreso;
        if (shareV > UMBRAL_VARIABLES) {
            double liberable = e.variables() * RECORTE_VARIABLES;
            out.add(rec("AHORRO", "AVISO", "Tus gastos variables son un " + pct(shareV) + " de tu ingreso",
                    "Son los que más fácil se ajustan mes a mes. Revisa salidas, compras no planificadas y consumos que puedas acotar, y ponte un tope mensual. Recortar un 15% liberaría cerca de "
                            + clp(liberable) + " al mes.",
                    potencial(liberable)));
        }

        // 5) Gastos fijos
        double shareF = e.fijos() / ingreso;
        if (shareF > UMBRAL_FIJOS) {
            out.add(rec("AHORRO", "AVISO", "Tus gastos fijos son un " + pct(shareF) + " de tu ingreso",
                    "Dejan poco margen para ahorrar o pagar deudas. Como referencia, la regla 50/30/20 sugiere que lo esencial no supere el 50%. Revisa arriendo, planes y contratos que puedas renegociar o cambiar.",
                    null));
        }

        // 6) Suscripciones de streaming y plan móvil (se detectan por la descripción del gasto)
        List<GastoInfo> subs = e.gastos().stream()
                .filter(g -> STREAMING.matcher(normalizar(g.descripcion())).find())
                .toList();
        if (!subs.isEmpty()) {
            double total = subs.stream().mapToDouble(GastoInfo::monto).sum();
            GastoInfo masCara = subs.stream().max(Comparator.comparingDouble(GastoInfo::monto)).get();
            out.add(rec("AHORRO", "INFO", "Gastas " + clp(total) + " en streaming y suscripciones",
                    "La más cara es «" + masCara.descripcion() + "» (" + clp(masCara.monto())
                            + "). Revisa cuáles usas de verdad: pausar una o compartir un plan familiar podría ahorrarte hasta "
                            + clp(masCara.monto()) + " al mes.",
                    potencial(masCara.monto())));
        }
        boolean hayMovil = e.gastos().stream().anyMatch(g -> MOVIL.matcher(normalizar(g.descripcion())).find());
        if (hayMovil) {
            out.add(rec("AHORRO", "INFO", "Revisa tu plan móvil",
                    "Si pagas más de lo que usas, cotiza otros planes. En Chile puedes cambiarte de compañía conservando tu número (portabilidad).",
                    null));
        }

        // 7) Margen de ahorro general
        double tasa = ahorro / ingreso;
        if (ahorro >= 0 && tasa >= AHORRO_SALUDABLE) {
            out.add(rec("AHORRO", "POSITIVO", "Estás ahorrando un " + pct(tasa) + " de tu ingreso",
                    "Vas sobre el 20% que suele recomendarse. "
                            + (e.deudas().isEmpty()
                            ? "Una buena idea es armar un fondo de emergencia."
                            : "Una buena idea es destinar parte a tus deudas."),
                    null));
        } else if (ahorro >= 0 && tasa < AHORRO_BAJO) {
            out.add(rec("AHORRO", "INFO", "Tu margen de ahorro es de un " + pct(tasa),
                    "Una referencia común es ahorrar entre 10% y 20% del ingreso. Empieza por los gastos hormiga y variables, que son los más fáciles de ajustar.",
                    null));
        }
    }

    // ------------------------------------------------------------------
    // Reglas de deuda
    // ------------------------------------------------------------------
    private static void reglasDeDeuda(Entrada e, double ahorro, List<RecomendacionDTO> out) {
        if (e.deudas().isEmpty()) return;

        double ingreso = e.ingreso();
        double cuotas = e.deudas().stream().mapToDouble(DeudaInfo::cuotaMinima).sum();
        double disponible = Math.max(ahorro, 0);
        double carga = cuotas / ingreso;

        // 1) Carga de deuda alta
        if (carga > CARGA_DEUDA_ALTA) {
            out.add(rec("DEUDA", "ALERTA", "Tus cuotas mínimas son un " + pct(carga) + " de tu ingreso",
                    "Sumadas, tus cuotas mínimas son " + clp(cuotas)
                            + " al mes. Sobre el 40% del ingreso suele considerarse una carga alta. Evita tomar nuevas deudas y evalúa renegociar o consolidar las de mayor tasa.",
                    null));
        }

        if (disponible < cuotas) {
            // 2) No alcanza ni para las cuotas mínimas
            double falta = cuotas - disponible;
            out.add(rec("DEUDA", "ALERTA", "Tu ahorro no alcanza para las cuotas mínimas",
                    "Después de tus gastos te quedarían " + clp(disponible) + ", pero tus cuotas mínimas suman " + clp(cuotas)
                            + ". Te faltan " + clp(falta)
                            + " para pagarlas sin atrasarte. Recorta esa cantidad en gastos hormiga o variables, o habla con tu acreedor para renegociar antes de caer en mora.",
                    potencial(falta)));
        } else if (!e.ordenDePago().isEmpty()) {
            // 3) Deuda prioritaria, con plazo estimado
            double excedente = disponible - cuotas;
            PagoSugerido p = e.ordenDePago().get(0);
            int meses = mesesParaPagar(p.montoTotal(), p.tasaAnual(), p.pago());
            double interesMensual = p.montoTotal() * p.tasaAnual() / 100.0 / 12.0;

            StringBuilder sb = new StringBuilder();
            sb.append("Con la estrategia ").append(nombreEstrategia(e.estrategia()))
                    .append(", conviene pagar primero «").append(p.nombre()).append("» (tasa ")
                    .append(num(p.tasaAnual())).append("% anual). Este mes puedes destinarle ").append(clp(p.pago()));
            if (excedente > 0.5) {
                sb.append(" (su cuota mínima más ").append(clp(excedente)).append(" extra)");
            }
            sb.append(". ");
            if (interesMensual > 0.5) {
                sb.append("Solo en intereses te cuesta cerca de ").append(clp(interesMensual)).append(" al mes. ");
            }
            if (meses > 0) {
                sb.append("A este ritmo la terminarías de pagar en aproximadamente ").append(meses(meses))
                        .append(" (estimación, sin nuevos cargos).");
            } else {
                sb.append("Con ese pago no alcanzas a cubrir los intereses, así que la deuda no bajaría.");
            }
            out.add(rec("DEUDA", meses > 0 ? "INFO" : "ALERTA", "Paga primero «" + p.nombre() + "»", sb.toString(), null));

            // 4) Cuánto se acelera si recorta gastos hormiga
            double extra = e.hormiga() * RECORTE_HORMIGA;
            if (extra > 0.5) {
                int conExtra = mesesParaPagar(p.montoTotal(), p.tasaAnual(), p.pago() + extra);
                if (conExtra > 0 && (meses < 0 || conExtra < meses)) {
                    String msg;
                    if (meses < 0) {
                        msg = "Hoy tu pago a «" + p.nombre() + "» no cubre ni los intereses. Si reduces un 30% tus gastos hormiga ("
                                + clp(extra) + " al mes) y lo destinas a esa deuda, la terminarías en aproximadamente "
                                + meses(conExtra) + ".";
                    } else {
                        msg = "Si reduces un 30% tus gastos hormiga (" + clp(extra) + " al mes) y lo destinas a «" + p.nombre()
                                + "», la terminarías " + meses(meses - conExtra) + " antes: en aproximadamente "
                                + meses(conExtra) + " en vez de " + meses(meses) + ".";
                    }
                    out.add(rec("DEUDA", "INFO", "Recortar gastos hormiga acelera tu deuda", msg, potencial(extra)));
                }
            }
        }

        // 5) Deuda con tasa alta: conviene refinanciar
        e.deudas().stream()
                .filter(d -> d.tasaAnual() >= TASA_ALTA)
                .max(Comparator.comparingDouble(DeudaInfo::tasaAnual))
                .ifPresent(d -> out.add(rec("DEUDA", "AVISO",
                        "«" + d.nombre() + "» tiene una tasa alta (" + num(d.tasaAnual()) + "% anual)",
                        "Consulta con tu banco o acreedor si puedes refinanciarla o consolidarla en un crédito con menor tasa, y evita seguir usándola mientras la pagas. Mientras más tiempo la mantengas, más intereses pagas.",
                        null)));
    }

    // ------------------------------------------------------------------
    // Cálculo de plazo: meses para pagar una deuda con cuota fija e interés mensual
    // ------------------------------------------------------------------
    /** Devuelve los meses aproximados para saldar la deuda, o -1 si el pago no alcanza a cubrir los intereses. */
    public static int mesesParaPagar(double principal, double tasaAnualPct, double pagoMensual) {
        if (principal <= 0) return 0;
        if (pagoMensual <= 0) return -1;
        double r = tasaAnualPct / 100.0 / 12.0;
        if (r == 0) return (int) Math.ceil(principal / pagoMensual);
        if (pagoMensual <= r * principal) return -1;
        double n = -Math.log(1 - r * principal / pagoMensual) / Math.log(1 + r);
        if (n > MAX_MESES) return -1;
        return (int) Math.ceil(n);
    }

    // ------------------------------------------------------------------
    // Utilidades
    // ------------------------------------------------------------------
    private static RecomendacionDTO rec(String tipo, String nivel, String titulo, String mensaje, Double potencial) {
        return new RecomendacionDTO(tipo, nivel, titulo, mensaje, potencial);
    }

    private static Double potencial(double v) {
        return v > 0.5 ? (double) Math.round(v) : null;
    }

    private static int ordenNivel(String nivel) {
        return switch (nivel) {
            case "ALERTA" -> 0;
            case "AVISO" -> 1;
            case "INFO" -> 2;
            case "POSITIVO" -> 3;
            default -> 4;
        };
    }

    private static String nombreEstrategia(String estrategia) {
        return "AVALANCHA".equals(estrategia)
                ? "avalancha (mayor interés primero)"
                : "bola de nieve (menor saldo primero)";
    }

    private static double sumaSuscripciones(List<GastoInfo> gastos, String categoria) {
        return gastos.stream()
                .filter(g -> categoria.equals(g.categoria()))
                .filter(g -> STREAMING.matcher(normalizar(g.descripcion())).find())
                .mapToDouble(GastoInfo::monto)
                .sum();
    }

    private static String detalleTop(List<GastoInfo> ordenadosDesc, int n) {
        List<String> partes = new ArrayList<>();
        for (int i = 0; i < Math.min(n, ordenadosDesc.size()); i++) {
            GastoInfo g = ordenadosDesc.get(i);
            String desc = (g.descripcion() == null || g.descripcion().isBlank()) ? "sin descripción" : g.descripcion();
            partes.add(desc + " " + clp(g.monto()));
        }
        return String.join(", ", partes);
    }

    private static String normalizar(String s) {
        if (s == null) return "";
        return Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
    }

    /** Formato de pesos chilenos con punto como separador de miles: $1.234.567 */
    static String clp(double v) {
        DecimalFormatSymbols simbolos = new DecimalFormatSymbols(Locale.forLanguageTag("es-CL"));
        simbolos.setGroupingSeparator('.');
        return "$" + new DecimalFormat("#,##0", simbolos).format(Math.round(v));
    }

    private static String pct(double ratio) {
        return Math.round(ratio * 100) + "%";
    }

    private static String num(double v) {
        return v == Math.rint(v)
                ? String.valueOf((long) v)
                : String.format(Locale.forLanguageTag("es-CL"), "%.1f", v);
    }

    private static String meses(int n) {
        return n == 1 ? "1 mes" : n + " meses";
    }
}
