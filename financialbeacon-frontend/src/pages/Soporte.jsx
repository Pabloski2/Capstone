import { useEffect, useState } from "react";
import { api } from "../api/client.js";
import BarraNavegacion from "../components/BarraNavegacion.jsx";
import "../components/Perfil.css";

const CATEGORIAS = [
  { valor: "ERROR", etiqueta: "Algo no funciona" },
  { valor: "CUENTA", etiqueta: "Problema con mi cuenta" },
  { valor: "SUGERENCIA", etiqueta: "Sugerencia" },
  { valor: "OTRO", etiqueta: "Otro" },
];

const FAQ = [
  {
    p: "No me llega el código para recuperar mi contraseña",
    r: "Revisa la carpeta de spam o correo no deseado. El código llega en menos de un minuto y dura 15 minutos. Puedes pedir uno nuevo cada 60 segundos. Tras 5 intentos incorrectos se bloquea por una hora.",
  },
  {
    p: "¿Cómo cambio mi correo o mi contraseña?",
    r: "En “Mi cuenta” encontrarás las secciones Correo electrónico y Contraseña. Te pediremos tu contraseña actual para confirmar.",
  },
  {
    p: "Registré un gasto o una deuda por error",
    r: "En Resumen y en Deudas cada registro tiene un botón para eliminarlo. Luego puedes volver a crearlo con los datos correctos.",
  },
  {
    p: "¿Las recomendaciones de inversión son asesoría financiera?",
    r: "No. Son información educativa basada en tus datos y reglas generales. Antes de invertir revisa riesgos, comisiones y condiciones con tu institución o con un asesor autorizado.",
  },
  {
    p: "¿Cómo elimino mi cuenta y mis datos?",
    r: "En “Mi cuenta”, al final, está “Eliminar mi cuenta”. Se borran tu cuenta y todos tus datos de forma permanente.",
  },
];

function formatoFecha(iso) {
  try {
    return new Date(iso).toLocaleDateString("es-CL", { day: "numeric", month: "short", year: "numeric" });
  } catch {
    return "";
  }
}

export default function Soporte() {
  const [asunto, setAsunto] = useState("");
  const [categoria, setCategoria] = useState("ERROR");
  const [mensaje, setMensaje] = useState("");
  const [tickets, setTickets] = useState([]);
  const [error, setError] = useState("");
  const [exito, setExito] = useState("");
  const [enviando, setEnviando] = useState(false);

  async function cargar() {
    try {
      setTickets(await api.listarTickets());
    } catch (err) {
      setError(err.message || "No se pudieron cargar tus solicitudes");
    }
  }

  useEffect(() => {
    cargar();
  }, []);

  async function enviar(e) {
    e.preventDefault();
    setError("");
    setExito("");
    setEnviando(true);
    try {
      const t = await api.crearTicket(asunto.trim(), categoria, mensaje.trim());
      setExito(`Recibimos tu solicitud #${t.id}. Te responderemos a tu correo.`);
      setAsunto("");
      setMensaje("");
      await cargar();
    } catch (err) {
      setError(err.message || "No se pudo enviar tu solicitud");
    } finally {
      setEnviando(false);
    }
  }

  return (
    <>
      <BarraNavegacion paginaActiva="soporte" />
      <div className="contenedor-dashboard" style={{ maxWidth: 760 }}>
        <header className="encabezado-dashboard">
          <h1>Soporte técnico</h1>
          <p className="subtitulo">Resuelve tus dudas o escríbenos</p>
        </header>

        <section className="tarjeta seccion-cuenta faq">
          <h2>Preguntas frecuentes</h2>
          {FAQ.map((f) => (
            <details key={f.p}>
              <summary>{f.p}</summary>
              <p>{f.r}</p>
            </details>
          ))}
        </section>

        <section className="tarjeta seccion-cuenta">
          <h2>Contactar a soporte</h2>
          <p className="ayuda-campo">Te respondemos al correo de tu cuenta.</p>
          {error && <div className="mensaje-error">{error}</div>}
          {exito && <div className="mensaje-exito" role="status">{exito}</div>}
          <form onSubmit={enviar}>
            <div className="formulario-grid">
              <div className="campo">
                <label htmlFor="s-categoria">Tipo de consulta</label>
                <select id="s-categoria" value={categoria} onChange={(e) => setCategoria(e.target.value)}>
                  {CATEGORIAS.map((c) => (
                    <option key={c.valor} value={c.valor}>
                      {c.etiqueta}
                    </option>
                  ))}
                </select>
              </div>
              <div className="campo">
                <label htmlFor="s-asunto">Asunto</label>
                <input id="s-asunto" value={asunto} onChange={(e) => setAsunto(e.target.value)} maxLength={150} required />
              </div>
            </div>
            <div className="campo">
              <label htmlFor="s-mensaje">Cuéntanos qué pasó</label>
              <textarea id="s-mensaje" value={mensaje} onChange={(e) => setMensaje(e.target.value)} minLength={10} maxLength={2000} required />
              <small className="ayuda-campo">{mensaje.length}/2000. Mientras más detalles, más rápido podemos ayudarte.</small>
            </div>
            <button className="boton-primario" type="submit" disabled={enviando}>
              {enviando ? "Enviando..." : "Enviar solicitud"}
            </button>
          </form>
        </section>

        <section className="tarjeta seccion-cuenta">
          <h2>Mis solicitudes</h2>
          {tickets.length === 0 ? (
            <p className="ayuda-campo">Aún no has enviado ninguna.</p>
          ) : (
            <ul className="lista-tickets">
              {tickets.map((t) => (
                <li key={t.id} className="ticket">
                  <div className="ticket-cabecera">
                    <strong>
                      #{t.id} · {t.asunto}
                    </strong>
                    <span className="chip-estado">{t.estado === "ABIERTO" ? "Abierta" : t.estado}</span>
                  </div>
                  <p className="ticket-meta">
                    {CATEGORIAS.find((c) => c.valor === t.categoria)?.etiqueta || t.categoria} · {formatoFecha(t.creadoEn)}
                  </p>
                </li>
              ))}
            </ul>
          )}
        </section>
      </div>
    </>
  );
}
