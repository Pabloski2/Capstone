import "./AvisoDatos.css";

/**
 * Notificación que aparece arriba cuando faltan datos para armar el plan de inversión.
 * Cada dato faltante trae un botón que lleva directo al lugar donde se completa.
 *
 * faltantes: [{ clave, texto, boton, accion }]
 */
export default function AvisoDatosFaltantes({ faltantes, onCerrar }) {
  if (!faltantes || faltantes.length === 0) return null;

  return (
    <div className="aviso-datos" role="status" aria-live="polite">
      <div className="aviso-datos-cabecera">
        <span className="aviso-datos-icono" aria-hidden="true">
          !
        </span>
        <strong>Faltan datos para tu plan de inversión</strong>
        <button type="button" className="aviso-datos-cerrar" aria-label="Cerrar aviso" onClick={onCerrar}>
          ×
        </button>
      </div>
      <ul className="aviso-datos-lista">
        {faltantes.map((f) => (
          <li key={f.clave}>
            <span>{f.texto}</span>
            <button type="button" className="aviso-datos-boton" onClick={f.accion}>
              {f.boton}
            </button>
          </li>
        ))}
      </ul>
    </div>
  );
}
