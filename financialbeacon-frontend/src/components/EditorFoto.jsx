import { useRef, useState } from "react";
import { redimensionarFoto } from "../utils/imagen.js";
import Avatar from "./Avatar.jsx";
import "./Perfil.css";

// Elegir foto (galería o cámara), reducirla y entregarla a onGuardar(dataUrl).
export default function EditorFoto({ nombre, foto, onGuardar, onEliminar }) {
  const entrada = useRef(null);
  const [trabajando, setTrabajando] = useState(false);
  const [error, setError] = useState("");

  async function alElegir(e) {
    const archivo = e.target.files?.[0];
    e.target.value = ""; // permite volver a elegir la misma foto
    if (!archivo) return;
    setError("");
    setTrabajando(true);
    try {
      await onGuardar(await redimensionarFoto(archivo));
    } catch (err) {
      setError(err.message || "No se pudo guardar la foto");
    } finally {
      setTrabajando(false);
    }
  }

  async function quitar() {
    setError("");
    setTrabajando(true);
    try {
      await onEliminar();
    } catch (err) {
      setError(err.message || "No se pudo quitar la foto");
    } finally {
      setTrabajando(false);
    }
  }

  return (
    <div className="editor-foto">
      <Avatar nombre={nombre} foto={foto} tamano={88} />
      <div className="editor-foto-acciones">
        <input ref={entrada} type="file" accept="image/*" hidden onChange={alElegir} />
        <button
          type="button"
          className="boton-secundario"
          onClick={() => entrada.current?.click()}
          disabled={trabajando}
        >
          {trabajando ? "Guardando..." : foto ? "Cambiar foto" : "Subir foto"}
        </button>
        {foto && (
          <button type="button" className="boton-texto" onClick={quitar} disabled={trabajando}>
            Quitar foto
          </button>
        )}
        {error && <p className="texto-error">{error}</p>}
      </div>
    </div>
  );
}
