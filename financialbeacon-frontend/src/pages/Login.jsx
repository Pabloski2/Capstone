import { useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { api } from "../api/client.js";
import { guardarSesion, rutaTrasLogin } from "../utils/sesion.js";
import "../components/Perfil.css";

export default function Login() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [cargando, setCargando] = useState(false);
  const navigate = useNavigate();
  const aviso = useLocation().state?.mensaje; // por ejemplo, tras restablecer la contraseña

  async function manejarSubmit(e) {
    e.preventDefault();
    setError("");
    setCargando(true);
    try {
      const usuario = await api.login(email, password);
      guardarSesion(usuario);
      navigate(rutaTrasLogin(usuario));
    } catch (err) {
      setError(err.message || "No se pudo iniciar sesión");
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
          <h2>Tu plata, con rumbo claro.</h2>
          <p>
            Registra tus ingresos y gastos, y deja que el sistema te muestre
            cuánto puedes ahorrar y cómo salir de tus deudas más rápido.
          </p>
        </div>
        <div />
      </div>

      <div className="auth-panel-forma">
        <div className="auth-tarjeta">
          <h1>Bienvenido de vuelta</h1>
          <p className="subtitulo">Inicia sesión para ver tu estado financiero</p>

          {aviso && !error && <div className="mensaje-exito" role="status">{aviso}</div>}
          {error && <div className="mensaje-error">{error}</div>}

          <form onSubmit={manejarSubmit}>
            <div className="campo">
              <label htmlFor="email">Correo</label>
              <input
                id="email"
                type="email"
                autoComplete="email"
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
                autoComplete="current-password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
            </div>
            <button className="boton-primario" type="submit" disabled={cargando}>
              {cargando ? "Ingresando..." : "Ingresar"}
            </button>
          </form>

          <Link className="enlace-secundario" to="/recuperar">
            ¿Olvidaste tu contraseña?
          </Link>
          <Link className="enlace-secundario" to="/register">
            ¿No tienes cuenta? Regístrate
          </Link>
        </div>
      </div>
    </div>
  );
}
