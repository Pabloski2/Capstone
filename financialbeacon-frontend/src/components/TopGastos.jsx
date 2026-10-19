import { formatoCLP } from "../utils/formato.js";

const COLOR = {
  FIJO: "var(--c-fijo)",
  VARIABLE: "var(--c-variable)",
  HORMIGA: "var(--c-hormiga)",
};

/** Los gastos individuales más altos del mes, con barras proporcionales y el color de su categoría. */
export default function TopGastos({ gastos = [], limite = 5 }) {
  const top = [...gastos].sort((a, b) => b.monto - a.monto).slice(0, limite);

  if (top.length === 0) {
    return <p className="estado-vacio">Cuando registres gastos, aquí verás en cuáles se va más plata.</p>;
  }

  const maximo = top[0].monto || 1;

  return (
    <ul className="lista-top">
      {top.map((g) => (
        <li key={g.id} className="fila-top">
          <span className="nombre">{g.descripcion}</span>
          <span className="monto">{formatoCLP(g.monto)}</span>
          <div className="pista">
            <span style={{ width: `${(g.monto / maximo) * 100}%`, background: COLOR[g.categoria] }} />
          </div>
        </li>
      ))}
    </ul>
  );
}
