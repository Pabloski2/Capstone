import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { api } from "../api/client.js";
import BarraNavegacion from "../components/BarraNavegacion.jsx";
import EditorFoto from "../components/EditorFoto.jsx";
import FormularioPerfil from "../components/FormularioPerfil.jsx";
import { actualizarSesion, cerrarSesionLocal, guardarSesion, leerSesion } from "../utils/sesion.js";
import "../components/Perfil.css";

export default function Cuenta() {
  const navigate = useNavigate();
  const sesion = leerSesion();

  const [perfil, setPerfil] = useState(null);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState("");
  const [exito, setExito] = useState("");

  useEffect(() => {
    if (!sesion) {
      navigate("/login");
      return;
    }
    api
      .obtenerPerfil()
      .then(setPerfil)
      .catch((err) => setError(err.message || "No se pudo cargar tu cuenta"))
      .finally(() => setCargando(false));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  function avisar(mensaje) {
    setError("");
    setExito(mensaje);
  }

  async function guardarDatos(datos) {
    const p = await api.guardarPerfil(datos);
    setPerfil(p);
    actualizarSesion({ nombre: p.nombre, perfilCompleto: p.perfilCompleto });
    avisar("Tus datos se guardaron.");
  }

  async function guardarFoto(dataUrl) {
    const p = await api.guardarFoto(dataUrl);
    setPerfil(p);
    actualizarSesion({ fotoPerfil: p.fotoPerfil });
    avisar("Tu foto de perfil se actualizó.");
  }

  async function quitarFoto() {
    const p = await api.eliminarFoto();
    setPerfil(p);
    actualizarSesion({ fotoPerfil: null });
    avisar("Quitamos tu foto de perfil.");
  }

  function salir() {
    cerrarSesionLocal();
    navigate("/login");
  }

  if (cargando) {
    return (
      <div className="pantalla-centrada">
        <p>Cargando tu cuenta...</p>
      </div>
    );
  }

  return (
    <>
      <BarraNavegacion paginaActiva="cuenta" />
      <div className="contenedor-dashboard" style={{ maxWidth: 760 }}>
        <header className="encabezado-dashboard">
          <h1>Mi cuenta</h1>
          <p className="subtitulo">{perfil?.email || sesion?.email}</p>
        </header>

        {error && <div className="mensaje-error">{error}</div>}
        {exito && <div className="mensaje-exito" role="status">{exito}</div>}

        {perfil && (
          <>
            <section className="tarjeta seccion-cuenta">
              <h2>Foto de perfil</h2>
              <p className="ayuda-campo">Se reduce automáticamente para que cargue rápido.</p>
              <EditorFoto nombre={perfil.nombre} foto={perfil.fotoPerfil} onGuardar={guardarFoto} onEliminar={quitarFoto} />
            </section>

            <section className="tarjeta seccion-cuenta">
              <h2>Datos personales</h2>
              <p className="ayuda-campo">Mantén tu información al día.</p>
              <FormularioPerfil inicial={perfil} textoBoton="Guardar cambios" onGuardar={guardarDatos} />
            </section>

            <CambiarEmail actual={perfil.email} onListo={(p) => { setPerfil(p); actualizarSesion({ email: p.email }); avisar("Tu correo se actualizó."); }} />

            <CambiarPassword
              onListo={(resp) => {
                guardarSesion({ ...resp, fotoPerfil: perfil.fotoPerfil });
                avisar("Tu contraseña se cambió. Las demás sesiones abiertas se cerraron.");
              }}
            />
          </>
        )}

        <section className="tarjeta seccion-cuenta">
          <h2>Ayuda y sesión</h2>
          <div className="acciones-cuenta">
            <Link className="boton-secundario" to="/soporte">
              Soporte técnico
            </Link>
            <button type="button" className="boton-secundario" onClick={salir}>
              Cerrar sesión
            </button>
          </div>
        </section>

        <EliminarCuenta onEliminada={() => { cerrarSesionLocal(); navigate("/login"); }} />
      </div>
    </>
  );
}

function CambiarEmail({ actual, onListo }) {
  const [nuevoEmail, setNuevoEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [guardando, setGuardando] = useState(false);

  async function enviar(e) {
    e.preventDefault();
    setError("");
    setGuardando(true);
    try {
      onListo(await api.cambiarEmail(nuevoEmail.trim(), password));
      setNuevoEmail("");
      setPassword("");
    } catch (err) {
      setError(err.message || "No se pudo cambiar el correo");
    } finally {
      setGuardando(false);
    }
  }

  return (
    <section className="tarjeta seccion-cuenta">
      <h2>Correo electrónico</h2>
      <p className="ayuda-campo">Actual: {actual}. Recibirás aquí los códigos de recuperación.</p>
      {error && <div className="mensaje-error">{error}</div>}
      <form onSubmit={enviar}>
        <div className="formulario-grid">
          <div className="campo">
            <label htmlFor="nuevo-email">Correo nuevo</label>
            <input id="nuevo-email" type="email" autoComplete="email" value={nuevoEmail} onChange={(e) => setNuevoEmail(e.target.value)} required />
          </div>
          <div className="campo">
            <label htmlFor="email-password">Tu contraseña (para confirmar)</label>
            <input id="email-password" type="password" autoComplete="current-password" value={password} onChange={(e) => setPassword(e.target.value)} required />
          </div>
        </div>
        <button className="boton-primario" type="submit" disabled={guardando}>
          {guardando ? "Guardando..." : "Cambiar correo"}
        </button>
      </form>
    </section>
  );
}

function CambiarPassword({ onListo }) {
  const [actual, setActual] = useState("");
  const [nueva, setNueva] = useState("");
  const [repetida, setRepetida] = useState("");
  const [error, setError] = useState("");
  const [guardando, setGuardando] = useState(false);

  async function enviar(e) {
    e.preventDefault();
    setError("");
    if (nueva !== repetida) {
      setError("Las contraseñas nuevas no coinciden");
      return;
    }
    setGuardando(true);
    try {
      onListo(await api.cambiarPassword(actual, nueva));
      setActual("");
      setNueva("");
      setRepetida("");
    } catch (err) {
      setError(err.message || "No se pudo cambiar la contraseña");
    } finally {
      setGuardando(false);
    }
  }

  return (
    <section className="tarjeta seccion-cuenta">
      <h2>Contraseña</h2>
      <p className="ayuda-campo">Mínimo 8 caracteres.</p>
      {error && <div className="mensaje-error">{error}</div>}
      <form onSubmit={enviar}>
        <div className="campo">
          <label htmlFor="pw-actual">Contraseña actual</label>
          <input id="pw-actual" type="password" autoComplete="current-password" value={actual} onChange={(e) => setActual(e.target.value)} required />
        </div>
        <div className="formulario-grid">
          <div className="campo">
            <label htmlFor="pw-nueva">Contraseña nueva</label>
            <input id="pw-nueva" type="password" autoComplete="new-password" minLength={8} value={nueva} onChange={(e) => setNueva(e.target.value)} required />
          </div>
          <div className="campo">
            <label htmlFor="pw-repetida">Repite la nueva</label>
            <input id="pw-repetida" type="password" autoComplete="new-password" minLength={8} value={repetida} onChange={(e) => setRepetida(e.target.value)} required />
          </div>
        </div>
        <button className="boton-primario" type="submit" disabled={guardando}>
          {guardando ? "Guardando..." : "Cambiar contraseña"}
        </button>
      </form>
    </section>
  );
}

function EliminarCuenta({ onEliminada }) {
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [eliminando, setEliminando] = useState(false);

  async function eliminar(e) {
    e.preventDefault();
    if (!window.confirm("Esta acción borra tu cuenta y todos tus datos de forma permanente. ¿Continuar?")) return;
    setError("");
    setEliminando(true);
    try {
      await api.eliminarCuenta(password);
      onEliminada();
    } catch (err) {
      setError(err.message || "No se pudo eliminar la cuenta");
      setEliminando(false);
    }
  }

  return (
    <section className="tarjeta zona-peligro seccion-cuenta">
      <h2>Eliminar mi cuenta</h2>
      <p className="ayuda-campo">
        Se borrarán tu cuenta, tus ingresos, gastos, deudas y solicitudes de soporte. No se puede deshacer.
        Para confirmar, escribe tu contraseña.
      </p>
      {error && <div className="mensaje-error">{error}</div>}
      <form onSubmit={eliminar}>
        <div className="campo">
          <label htmlFor="password-eliminar">Contraseña</label>
          <input id="password-eliminar" type="password" autoComplete="current-password" value={password} onChange={(e) => setPassword(e.target.value)} required />
        </div>
        <button className="boton-peligro" type="submit" disabled={eliminando}>
          {eliminando ? "Eliminando..." : "Eliminar mi cuenta definitivamente"}
        </button>
      </form>
    </section>
  );
}
