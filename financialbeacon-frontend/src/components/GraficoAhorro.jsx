import { useEffect, useRef, useState } from "react";
import { api } from "../api/client.js";
import { formatoCLP } from "../utils/formato.js";
import "./GraficoAhorro.css";

const MESES = ["ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic"];

const RANGOS = [
  { clave: "S", etiqueta: "Semana", tipo: "dia", cantidad: 7, titulo: "últimos 7 días" },
  { clave: "M", etiqueta: "Mes", tipo: "dia", cantidad: 30, titulo: "últimos 30 días" },
  { clave: "6M", etiqueta: "Semestre", tipo: "mes", cantidad: 6, titulo: "últimos 6 meses" },
  { clave: "A", etiqueta: "Año", tipo: "mes", cantidad: 12, titulo: "últimos 12 meses" },
];

const claveMes = (anio, mes) => `${anio}-${mes}`;
const diasEnMes = (anio, mes) => new Date(anio, mes, 0).getDate();

function aISO(d) {
  const dos = (n) => String(n).padStart(2, "0");
  return `${d.getFullYear()}-${dos(d.getMonth() + 1)}-${dos(d.getDate())}`;
}

function conSigno(valor) {
  if (valor === 0) return formatoCLP(0);
  return `${valor < 0 ? "−" : "+"}${formatoCLP(Math.abs(valor))}`;
}

// Último día que cubre el gráfico: hoy si miras el mes actual; si no, el fin del mes que miras.
function calcularFin(mes, anio, esMesActual) {
  if (esMesActual) {
    const h = new Date();
    return new Date(h.getFullYear(), h.getMonth(), h.getDate());
  }
  return new Date(anio, mes, 0);
}

function mesesNecesarios(rango, fin) {
  const vistos = new Map();
  for (let k = 0; k < rango.cantidad; k++) {
    const d =
      rango.tipo === "dia"
        ? new Date(fin.getFullYear(), fin.getMonth(), fin.getDate() - k)
        : new Date(fin.getFullYear(), fin.getMonth() - k, 1);
    const anio = d.getFullYear();
    const mes = d.getMonth() + 1;
    vistos.set(claveMes(anio, mes), { anio, mes });
  }
  return [...vistos.values()];
}

// Ahorro acumulado del período, partiendo de $0.
// - Semana y mes (por día): el sueldo del mes se reparte en partes iguales por día y se restan los gastos reales.
// - Semestre y año (por mes): se suma el ahorro estimado de cada mes (sueldo − gastos).
function construirSerie(rango, fin, resumenes, gastosMes) {
  const puntos = [];
  let acumulado = 0;

  if (rango.tipo === "dia") {
    const porDia = new Map();
    for (const lista of gastosMes.values()) {
      for (const g of lista) porDia.set(g.fecha, (porDia.get(g.fecha) || 0) + Number(g.monto || 0));
    }
    const inicio = new Date(fin.getFullYear(), fin.getMonth(), fin.getDate() - (rango.cantidad - 1));
    puntos.push({ valor: 0, etiqueta: "Inicio", texto: "Punto de partida" });
    for (let k = 0; k < rango.cantidad; k++) {
      const d = new Date(inicio.getFullYear(), inicio.getMonth(), inicio.getDate() + k);
      const r = resumenes.get(claveMes(d.getFullYear(), d.getMonth() + 1));
      const ingresoDia = Number(r?.ingresoMensual || 0) / diasEnMes(d.getFullYear(), d.getMonth() + 1);
      acumulado += ingresoDia - (porDia.get(aISO(d)) || 0);
      const etiqueta = `${d.getDate()} ${MESES[d.getMonth()]}`;
      puntos.push({ valor: Math.round(acumulado), etiqueta, texto: `Al ${etiqueta}` });
    }
  } else {
    puntos.push({ valor: 0, etiqueta: "Inicio", texto: "Punto de partida" });
    for (let k = rango.cantidad - 1; k >= 0; k--) {
      const d = new Date(fin.getFullYear(), fin.getMonth() - k, 1);
      const r = resumenes.get(claveMes(d.getFullYear(), d.getMonth() + 1));
      acumulado += Number(r?.ahorroEstimado || 0);
      const etiqueta = rango.cantidad > 6 ? `${MESES[d.getMonth()]} ${d.getFullYear()}` : MESES[d.getMonth()];
      puntos.push({ valor: Math.round(acumulado), etiqueta, texto: `Hasta ${etiqueta}` });
    }
  }
  return puntos;
}

// Convierte los valores a coordenadas dentro de un recuadro de 100 x 100 (x: izquierda a derecha, y: arriba a abajo).
function geometria(puntos) {
  const valores = puntos.map((p) => p.valor);
  const min = Math.min(0, ...valores);
  let max = Math.max(0, ...valores);
  if (max === min) max = min + 1;
  const span = max - min;
  const bajo = min - span * 0.08;
  const alto = max + span * 0.12;
  const n = puntos.length;
  const coords = puntos.map((p, i) => ({
    x: n === 1 ? 50 : (i / (n - 1)) * 100,
    y: 100 - ((p.valor - bajo) / (alto - bajo)) * 100,
  }));
  const linea = coords.map((c, i) => `${i === 0 ? "M" : "L"}${c.x.toFixed(2)} ${c.y.toFixed(2)}`).join(" ");
  const area = `${linea} L100 100 L0 100 Z`;
  const yCero = 100 - ((0 - bajo) / (alto - bajo)) * 100;
  return { coords, linea, area, yCero, cruzaCero: min < 0 && max > 0 };
}

export default function GraficoAhorro({ mes, anio, esMesActual, claveDatos }) {
  const [rangoClave, setRangoClave] = useState("S");
  const [serie, setSerie] = useState(null);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState("");
  const [intento, setIntento] = useState(0);
  const [seleccion, setSeleccion] = useState(null);
  const resumenes = useRef(new Map());
  const gastosMes = useRef(new Map());
  const ultimaClave = useRef(claveDatos);
  const lienzoRef = useRef(null);

  const rango = RANGOS.find((r) => r.clave === rangoClave);

  useEffect(() => {
    // Si el usuario registró o borró algo, los datos guardados ya no sirven.
    if (ultimaClave.current !== claveDatos) {
      ultimaClave.current = claveDatos;
      resumenes.current.clear();
      gastosMes.current.clear();
    }

    let vigente = true;
    setCargando(true);
    setError("");
    const fin = calcularFin(mes, anio, esMesActual);

    const tareas = [];
    for (const { anio: a, mes: m } of mesesNecesarios(rango, fin)) {
      const clave = claveMes(a, m);
      if (!resumenes.current.has(clave)) {
        tareas.push(api.obtenerResumen(null, m, a).then((r) => resumenes.current.set(clave, r)));
      }
      if (rango.tipo === "dia" && !gastosMes.current.has(clave)) {
        tareas.push(api.listarGastos(null, m, a).then((g) => gastosMes.current.set(clave, g)));
      }
    }

    Promise.all(tareas)
      .then(() => {
        if (!vigente) return;
        setSerie({ rango: rango.clave, puntos: construirSerie(rango, fin, resumenes.current, gastosMes.current) });
        setSeleccion(null);
      })
      .catch((err) => {
        if (vigente) setError(err.message || "No se pudo cargar el gráfico");
      })
      .finally(() => {
        if (vigente) setCargando(false);
      });

    return () => {
      vigente = false;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [rangoClave, claveDatos, mes, anio, esMesActual, intento]);

  const listo = serie && serie.rango === rangoClave;
  const puntos = listo ? serie.puntos : [];
  const n = puntos.length;
  const indice = seleccion ?? n - 1;
  const actual = listo ? puntos[indice] : null;
  const sinDatos = listo && puntos.every((p) => p.valor === 0);
  const geo = listo && !sinDatos ? geometria(puntos) : null;

  function elegirDesdePuntero(e) {
    if (!lienzoRef.current || n < 2) return;
    const caja = lienzoRef.current.getBoundingClientRect();
    const r = Math.min(1, Math.max(0, (e.clientX - caja.left) / caja.width));
    setSeleccion(Math.round(r * (n - 1)));
  }

  function alPulsarTecla(e) {
    const pasos = { ArrowLeft: -1, ArrowRight: 1 };
    if (e.key in pasos) {
      e.preventDefault();
      setSeleccion(Math.min(n - 1, Math.max(0, indice + pasos[e.key])));
    } else if (e.key === "Home") {
      e.preventDefault();
      setSeleccion(0);
    } else if (e.key === "End") {
      e.preventDefault();
      setSeleccion(null);
    }
  }

  return (
    <section className="tarjeta grafico-ahorro" aria-labelledby="titulo-grafico-ahorro">
      <h2 id="titulo-grafico-ahorro">Evolución de tu ahorro</h2>
      <p className="ga-sub">Ahorro acumulado · {rango.titulo}</p>

      {error ? (
        <div className="ga-vacio">
          <p>{error}</p>
          <button type="button" className="boton-secundario" onClick={() => setIntento((i) => i + 1)}>
            Reintentar
          </button>
        </div>
      ) : !listo ? (
        <div className="ga-vacio" aria-busy={cargando}>
          <p>Cargando tu gráfico…</p>
        </div>
      ) : sinDatos ? (
        <div className="ga-vacio">
          <p>
            Todavía no hay datos en este período. Registra tu sueldo y tus gastos para ver cómo crece tu ahorro.
          </p>
        </div>
      ) : (
        <>
          <div className={`ga-valor ${actual.valor < 0 ? "negativo" : "positivo"}`}>{conSigno(actual.valor)}</div>
          <p className="ga-fecha">{actual.texto}</p>

          <div
            className="ga-lienzo"
            ref={lienzoRef}
            role="slider"
            tabIndex={0}
            aria-label="Ahorro acumulado. Usa las flechas para recorrer el período"
            aria-valuemin={0}
            aria-valuemax={n - 1}
            aria-valuenow={indice}
            aria-valuetext={`${actual.texto}: ${conSigno(actual.valor)}`}
            onPointerDown={elegirDesdePuntero}
            onPointerMove={(e) => {
              if (e.pointerType === "mouse" || e.buttons) elegirDesdePuntero(e);
            }}
            onPointerLeave={(e) => {
              if (e.pointerType === "mouse") setSeleccion(null);
            }}
            onKeyDown={alPulsarTecla}
          >
            <svg viewBox="0 0 100 100" preserveAspectRatio="none" aria-hidden="true">
              <defs>
                <linearGradient id="ga-degradado" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="0%" style={{ stopColor: "var(--grafico-linea)", stopOpacity: 0.28 }} />
                  <stop offset="100%" style={{ stopColor: "var(--grafico-linea)", stopOpacity: 0.02 }} />
                </linearGradient>
              </defs>
              {geo.cruzaCero && <line className="ga-cero" x1="0" x2="100" y1={geo.yCero} y2={geo.yCero} />}
              <path className="ga-area" d={geo.area} />
              <path className="ga-linea" d={geo.linea} />
            </svg>
            <div className="ga-guia" style={{ left: `${geo.coords[indice].x}%` }} />
            <div className="ga-punto" style={{ left: `${geo.coords[indice].x}%`, top: `${geo.coords[indice].y}%` }} />
          </div>

          <div className="ga-ejes" aria-hidden="true">
            <span>{puntos[1]?.etiqueta}</span>
            <span>{puntos[n - 1]?.etiqueta}</span>
          </div>
        </>
      )}

      <div className="ga-rangos" role="group" aria-label="Período del gráfico">
        {RANGOS.map((r) => (
          <button
            key={r.clave}
            type="button"
            className={`ga-rango${r.clave === rangoClave ? " activo" : ""}`}
            aria-pressed={r.clave === rangoClave}
            onClick={() => setRangoClave(r.clave)}
          >
            {r.etiqueta}
          </button>
        ))}
      </div>

      {listo && !sinDatos && (
        <p className="ga-nota">
          {rango.tipo === "dia"
            ? "Es un estimado: tu sueldo del mes se reparte por día y se restan tus gastos reales."
            : "Suma el ahorro estimado de cada mes (sueldo menos gastos). El mes actual cuenta tu sueldo completo."}
        </p>
      )}
    </section>
  );
}
