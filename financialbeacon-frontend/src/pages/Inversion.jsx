import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { api } from "../api/client.js";
import BarraNavegacion from "../components/BarraNavegacion.jsx";
import { formatoCLP } from "../utils/formato.js";
import "./Inversion.css";

const CLAVE_PREFS = "fb_inversion";

const PERFILES = [
  { valor: "CONSERVADOR", nombre: "Conservador", detalle: "Prefiero no perder dinero, aunque gane menos." },
  { valor: "MODERADO", nombre: "Moderado", detalle: "Acepto altibajos a cambio de ganar algo más." },
  { valor: "AGRESIVO", nombre: "Agresivo", detalle: "Tolero caídas fuertes por un mayor potencial." },
];

const HORIZONTES = [
  { meses: 6, etiqueta: "6 meses" },
  { meses: 12, etiqueta: "1 año" },
  { meses: 36, etiqueta: "3 años" },
  { meses: 60, etiqueta: "5 años" },
  { meses: 120, etiqueta: "10 años" },
  { meses: 240, etiqueta: "20 años" },
];

const ETAPAS = {
  SIN_DATOS: { etiqueta: "Falta información", clase: "etapa-info" },
  SIN_EXCEDENTE: { etiqueta: "Sin excedente", clase: "etapa-alerta" },
  PAGAR_DEUDA: { etiqueta: "Paso 1 · Pagar deudas", clase: "etapa-alerta" },
  FONDO_EMERGENCIA: { etiqueta: "Paso 2 · Fondo de emergencia", clase: "etapa-aviso" },
  INVERTIR: { etiqueta: "Paso 3 · Invertir", clase: "etapa-ok" },
};

const RIESGOS = {
  BAJO: { etiqueta: "Riesgo bajo", clase: "riesgo-bajo" },
  MEDIO: { etiqueta: "Riesgo medio", clase: "riesgo-medio" },
  ALTO: { etiqueta: "Riesgo alto", clase: "riesgo-alto" },
  VARIABLE: { etiqueta: "Riesgo según el fondo", clase: "riesgo-variable" },
};

function hoy() {
  const d = new Date();
  return { mes: d.getMonth() + 1, anio: d.getFullYear() };
}

function leerPrefs() {
  try {
    const p = JSON.parse(localStorage.getItem(CLAVE_PREFS) || "{}");
    return {
      perfil: PERFILES.some((x) => x.valor === p.perfil) ? p.perfil : "MODERADO",
      horizonteMeses: HORIZONTES.some((x) => x.meses === p.horizonteMeses) ? p.horizonteMeses : 60,
      fondo: p.fondo ?? "",
    };
  } catch {
    return { perfil: "MODERADO", horizonteMeses: 60, fondo: "" };
  }
}

export default function Inversion() {
  const navigate = useNavigate();
  const usuario = JSON.parse(localStorage.getItem("fb_user") || "{}");
  const { mes, anio } = hoy();

  const [prefs, setPrefs] = useState(leerPrefs);
  const [plan, setPlan] = useState(null);
  const [error, setError] = useState("");
  const [cargando, setCargando] = useState(true);

  async function cargarPlan(p = prefs) {
    setError("");
    try {
      const resultado = await api.obtenerPlanInversion(
        usuario.id,
        mes,
        anio,
        p.perfil,
        p.horizonteMeses,
        Number(p.fondo) || 0
      );
      setPlan(resultado);
    } catch (err) {
      setError(err.message || "No se pudo calcular la recomendación de inversión");
    } finally {
      setCargando(false);
    }
  }

  useEffect(() => {
    if (!usuario.id) {
      navigate("/login");
      return;
    }
    cargarPlan();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  function enviar(e) {
    e.preventDefault();
    localStorage.setItem(CLAVE_PREFS, JSON.stringify(prefs));
    cargarPlan();
  }

  if (cargando) {
    return (
      <div className="pantalla-centrada">
        <p>Calculando tu plan de inversión...</p>
      </div>
    );
  }

  const etapa = plan ? ETAPAS[plan.etapa] || ETAPAS.SIN_DATOS : null;
  const progresoFondo =
    plan && plan.metaFondoEmergencia > 0
      ? Math.min(100, (plan.fondoEmergenciaActual / plan.metaFondoEmergencia) * 100)
      : 0;
  const opcionesConMonto = plan ? plan.opciones.filter((o) => o.porcentaje > 0) : [];
  const opcionesInformativas = plan ? plan.opciones.filter((o) => o.porcentaje === 0) : [];

  return (
    <>
      <BarraNavegacion paginaActiva="inversion" />
      <div className="contenedor-dashboard">
        <header className="encabezado-dashboard">
          <h1>Inversión</h1>
          <p className="subtitulo">Dónde conviene poner tu excedente, según tu situación</p>
        </header>

        {error && <div className="mensaje-error">{error}</div>}

        <form className="tarjeta inv-formulario" onSubmit={enviar}>
          <h2>Tu perfil de inversión</h2>

          <fieldset className="inv-perfiles">
            <legend className="solo-lectores">Perfil de riesgo</legend>
            {PERFILES.map((p) => (
              <label
                key={p.valor}
                className={`inv-perfil${prefs.perfil === p.valor ? " seleccionado" : ""}`}
              >
                <input
                  type="radio"
                  name="perfil"
                  value={p.valor}
                  checked={prefs.perfil === p.valor}
                  onChange={() => setPrefs({ ...prefs, perfil: p.valor })}
                />
                <strong>{p.nombre}</strong>
                <span>{p.detalle}</span>
              </label>
            ))}
          </fieldset>

          <div className="inv-campos">
            <div className="campo">
              <label htmlFor="horizonte">¿En cuánto tiempo podrías necesitar este dinero?</label>
              <select
                id="horizonte"
                value={prefs.horizonteMeses}
                onChange={(e) => setPrefs({ ...prefs, horizonteMeses: Number(e.target.value) })}
              >
                {HORIZONTES.map((h) => (
                  <option key={h.meses} value={h.meses}>
                    {h.etiqueta}
                  </option>
                ))}
              </select>
            </div>
            <div className="campo">
              <label htmlFor="fondo">Fondo de emergencia que ya tienes ($)</label>
              <input
                id="fondo"
                type="number"
                min="0"
                inputMode="numeric"
                placeholder="0"
                value={prefs.fondo}
                onChange={(e) => setPrefs({ ...prefs, fondo: e.target.value })}
              />
            </div>
          </div>

          <button className="boton-primario" type="submit">
            Ver recomendación
          </button>
        </form>

        {plan && (
          <>
            <section className="tarjeta tarjeta-estrategia">
              <span className="etiqueta-estrategia">{etapa.etiqueta}</span>
              <h2 className="inv-titulo-plan">{plan.titulo}</h2>
              <p className="texto-justificacion">{plan.mensaje}</p>

              {plan.excedenteMensual > 0 && (
                <p className="monto-disponible">
                  Excedente libre al mes (después de gastos y cuotas mínimas):{" "}
                  <strong>{formatoCLP(plan.excedenteMensual)}</strong>
                </p>
              )}

              {plan.metaFondoEmergencia > 0 && plan.etapa !== "SIN_DATOS" && (
                <div className="inv-fondo">
                  <div className="inv-fondo-texto">
                    <span>Fondo de emergencia</span>
                    <span>
                      {formatoCLP(plan.fondoEmergenciaActual)} de {formatoCLP(plan.metaFondoEmergencia)}
                    </span>
                  </div>
                  <div
                    className="pista inv-pista-fondo"
                    role="img"
                    aria-label={`Fondo de emergencia al ${Math.round(progresoFondo)}% de la meta`}
                  >
                    <span style={{ width: `${progresoFondo}%` }} />
                  </div>
                </div>
              )}
            </section>

            {opcionesConMonto.length > 0 && (
              <section className="inv-seccion">
                <h2 className="titulo-seccion">
                  {plan.etapa === "FONDO_EMERGENCIA" ? "Dónde guardar tu fondo" : "Distribución de referencia"}
                </h2>
                <ul className="inv-lista">
                  {opcionesConMonto.map((o) => (
                    <OpcionTarjeta key={o.clave} opcion={o} />
                  ))}
                </ul>
              </section>
            )}

            {opcionesInformativas.length > 0 && (
              <section className="inv-seccion">
                <h2 className="titulo-seccion">Para el largo plazo</h2>
                <ul className="inv-lista">
                  {opcionesInformativas.map((o) => (
                    <OpcionTarjeta key={o.clave} opcion={o} />
                  ))}
                </ul>
              </section>
            )}

            {plan.proyecciones.length > 0 && (
              <section className="tarjeta inv-proyeccion">
                <h2>Proyección ilustrativa</h2>
                <p className="inv-nota">
                  Si inviertes {formatoCLP(plan.excedenteMensual)} al mes con una rentabilidad supuesta de{" "}
                  {String(plan.rentabilidadSupuestaAnual).replace(".", ",")}% anual (±2 puntos para los
                  escenarios bajo y alto). Es un supuesto, no una garantía, y no descuenta inflación ni impuestos.
                </p>
                <div className="inv-tabla-scroll">
                  <table className="tabla-proyeccion">
                    <thead>
                      <tr>
                        <th>Plazo</th>
                        <th>Aportado</th>
                        <th>Escenario bajo</th>
                        <th>Escenario base</th>
                        <th>Escenario alto</th>
                      </tr>
                    </thead>
                    <tbody>
                      {plan.proyecciones.map((p) => (
                        <tr key={p.anios}>
                          <td>{p.anios === 1 ? "1 año" : `${p.anios} años`}</td>
                          <td>{formatoCLP(p.aportado)}</td>
                          <td>{formatoCLP(p.bajo)}</td>
                          <td className="celda-base">{formatoCLP(p.base)}</td>
                          <td>{formatoCLP(p.alto)}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </section>
            )}

            <p className="inv-aviso">{plan.aviso}</p>
          </>
        )}
      </div>
    </>
  );
}

function OpcionTarjeta({ opcion: o }) {
  const riesgo = RIESGOS[o.riesgo] || RIESGOS.VARIABLE;
  return (
    <li className="tarjeta inv-opcion">
      <div className="inv-opcion-cabecera">
        <h3>{o.nombre}</h3>
        <span className={`inv-chip-riesgo ${riesgo.clase}`}>{riesgo.etiqueta}</span>
      </div>

      {o.porcentaje > 0 && (
        <div className="inv-asignacion">
          <div className="inv-asignacion-texto">
            <strong>{o.porcentaje}%</strong>
            <span>{formatoCLP(o.montoMensual)} al mes</span>
          </div>
          <div
            className="pista inv-pista"
            role="img"
            aria-label={`${o.porcentaje}% de tu excedente`}
          >
            <span style={{ width: `${o.porcentaje}%` }} />
          </div>
        </div>
      )}

      <dl className="inv-detalle">
        <div>
          <dt>Sirve para</dt>
          <dd>{o.paraQue}</dd>
        </div>
        <div>
          <dt>Liquidez</dt>
          <dd>{o.liquidez}</dd>
        </div>
        <div>
          <dt>Ten en cuenta</dt>
          <dd>{o.considera}</dd>
        </div>
        <div>
          <dt>Dónde se contrata</dt>
          <dd>{o.dondeSeContrata}</dd>
        </div>
      </dl>
    </li>
  );
}
