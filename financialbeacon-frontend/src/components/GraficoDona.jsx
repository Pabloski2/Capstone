import { formatoCLP } from "../utils/formato.js";

const SEGMENTOS = [
  { clave: "fijos", etiqueta: "Fijos", color: "var(--c-fijo)" },
  { clave: "variables", etiqueta: "Variables", color: "var(--c-variable)" },
  { clave: "hormiga", etiqueta: "Hormiga", color: "var(--c-hormiga)" },
];

const RADIO = 46;
const CIRCUNFERENCIA = 2 * Math.PI * RADIO;

/** Gráfico de dona: cómo se reparten los gastos del mes entre las 3 categorías. */
export default function GraficoDona({ fijos = 0, variables = 0, hormiga = 0 }) {
  const valores = { fijos, variables, hormiga };
  const total = fijos + variables + hormiga;

  let acumulado = 0;
  const arcos = SEGMENTOS.map((s) => {
    const valor = valores[s.clave] || 0;
    const largo = total > 0 ? (valor / total) * CIRCUNFERENCIA : 0;
    const arco = {
      ...s,
      valor,
      largo,
      inicio: acumulado,
      porcentaje: total > 0 ? Math.round((valor / total) * 100) : 0,
    };
    acumulado += largo;
    return arco;
  });

  const resumen = arcos.map((a) => `${a.etiqueta} ${formatoCLP(a.valor)}`).join(", ");

  return (
    <div className="dona-contenedor">
      <div className="dona" role="img" aria-label={`Gastos por categoría: ${resumen}`}>
        <svg viewBox="0 0 120 120">
          <circle className="dona-fondo" cx="60" cy="60" r={RADIO} fill="none" strokeWidth="16" />
          {arcos
            .filter((a) => a.largo > 0)
            .map((a) => (
              <circle
                key={a.clave}
                cx="60"
                cy="60"
                r={RADIO}
                fill="none"
                strokeWidth="16"
                style={{ stroke: a.color }}
                // Se resta un pequeño espacio para separar visualmente los segmentos.
                strokeDasharray={`${Math.max(a.largo - 1.5, 0.5)} ${CIRCUNFERENCIA}`}
                strokeDashoffset={-a.inicio}
                transform="rotate(-90 60 60)"
              />
            ))}
        </svg>
        <div className="dona-centro">
          <span className="dona-total">{formatoCLP(total)}</span>
          <span className="dona-etiqueta">en gastos</span>
        </div>
      </div>

      <ul className="leyenda-lista">
        {arcos.map((a) => (
          <li key={a.clave}>
            <span className="punto-categoria" style={{ background: a.color }} />
            <span>
              {a.etiqueta}
              <span className="leyenda-pct">{a.porcentaje}%</span>
            </span>
            <span className="leyenda-monto">{formatoCLP(a.valor)}</span>
          </li>
        ))}
      </ul>
    </div>
  );
}
