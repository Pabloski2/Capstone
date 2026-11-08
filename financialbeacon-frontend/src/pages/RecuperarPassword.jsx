import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { api } from "../api/client.js";
import "../components/Perfil.css";

const ESPERA_REENVIO_SEGUNDOS = 60;

// Paso 1: el usuario escribe su correo y recibe un código de 6 dígitos.
// Paso 2: escribe el código y una contraseña nueva.
export default function RecuperarPassword() {
  const navigate = useNavigate();
  const [paso, setPaso] = useState(1);
  const [email, setEmail] = useState("");
  const [codigo, setCodigo] = useState("");
  const [nueva, setNueva] = useState("");
  const [repetida, setRepetida] = useState("");
  const [aviso, setAviso] = useState("");
  const [error, setError] = useState("");
  const [cargando, setCargando] = useState(false);
  const [espera, setEspera] = useState(0);

  useEffect(() => {
    if (espera <= 0) return undefined;
    const t = setTimeout(() => setEspera((s) => s - 1), 1000);
    return () => clearTimeout(t);
  }, [espera]);

  async function pedirCodigo(e) {
    e?.preventDefault();
    setError("");
    setCargando(true);
    try {
      const r = await api.solicitarRecuperacion(email.trim());
      setAviso(r.mensaje);
      setPaso(2);
      setEspera(ESPERA_REENVIO_SEGUNDOS);
    } catch (err) {
      setError(err.message || "No se pudo enviar el código");
    } finally {
      setCargando(false);
    }
  }

  async function cambiarPassword(e) {
    e.preventDefault();
    setError("");
    if (nueva !== repetida) {
      setError("Las contraseñas no coinciden");
      return;
    }
    setCargando(true);
    try {
      const r = await api.restablecerPassword(email.trim(), codigo.trim(), nueva);
      navigate("/login", { state: { mensaje: r.mensaje } });
    } catch (err) {
      setError(err.message || "No se pudo cambiar la contraseña");
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
          <h2>Recupera tu acceso.</h2>
          <p>Te enviamos un código de 6 dígitos a tu correo para que elijas una contraseña nueva.</p>
        </div>
        <div />
      </div>

      <div className="auth-panel-forma">
        <div className="auth-tarjeta">
          <h1>Recuperar contraseña</h1>
          <p className="subtitulo">
            {paso === 1 ? "Escribe el correo con el que te registraste" : "Revisa tu correo e ingresa el código"}
          </p>

          {aviso && paso === 2 && <div className="mensaje-exito">{aviso}</div>}
          {error && <div className="mensaje-error">{error}</div>}

          {paso === 1 ? (
            <form onSubmit={pedirCodigo}>
              <div className="campo">
                <label htmlFor="rec-email">Correo</label>
                <input
                  id="rec-email"
                  type="email"
                  autoComplete="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  required
                />
              </div>
              <button className="boton-primario" type="submit" disabled={cargando}>
                {cargando ? "Enviando..." : "Enviar código"}
              </button>
            </form>
          ) : (
            <form onSubmit={cambiarPassword}>
              <div className="campo">
                <label htmlFor="rec-codigo">Código de 6 dígitos</label>
                <input
                  id="rec-codigo"
                  inputMode="numeric"
                  autoComplete="one-time-code"
                  pattern="\d{6}"
                  maxLength={6}
                  value={codigo}
                  onChange={(e) => setCodigo(e.target.value.replace(/\D/g, ""))}
                  required
                />
              </div>
              <div className="campo">
                <label htmlFor="rec-nueva">Contraseña nueva</label>
                <input
                  id="rec-nueva"
                  type="password"
                  autoComplete="new-password"
                  minLength={8}
                  value={nueva}
                  onChange={(e) => setNueva(e.target.value)}
                  required
                />
                <small className="ayuda-campo">Mínimo 8 caracteres.</small>
              </div>
              <div className="campo">
                <label htmlFor="rec-repetida">Repite la contraseña nueva</label>
                <input
                  id="rec-repetida"
                  type="password"
                  autoComplete="new-password"
                  minLength={8}
                  value={repetida}
                  onChange={(e) => setRepetida(e.target.value)}
                  required
                />
              </div>
              <button className="boton-primario" type="submit" disabled={cargando}>
                {cargando ? "Guardando..." : "Cambiar contraseña"}
              </button>
              <button
                type="button"
                className="boton-secundario"
                style={{ width: "100%", marginTop: 10 }}
                onClick={pedirCodigo}
                disabled={cargando || espera > 0}
              >
                {espera > 0 ? `Reenviar código (${espera} s)` : "Reenviar código"}
              </button>
            </form>
          )}

          <Link className="enlace-secundario" to="/login">
            Volver a iniciar sesión
          </Link>
        </div>
      </div>
    </div>
  );
}
