import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { api } from "../api/client.js";

export default function Register() {
  const [nombre, setNombre] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [cargando, setCargando] = useState(false);
  const navigate = useNavigate();

  async function manejarSubmit(e) {
    e.preventDefault();
    setError("");
    setCargando(true);
    try {
      const usuario = await api.registrar(email, password, nombre);
      localStorage.setItem("fb_user", JSON.stringify(usuario));
      navigate("/dashboard");
    } catch (err) {
      setError(err.message || "No se pudo completar el registro");
    } finally {
      setCargando(false);
    }
  }

  return (
    <div className="auth-layout">
      <div className="auth-panel-faro">
        <div className="auth-marca">
          <span className="auth-marca-punto" />
          Financial Beacon
        </div>
        <div className="auth-mensaje">
          <h2>Empieza a ver tu plata distinto.</h2>
          <p>
            Un minuto para crear tu cuenta, y desde ahí el sistema arma solo
            tu resumen mensual y tu plan de pago de deudas.
          </p>
        </div>
        <div />
      </div>

      <div className="auth-panel-forma">
        <div className="auth-tarjeta">
          <h1>Crea tu cuenta</h1>
          <p className="subtitulo">Empieza a organizar tus finanzas</p>

          {error && <div className="mensaje-error">{error}</div>}

          <form onSubmit={manejarSubmit}>
            <div className="campo">
              <label htmlFor="nombre">Nombre</label>
              <input
                id="nombre"
                type="text"
                value={nombre}
                onChange={(e) => setNombre(e.target.value)}
                required
              />
            </div>
            <div className="campo">
              <label htmlFor="email">Correo</label>
              <input
                id="email"
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
              />
            </div>
            <div className="campo">
              <label htmlFor="password">Contraseña</label>
              <input
                id="password"
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
            </div>
            <button className="boton-primario" type="submit" disabled={cargando}>
              {cargando ? "Creando cuenta..." : "Registrarme"}
            </button>
          </form>

          <Link className="enlace-secundario" to="/login">
            ¿Ya tienes cuenta? Inicia sesión
          </Link>
        </div>
      </div>
    </div>
  );
}
