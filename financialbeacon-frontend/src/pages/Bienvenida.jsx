import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { api } from "../api/client.js";
import EditorFoto from "../components/EditorFoto.jsx";
import FormularioPerfil from "../components/FormularioPerfil.jsx";
import { actualizarSesion, cerrarSesionLocal, leerSesion } from "../utils/sesion.js";
import "../components/Perfil.css";

// Primer paso después de iniciar sesión por primera vez: completar los datos personales.
export default function Bienvenida() {
  const navigate = useNavigate();
  const sesion = leerSesion();
  const [foto, setFoto] = useState(sesion?.fotoPerfil || null);

  if (!sesion) {
    navigate("/login");
    return null;
  }

  async function guardarFoto(dataUrl) {
    const p = await api.guardarFoto(dataUrl);
    setFoto(p.fotoPerfil);
    actualizarSesion({ fotoPerfil: p.fotoPerfil });
  }

  async function quitarFoto() {
    await api.eliminarFoto();
    setFoto(null);
    actualizarSesion({ fotoPerfil: null });
  }

  async function guardarDatos(datos) {
    const p = await api.guardarPerfil(datos);
    actualizarSesion({ perfilCompleto: p.perfilCompleto, nombre: p.nombre });
    navigate("/dashboard");
  }

  function salir() {
    cerrarSesionLocal();
    navigate("/login");
  }

  return (
    <div className="contenedor-dashboard" style={{ maxWidth: 720 }}>
      <header className="encabezado-dashboard">
        <h1>¡Te damos la bienvenida!</h1>
        <p className="subtitulo">Antes de empezar, cuéntanos un poco sobre ti.</p>
      </header>

      <ol className="pasos" aria-label="Progreso">
        <li className="paso activo" aria-current="step">
          <span className="paso-numero">1</span> Tus datos
        </li>
        <li className="pasos-linea" aria-hidden="true" />
        <li className="paso">
          <span className="paso-numero">2</span> Tu resumen
        </li>
      </ol>

      <section className="tarjeta seccion-cuenta">
        <h2>Foto de perfil (opcional)</h2>
        <p className="ayuda-campo">Puedes elegirla ahora o más tarde en “Mi cuenta”.</p>
        <EditorFoto nombre={sesion.nombre} foto={foto} onGuardar={guardarFoto} onEliminar={quitarFoto} />
      </section>

      <section className="tarjeta seccion-cuenta">
        <h2>Datos personales</h2>
        <p className="ayuda-campo">Todos los campos son obligatorios.</p>
        <FormularioPerfil
          inicial={{ nombre: sesion.nombre }}
          textoBoton="Guardar y continuar"
          onGuardar={guardarDatos}
        />
      </section>

      <p className="pie-cuenta">
        <button type="button" className="boton-texto" style={{ color: "var(--tinta-suave)" }} onClick={salir}>
          Cerrar sesión
        </button>
      </p>
    </div>
  );
}
