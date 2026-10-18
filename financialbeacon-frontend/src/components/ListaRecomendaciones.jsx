import { formatoCLP } from "../utils/formato.js";
import "./ListaRecomendaciones.css";

const NIVELES = {
  ALERTA: { icono: "!", clase: "recomendacion-alerta", etiqueta: "Alerta" },
  AVISO: { icono: "!", clase: "recomendacion-aviso", etiqueta: "Aviso" },
  INFO: { icono: "i", clase: "recomendacion-info", etiqueta: "Información" },
  POSITIVO: { icono: "✓", clase: "recomendacion-positivo", etiqueta: "Buena noticia" },
};

/**
 * Muestra las recomendaciones que calcula el backend.
 * - filtro: función opcional para elegir cuáles mostrar en esta pantalla.
 * - children: contenido extra bajo el título (por ejemplo, el formulario de meta de ahorro).
 */
export default function ListaRecomendaciones({
  recomendaciones,
  filtro,
  titulo = "Recomendaciones para ti",
  vacio,
  children,
}) {
  if (!recomendaciones) return null;
  const items = filtro ? recomendaciones.filter(filtro) : recomendaciones;
  if (items.length === 0 && !vacio && !children) return null;

  return (
    <section className="seccion-recomendaciones">
      <h2 className="titulo-seccion">{titulo}</h2>
      {children}
      {items.length === 0 ? (
        <p className="estado-vacio">{vacio}</p>
      ) : (
        <ul className="lista-recomendaciones">
          {items.map((r, i) => {
            const nivel = NIVELES[r.nivel] || NIVELES.INFO;
            return (
              <li key={i} className={`recomendacion-item ${nivel.clase}`}>
                <span className="recomendacion-icono" aria-hidden="true">
                  {nivel.icono}
                </span>
                <div>
                  <h3>
                    <span className="solo-lectores">{nivel.etiqueta}: </span>
                    {r.titulo}
                  </h3>
                  <p>{r.mensaje}</p>
                  {r.ahorroPotencial > 0 && (
                    <span className="chip-ahorro">
                      Podrías liberar {formatoCLP(r.ahorroPotencial)} al mes
                    </span>
                  )}
                </div>
              </li>
            );
          })}
        </ul>
      )}
    </section>
  );
}
