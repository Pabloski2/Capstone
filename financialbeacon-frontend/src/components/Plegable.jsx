import { useId, useState } from "react";
import "./Plegable.css";

const CLAVE = "fb_plegables";

function leer() {
  try {
    const guardado = JSON.parse(localStorage.getItem(CLAVE) || "{}");
    return guardado && typeof guardado === "object" ? guardado : {};
  } catch {
    return {};
  }
}

/** Recuerda en este dispositivo si cada recuadro quedó abierto o encogido. */
export function useAbierto(id, abiertoPorDefecto = true) {
  const [abierto, setAbierto] = useState(() => {
    const guardado = leer()[id];
    return typeof guardado === "boolean" ? guardado : abiertoPorDefecto;
  });

  function alternar() {
    const nuevo = !abierto;
    setAbierto(nuevo);
    try {
      localStorage.setItem(CLAVE, JSON.stringify({ ...leer(), [id]: nuevo }));
    } catch {
      // Sin almacenamiento disponible: el recuadro igual se abre y se cierra en pantalla.
    }
  }

  return [abierto, alternar];
}

/**
 * Tarjeta que se puede encoger. Encogida muestra solo el título y un resumen de una línea;
 * la pestañita de la derecha la abre y la cierra.
 */
export default function Plegable({ id, titulo, resumen, abiertoPorDefecto = true, className = "", children }) {
  const [abierto, alternar] = useAbierto(id, abiertoPorDefecto);
  const panelId = useId();

  return (
    <section className={`tarjeta plegable${abierto ? " abierto" : ""} ${className}`.trim()}>
      <h2 className="plegable-h">
        <button
          type="button"
          className="plegable-cabecera"
          aria-expanded={abierto}
          aria-controls={panelId}
          onClick={alternar}
        >
          <span className="plegable-textos">
            <span className="plegable-titulo">{titulo}</span>
            {!abierto && resumen && <span className="plegable-resumen">{resumen}</span>}
          </span>
          <span className="plegable-tab" aria-hidden="true">
            ▾
          </span>
        </button>
      </h2>
      <div className="plegable-panel" id={panelId} hidden={!abierto}>
        {children}
      </div>
    </section>
  );
}
