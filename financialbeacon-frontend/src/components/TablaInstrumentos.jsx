import { useState } from "react";

const CLAVE_ELEGIDAS = "fb_inversion_elegidas";

// Catálogo de referencia (Chile). No incluye tasas numéricas a propósito: cambian cada semana.
// La rentabilidad se expresa como nivel relativo para poder comparar entre instrumentos.
const INSTRUMENTOS = [
  {
    clave: "CUENTA_REMUNERADA",
    nombre: "Cuenta de ahorro o cuenta remunerada",
    riesgo: "BAJO",
    rentabilidad: "Baja",
    plazo: "Cualquiera",
    minimo: "Muy bajo o sin mínimo",
    liquidez: "Inmediata",
    donde: "Bancos y fintech",
  },
  {
    clave: "FM_MONEY_MARKET",
    nombre: "Fondo mutuo de corto plazo (money market)",
    riesgo: "BAJO",
    rentabilidad: "Baja a media",
    plazo: "Desde unos meses",
    minimo: "Bajo (varía por administradora)",
    liquidez: "Rescate en pocos días hábiles",
    donde: "Bancos, corredoras y apps de inversión",
  },
  {
    clave: "DEPOSITO_PLAZO",
    nombre: "Depósito a plazo",
    riesgo: "BAJO",
    rentabilidad: "Media (fija, la conoces al contratar)",
    plazo: "Desde 30 días; en UF, plazos más largos",
    minimo: "Varía por banco",
    liquidez: "Al vencimiento (rescatar antes tiene costo)",
    donde: "Bancos",
  },
  {
    clave: "BONOS_RENTA_FIJA",
    nombre: "Bonos y fondos de deuda de mediano plazo",
    riesgo: "MEDIO",
    rentabilidad: "Media",
    plazo: "2 a 5 años",
    minimo: "Bajo en fondos; mayor en bonos directos",
    liquidez: "Rescate en días; el valor puede oscilar",
    donde: "Corredoras, bancos y administradoras de fondos",
  },
  {
    clave: "FM_BALANCEADO",
    nombre: "Fondo mutuo balanceado",
    riesgo: "MEDIO",
    rentabilidad: "Media a alta",
    plazo: "3 años o más",
    minimo: "Bajo (varía por administradora)",
    liquidez: "Rescate en pocos días hábiles",
    donde: "Bancos, corredoras y apps de inversión",
  },
  {
    clave: "APV",
    nombre: "APV (Ahorro Previsional Voluntario)",
    riesgo: "VARIABLE",
    rentabilidad: "Según el fondo elegido, más beneficio tributario",
    plazo: "Largo plazo (pensión)",
    minimo: "Bajo",
    liquidez: "Retirar antes puede tener costo tributario",
    donde: "AFP, bancos, administradoras de fondos y seguros",
  },
  {
    clave: "FM_ACCIONARIO_ETF",
    nombre: "Fondo accionario o ETF diversificado",
    riesgo: "ALTO",
    rentabilidad: "Alta, pero muy variable",
    plazo: "5 años o más",
    minimo: "Bajo a medio",
    liquidez: "Rescate o venta en días hábiles",
    donde: "Corredoras de bolsa y administradoras de fondos",
  },
  {
    clave: "ACCIONES",
    nombre: "Acciones de empresas (bolsa)",
    riesgo: "ALTO",
    rentabilidad: "Alta, pero muy variable",
    plazo: "5 años o más",
    minimo: "El precio de una acción",
    liquidez: "Alta (venta en días hábiles)",
    donde: "Corredoras de bolsa",
  },
  {
    clave: "FONDOS_INMOBILIARIOS",
    nombre: "Fondos de inversión inmobiliarios",
    riesgo: "ALTO",
    rentabilidad: "Media a alta, con rentas periódicas",
    plazo: "5 años o más",
    minimo: "Medio",
    liquidez: "Se venden en bolsa, pero pueden tener poco movimiento",
    donde: "Corredoras de bolsa y administradoras de fondos",
  },
  {
    clave: "CRIPTO",
    nombre: "Criptomonedas",
    riesgo: "ALTO",
    rentabilidad: "Imposible de anticipar: puede subir o perderse casi todo",
    plazo: "Solo dinero que puedas perder",
    minimo: "Muy bajo",
    liquidez: "Alta, pero con precios muy cambiantes",
    donde: "Plataformas de intercambio (revisa su seguridad y reputación)",
  },
];

const RIESGOS = {
  BAJO: { etiqueta: "Bajo", clase: "riesgo-bajo" },
  MEDIO: { etiqueta: "Medio", clase: "riesgo-medio" },
  ALTO: { etiqueta: "Alto", clase: "riesgo-alto" },
  VARIABLE: { etiqueta: "Según el fondo", clase: "riesgo-variable" },
};

const FILTROS = [
  { valor: "TODOS", etiqueta: "Todos" },
  { valor: "BAJO", etiqueta: "Riesgo bajo" },
  { valor: "MEDIO", etiqueta: "Riesgo medio" },
  { valor: "ALTO", etiqueta: "Riesgo alto" },
];

// Qué niveles de riesgo suelen encajar con cada perfil que el usuario ya eligió arriba.
const ENCAJE = {
  CONSERVADOR: ["BAJO"],
  MODERADO: ["BAJO", "MEDIO", "VARIABLE"],
  AGRESIVO: ["MEDIO", "ALTO", "VARIABLE"],
};

function leerElegidas() {
  try {
    const guardadas = JSON.parse(localStorage.getItem(CLAVE_ELEGIDAS) || "[]");
    return Array.isArray(guardadas)
      ? guardadas.filter((c) => INSTRUMENTOS.some((i) => i.clave === c))
      : [];
  } catch {
    return [];
  }
}

export default function TablaInstrumentos({ perfil }) {
  const [filtro, setFiltro] = useState("TODOS");
  const [elegidas, setElegidas] = useState(leerElegidas);

  const encaja = ENCAJE[perfil] || ENCAJE.MODERADO;
  const visibles = INSTRUMENTOS.filter((i) => filtro === "TODOS" || i.riesgo === filtro);
  const seleccion = INSTRUMENTOS.filter((i) => elegidas.includes(i.clave));

  function guardar(nuevas) {
    setElegidas(nuevas);
    try {
      localStorage.setItem(CLAVE_ELEGIDAS, JSON.stringify(nuevas));
    } catch {
      // Sin almacenamiento disponible: la selección sigue funcionando en pantalla.
    }
  }

  function alternar(clave) {
    guardar(elegidas.includes(clave) ? elegidas.filter((c) => c !== clave) : [...elegidas, clave]);
  }

  return (
    <section className="tarjeta inv-comparador" aria-labelledby="titulo-comparador">
      <h2 id="titulo-comparador">Compara dónde puedes invertir</h2>
      <p className="inv-nota">
        Marca las opciones que te interesan para compararlas. Las que llevan la etiqueta «Encaja con tu perfil»
        coinciden con el perfil de riesgo que elegiste arriba.
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

      <div className="inv-tabla-scroll">
        <table className="tabla-instrumentos">
          <thead>
            <tr>
              <th scope="col">Elegir</th>
              <th scope="col">Instrumento</th>
              <th scope="col">Riesgo</th>
              <th scope="col">Rentabilidad</th>
              <th scope="col">Plazo sugerido</th>
              <th scope="col">Monto mínimo</th>
              <th scope="col">Liquidez</th>
              <th scope="col">Dónde se contrata</th>
            </tr>
          </thead>
          <tbody>
            {visibles.map((i) => {
              const riesgo = RIESGOS[i.riesgo];
              const marcada = elegidas.includes(i.clave);
              return (
                <tr key={i.clave} className={marcada ? "fila-elegida" : undefined}>
                  <td>
                    <input
                      type="checkbox"
                      checked={marcada}
                      onChange={() => alternar(i.clave)}
                      aria-label={`Elegir ${i.nombre}`}
                    />
                  </td>
                  <th scope="row" className="celda-nombre">
                    {i.nombre}
                    {encaja.includes(i.riesgo) && <span className="inv-encaja">Encaja con tu perfil</span>}
                  </th>
                  <td>
                    <span className={`inv-chip-riesgo ${riesgo.clase}`}>{riesgo.etiqueta}</span>
                  </td>
                  <td>{i.rentabilidad}</td>
                  <td>{i.plazo}</td>
                  <td>{i.minimo}</td>
                  <td>{i.liquidez}</td>
                  <td>{i.donde}</td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>

      <div className="inv-elegidas" aria-live="polite">
        {seleccion.length === 0 ? (
          <p>Aún no has marcado ninguna opción.</p>
        ) : (
          <>
            <p>
              <strong>
                {seleccion.length === 1 ? "Tu opción elegida:" : `Tus ${seleccion.length} opciones elegidas:`}
              </strong>{" "}
              {seleccion.map((i) => i.nombre).join(" · ")}
            </p>
            <button type="button" className="boton-secundario" onClick={() => guardar([])}>
              Limpiar selección
            </button>
          </>
        )}
      </div>

      <p className="inv-aviso">
        Información de referencia, no es asesoría financiera. No incluye tasas porque cambian con frecuencia:
        confirma rentabilidad, comisiones, plazos y montos mínimos vigentes directamente con la institución antes
        de invertir.
      </p>
    </section>
  );
}
