import { formatoCLP } from "../utils/formato.js";

/**
 * Barra que muestra en qué se va el ingreso del mes: fijos, variables, hormiga y lo que queda.
 * Si los gastos superan el ingreso, una marca blanca indica hasta dónde llegaba el ingreso.
 */
export default function BarraReparto({ ingreso = 0, fijos = 0, variables = 0, hormiga = 0 }) {
  const gastos = fijos + variables + hormiga;
  const base = Math.max(ingreso, gastos);
  if (base <= 0) return null;

  const ahorro = Math.max(ingreso - gastos, 0);
  const pct = (v) => `${(v / base) * 100}%`;

  const segmentos = [
    { clase: "seg-fijo", etiqueta: "Fijos", valor: fijos },
    { clase: "seg-variable", etiqueta: "Variables", valor: variables },
    { clase: "seg-hormiga", etiqueta: "Hormiga", valor: hormiga },
    { clase: "seg-ahorro", etiqueta: "Te queda", valor: ahorro },
  ].filter((s) => s.valor > 0);

  const sobrepasa = gastos > ingreso && ingreso > 0;
  const descripcion = segmentos.map((s) => `${s.etiqueta} ${formatoCLP(s.valor)}`).join(", ");

  return (
    <div>
      <div className="barra-reparto" role="img" aria-label={`Reparto de tu ingreso: ${descripcion}`}>
        {segmentos.map((s) => (
          <span key={s.clase} className={s.clase} style={{ width: pct(s.valor) }} />
        ))}
        {sobrepasa && <i className="marca-ingreso" style={{ left: pct(ingreso) }} title="Tu ingreso" />}
      </div>
      <div className="leyenda-reparto" aria-hidden="true">
        {segmentos.map((s) => (
          <span key={s.clase}>
            <i className={s.clase} />
            {s.etiqueta} {formatoCLP(s.valor)}
          </span>
        ))}
        {sobrepasa && <span>La línea blanca marca tu ingreso</span>}
      </div>
    </div>
  );
}
