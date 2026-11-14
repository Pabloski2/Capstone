import { useEffect, useRef, useState } from "react";
import { formatoCLP } from "../utils/formato.js";

const CLAVE_ELEGIDAS = "fb_inversion_elegidas";
const MAX_COMPARAR = 4;

// Catálogo de referencia (Chile). No incluye tasas numéricas a propósito: cambian cada semana.
// nivel: 1 bajo, 2 medio, 3 alto. minMeses: plazo mínimo recomendado. liquida: se retira rápido.
const INSTRUMENTOS = [
  {
    clave: "CUENTA_REMUNERADA",
    nombre: "Cuenta de ahorro o remunerada",
    corto: "Tu plata siempre disponible, con poco rendimiento.",
    riesgo: "BAJO",
    nivel: 1,
    minMeses: 0,
    liquida: true,
    rentabilidad: "Baja",
    plazo: "Cualquiera",
    minimo: "Muy bajo o sin mínimo",
    liquidez: "Inmediata",
    donde: "Bancos y fintech",
  },
  {
    clave: "FM_MONEY_MARKET",
    nombre: "Fondo mutuo de corto plazo",
    corto: "Poco riesgo y puedes retirar en pocos días.",
    riesgo: "BAJO",
    nivel: 1,
    minMeses: 3,
    liquida: true,
    rentabilidad: "Baja a media",
    plazo: "Desde unos meses",
    minimo: "Bajo (varía por administradora)",
    liquidez: "En pocos días hábiles",
    donde: "Bancos, corredoras y apps de inversión",
  },
  {
    clave: "DEPOSITO_PLAZO",
    nombre: "Depósito a plazo",
    corto: "Sabes cuánto vas a ganar desde el primer día.",
    riesgo: "BAJO",
    nivel: 1,
    minMeses: 1,
    liquida: false,
    rentabilidad: "Media y fija",
    plazo: "Desde 30 días; en UF, plazos más largos",
    minimo: "Varía por banco",
    liquidez: "Al vencimiento (antes tiene costo)",
    donde: "Bancos",
  },
  {
    clave: "BONOS_RENTA_FIJA",
    nombre: "Bonos y fondos de deuda",
    corto: "Rinden más que un depósito, con algo de vaivén.",
    riesgo: "MEDIO",
    nivel: 2,
    minMeses: 24,
    liquida: false,
    rentabilidad: "Media",
    plazo: "2 a 5 años",
    minimo: "Bajo en fondos; mayor en bonos directos",
    liquidez: "En días; el valor puede oscilar",
    donde: "Corredoras, bancos y administradoras",
  },
  {
    clave: "FM_BALANCEADO",
    nombre: "Fondo mutuo balanceado",
    corto: "Mezcla renta fija y acciones para crecer con calma.",
    riesgo: "MEDIO",
    nivel: 2,
    minMeses: 36,
    liquida: false,
    rentabilidad: "Media a alta",
    plazo: "3 años o más",
    minimo: "Bajo (varía por administradora)",
    liquidez: "En pocos días hábiles",
    donde: "Bancos, corredoras y apps de inversión",
  },
  {
    clave: "APV",
    nombre: "APV (ahorro para tu pensión)",
    corto: "Ahorras para tu pensión y tienes beneficio tributario.",
    riesgo: "VARIABLE",
    nivel: 2,
    minMeses: 60,
    liquida: false,
    rentabilidad: "Según el fondo que elijas, más beneficio tributario",
    plazo: "Largo plazo (pensión)",
    minimo: "Bajo",
    liquidez: "Retirar antes puede tener costo tributario",
    donde: "AFP, bancos, administradoras y seguros",
    riesgoNota: true,
  },
  {
    clave: "FM_ACCIONARIO_ETF",
    nombre: "Fondo accionario o ETF",
    corto: "Inviertes en muchas empresas a la vez; sube y baja.",
    riesgo: "ALTO",
    nivel: 3,
    minMeses: 60,
    liquida: false,
    rentabilidad: "Alta, pero muy variable",
    plazo: "5 años o más",
    minimo: "Bajo a medio",
    liquidez: "Rescate o venta en días hábiles",
    donde: "Corredoras de bolsa y administradoras",
  },
  {
    clave: "ACCIONES",
    nombre: "Acciones de empresas",
    corto: "Compras partes de una empresa; mucho vaivén.",
    riesgo: "ALTO",
    nivel: 3,
    minMeses: 60,
    liquida: false,
    rentabilidad: "Alta, pero muy variable",
    plazo: "5 años o más",
    minimo: "El precio de una acción",
    liquidez: "Venta en días hábiles",
    donde: "Corredoras de bolsa",
  },
  {
    clave: "FONDOS_INMOBILIARIOS",
    nombre: "Fondos inmobiliarios",
    corto: "Ganas por arriendos sin comprar una propiedad.",
    riesgo: "ALTO",
    nivel: 3,
    minMeses: 60,
    liquida: false,
    rentabilidad: "Media a alta, con rentas periódicas",
    plazo: "5 años o más",
    minimo: "Medio",
    liquidez: "Se venden en bolsa, a veces con poco movimiento",
    donde: "Corredoras de bolsa y administradoras",
  },
  {
    clave: "CRIPTO",
    nombre: "Criptomonedas",
    corto: "Muy especulativo: puede subir mucho o perderse casi todo.",
    riesgo: "ALTO",
    nivel: 3,
    minMeses: 60,
    liquida: false,
    rentabilidad: "Imposible de anticipar",
    plazo: "Solo dinero que puedas perder",
    minimo: "Muy bajo",
    liquidez: "Alta, pero con precios muy cambiantes",
    donde: "Plataformas de intercambio (revisa su reputación)",
    nota: "Puedes perder casi todo lo invertido: usa solo dinero que no necesites.",
  },
];

const RIESGOS = {
  BAJO: { etiqueta: "Riesgo bajo", clase: "riesgo-bajo" },
  MEDIO: { etiqueta: "Riesgo medio", clase: "riesgo-medio" },
  ALTO: { etiqueta: "Riesgo alto", clase: "riesgo-alto" },
  VARIABLE: { etiqueta: "Riesgo según el fondo", clase: "riesgo-variable" },
};

const FILTROS = [
  { valor: "TODOS", etiqueta: "Todos" },
  { valor: "BAJO", etiqueta: "Bajo" },
  { valor: "MEDIO", etiqueta: "Medio" },
  { valor: "ALTO", etiqueta: "Alto" },
];

const PERFIL_MAX_NIVEL = { CONSERVADOR: 1, MODERADO: 2, AGRESIVO: 3 };
const PERFIL_NOMBRE = { CONSERVADOR: "conservador", MODERADO: "moderado", AGRESIVO: "agresivo" };

const AVISO_ETAPA = {
  PAGAR_DEUDA: "Tu prioridad hoy es pagar tus deudas. Estas opciones quedan para después.",
  SIN_EXCEDENTE: "Hoy no te queda dinero libre al mes. Revisa tus gastos antes de invertir.",
  FONDO_EMERGENCIA: "Tu prioridad hoy es armar un fondo de emergencia, por eso se prefieren las opciones de retiro rápido.",
  SIN_DATOS: "Aún faltan datos de tus ingresos y gastos: esta comparación usa solo tu perfil y tu plazo.",
};

const FIT = {
  encaja: { etiqueta: "Encaja contigo", icono: "✓" },
  parcial: { etiqueta: "Encaja en parte", icono: "!" },
  no: { etiqueta: "No por ahora", icono: "✕" },
};

const ICONO_MOTIVO = { bueno: "✓", aviso: "!", malo: "✕" };
const ORDEN_FIT = { encaja: 0, parcial: 1, no: 2 };
const ORDEN_MOTIVO = { malo: 0, aviso: 1, bueno: 2 };

function etiquetaPlazo(meses) {
  if (meses < 12) return `${meses} ${meses === 1 ? "mes" : "meses"}`;
  if (meses % 12 === 0) {
    const anios = meses / 12;
    return `${anios} ${anios === 1 ? "año" : "años"}`;
  }
  return `${meses} meses`;
}

// Cruza un instrumento con la situación del usuario (perfil, plazo y etapa financiera).
function evaluar(i, { perfil, horizonteMeses, etapa }) {
  const motivos = [];
  const maxNivel = PERFIL_MAX_NIVEL[perfil] ?? 2;

  if (i.nivel > maxNivel) {
    motivos.push({ tono: "malo", texto: `Tiene más riesgo del que aceptas con tu perfil ${PERFIL_NOMBRE[perfil] ?? "moderado"}.` });
  } else {
    motivos.push({
      tono: "bueno",
      texto: i.riesgoNota ? "El riesgo depende del fondo que elijas: puedes ajustarlo a tu perfil." : "Su riesgo va con tu perfil.",
    });
  }

  if (horizonteMeses >= i.minMeses) {
    motivos.push({ tono: "bueno", texto: `Tu plazo (${etiquetaPlazo(horizonteMeses)}) alcanza.` });
  } else {
    motivos.push({
      tono: i.nivel >= 3 ? "malo" : "aviso",
      texto: `Conviene dejarla al menos ${etiquetaPlazo(i.minMeses)} y tú podrías necesitar el dinero antes.`,
    });
  }

  if (etapa === "PAGAR_DEUDA") {
    motivos.push({ tono: "aviso", texto: "Primero conviene pagar tus deudas." });
  } else if (etapa === "SIN_EXCEDENTE") {
    motivos.push({ tono: "aviso", texto: "Hoy no te sobra dinero al mes para invertir." });
  } else if (etapa === "FONDO_EMERGENCIA") {
    motivos.push(
      i.liquida
        ? { tono: "bueno", texto: "Se retira rápido: ideal para tu fondo de emergencia." }
        : { tono: "aviso", texto: "Primero arma tu fondo de emergencia; esta opción no es de retiro rápido." }
    );
  }

  if (i.nota) motivos.push({ tono: "aviso", texto: i.nota });

  motivos.sort((a, b) => ORDEN_MOTIVO[a.tono] - ORDEN_MOTIVO[b.tono]);
  const nivel = motivos.some((m) => m.tono === "malo") ? "no" : motivos.some((m) => m.tono === "aviso") ? "parcial" : "encaja";
  return { instrumento: i, nivel, motivos };
}

function leerElegidas() {
  try {
    const guardadas = JSON.parse(localStorage.getItem(CLAVE_ELEGIDAS) || "[]");
    return Array.isArray(guardadas)
      ? guardadas.filter((c) => INSTRUMENTOS.some((i) => i.clave === c)).slice(0, MAX_COMPARAR)
      : [];
  } catch {
    return [];
  }
}

export default function TablaInstrumentos({ perfil, horizonteMeses, etapa = "SIN_DATOS", excedente = 0 }) {
  const [filtro, setFiltro] = useState("TODOS");
  const [elegidas, setElegidas] = useState(leerElegidas);
  const [vista, setVista] = useState("elegir");
  const seccionRef = useRef(null);
  const tituloRef = useRef(null);

  const perfilNorm = String(perfil || "MODERADO").toUpperCase();
  const situacion = { perfil: perfilNorm, horizonteMeses: Number(horizonteMeses) || 60, etapa };

  const seleccion = INSTRUMENTOS.filter((i) => elegidas.includes(i.clave));
  const enComparacion = vista === "comparar" && seleccion.length > 0;
  const lleno = elegidas.length >= MAX_COMPARAR;
  const visibles = INSTRUMENTOS.filter((i) => filtro === "TODOS" || i.riesgo === filtro);

  // Al pasar de la lista a la comparación (y al volver), lleva al usuario al inicio de la sección.
  const vistaAnterior = useRef(enComparacion);
  useEffect(() => {
    if (vistaAnterior.current === enComparacion) return;
    vistaAnterior.current = enComparacion;
    seccionRef.current?.scrollIntoView({ block: "start", behavior: "smooth" });
    if (enComparacion) tituloRef.current?.focus({ preventScroll: true });
  }, [enComparacion]);

  function guardar(nuevas) {
    setElegidas(nuevas);
    try {
      localStorage.setItem(CLAVE_ELEGIDAS, JSON.stringify(nuevas));
    } catch {
      // Sin almacenamiento disponible: la selección sigue funcionando en pantalla.
    }
  }

  function alternar(clave) {
    if (elegidas.includes(clave)) guardar(elegidas.filter((c) => c !== clave));
    else if (!lleno) guardar([...elegidas, clave]);
  }

  if (enComparacion) {
    const resultados = seleccion
      .map((i) => evaluar(i, situacion))
      .map((r, idx) => ({ ...r, idx }))
      .sort((a, b) => ORDEN_FIT[a.nivel] - ORDEN_FIT[b.nivel] || a.idx - b.idx);
    const mejor = resultados[0];
    const hayEncaja = mejor.nivel === "encaja";

    return (
      <section className="tarjeta inv-comparador" ref={seccionRef} aria-labelledby="titulo-comparador">
        <button type="button" className="inv-volver" onClick={() => setVista("elegir")}>
          ← Volver a elegir
        </button>
        <h2 id="titulo-comparador" tabIndex={-1} ref={tituloRef}>
          Tu comparación
        </h2>

        <ul className="inv-situacion" aria-label="Tu situación">
          <li>
            Perfil: <strong>{PERFIL_NOMBRE[perfilNorm] ?? "moderado"}</strong>
          </li>
          <li>
            Plazo: <strong>{etiquetaPlazo(situacion.horizonteMeses)}</strong>
          </li>
          {excedente > 0 && (
            <li>
              Te sobran: <strong>{formatoCLP(excedente)}/mes</strong>
            </li>
          )}
        </ul>

        {AVISO_ETAPA[etapa] && <p className="inv-banner">{AVISO_ETAPA[etapa]}</p>}

        <p className={`inv-mejor${hayEncaja ? "" : " sin-encaje"}`}>
          {hayEncaja ? (
            <>
              <strong>La que mejor va contigo:</strong> {mejor.instrumento.nombre}
            </>
          ) : mejor.nivel === "parcial" ? (
            <>
              <strong>Ninguna encaja del todo hoy.</strong> La más cercana: {mejor.instrumento.nombre}
            </>
          ) : (
            <>
              <strong>Ninguna de las que marcaste va con tu situación hoy.</strong> Mira los motivos abajo.
            </>
          )}
        </p>

        <ul className="inv-resultados">
          {resultados.map(({ instrumento: i, nivel, motivos }) => {
            const fit = FIT[nivel];
            return (
              <li key={i.clave} className={`inv-resultado ${nivel}`}>
                <h3>{i.nombre}</h3>
                <span className={`inv-fit ${nivel}`}>
                  <span aria-hidden="true">{fit.icono}</span> {fit.etiqueta}
                </span>
                <ul className="inv-motivos">
                  {motivos.map((m) => (
                    <li key={m.texto}>
                      <span className={`inv-motivo-icono ${m.tono}`} aria-hidden="true">
                        {ICONO_MOTIVO[m.tono]}
                      </span>
                      <span>{m.texto}</span>
                    </li>
                  ))}
                </ul>
                <p className="inv-resultado-datos">
                  <span>
                    <strong>Rentabilidad:</strong> {i.rentabilidad}
                  </span>
                  <span>
                    <strong>Retiro:</strong> {i.liquidez}
                  </span>
                </p>
                <button type="button" className="inv-quitar" onClick={() => alternar(i.clave)}>
                  Quitar de la comparación
                </button>
              </li>
            );
          })}
        </ul>

        <p className="inv-aviso">
          Es una guía de referencia, no asesoría financiera. Confirma rentabilidad, comisiones y montos mínimos
          vigentes con la institución antes de invertir.
        </p>
      </section>
    );
  }

  return (
    <section className="tarjeta inv-comparador" ref={seccionRef} aria-labelledby="titulo-comparador">
      <h2 id="titulo-comparador">¿Dónde puedes invertir?</h2>
      <p className="inv-ayuda">
        Toca <strong>Agregar a comparar</strong> en las que te interesen (hasta {MAX_COMPARAR}) y te mostramos cuáles
        van con tu situación.
      </p>

      <div className="inv-filtros" role="group" aria-label="Filtrar por riesgo">
        {FILTROS.map((f) => (
          <button
            key={f.valor}
            type="button"
            className={`inv-filtro${filtro === f.valor ? " activo" : ""}`}
            aria-pressed={filtro === f.valor}
            onClick={() => setFiltro(f.valor)}
          >
            {f.etiqueta}
          </button>
        ))}
      </div>

      <ul className="inv-cards">
        {visibles.map((i) => {
          const riesgo = RIESGOS[i.riesgo];
          const marcada = elegidas.includes(i.clave);
          return (
            <li key={i.clave} className={`inv-card${marcada ? " marcada" : ""}`}>
              <div className="inv-card-top">
                <h3>{i.nombre}</h3>
                <span className={`inv-chip-riesgo ${riesgo.clase}`}>{riesgo.etiqueta}</span>
              </div>
              <p className="inv-card-corto">{i.corto}</p>

              <details className="inv-card-mas">
                <summary>Ver detalles</summary>
                <dl className="inv-datos">
                  <div>
                    <dt>Rentabilidad</dt>
                    <dd>{i.rentabilidad}</dd>
                  </div>
                  <div>
                    <dt>Plazo sugerido</dt>
                    <dd>{i.plazo}</dd>
                  </div>
                  <div>
                    <dt>Monto mínimo</dt>
                    <dd>{i.minimo}</dd>
                  </div>
                  <div>
                    <dt>Retiro</dt>
                    <dd>{i.liquidez}</dd>
                  </div>
                  <div>
                    <dt>Dónde se contrata</dt>
                    <dd>{i.donde}</dd>
                  </div>
                </dl>
              </details>

              <button
                type="button"
                className={`inv-card-boton${marcada ? " activo" : ""}`}
                aria-pressed={marcada}
                disabled={!marcada && lleno}
                onClick={() => alternar(i.clave)}
              >
                {marcada ? "✓ En mi comparación" : "Agregar a comparar"}
              </button>
            </li>
          );
        })}
      </ul>

      {lleno && (
        <p className="inv-limite" role="status">
          Llegaste al máximo de {MAX_COMPARAR}. Quita una para agregar otra.
        </p>
      )}

      {seleccion.length > 0 && (
        <div className="inv-barra-comparar">
          <div className="inv-barra-texto">
            <strong>
              {seleccion.length} de {MAX_COMPARAR}
            </strong>{" "}
            elegidas
            <button type="button" className="inv-barra-limpiar" onClick={() => guardar([])}>
              Limpiar
            </button>
          </div>
          <button type="button" className="inv-barra-ir" onClick={() => setVista("comparar")}>
            {seleccion.length === 1 ? "Ver si me conviene" : "Comparar"}
          </button>
        </div>
      )}

      <p className="inv-aviso">
        Información de referencia, no es asesoría financiera. No incluye tasas porque cambian con frecuencia.
      </p>
    </section>
  );
}
