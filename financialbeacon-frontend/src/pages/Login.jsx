import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { api } from "../api/client.js";

export default function Login() {
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
      const usuario = await api.login(email, password);
      localStorage.setItem("fb_user", JSON.stringify(usuario));
      navigate("/dashboard");
    } catch (err) {
      setError(err.message || "No se pudo iniciar sesión");
    } finally {
      setCargando(false);
    }
  }

  return (
    <div className="pantalla-centrada">
      <div className="tarjeta">
        <h1>Financial Beacon</h1>
        <p className="subtitulo">Inicia sesión para ver tu estado financiero</p>

        {error && <div className="mensaje-error">{error}</div>}

        <form onSubmit={manejarSubmit}>
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
            {cargando ? "Ingresando..." : "Ingresar"}
          </button>
        </form>

        <Link className="enlace-secundario" to="/register">
          ¿No tienes cuenta? Regístrate
        </Link>
      </div>
    </div>
  );
}
