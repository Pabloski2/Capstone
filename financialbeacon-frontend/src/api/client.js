// Usa el mismo host desde el que se cargó la página: funciona tanto en
// localhost (PC) como cuando se abre desde el celular usando la IP del PC.
const BASE_URL = `http://${window.location.hostname}:8080/api`;

async function manejarRespuesta(res) {
  const data = await res.json().catch(() => null);
  if (!res.ok) {
    throw new Error(typeof data === "string" ? data : "Ocurrió un error");
  }
  return data;
}

export const api = {
  registrar: (email, password, nombre) =>
    fetch(`${BASE_URL}/auth/register`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email, password, nombre }),
    }).then(manejarRespuesta),

  login: (email, password) =>
    fetch(`${BASE_URL}/auth/login`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email, password }),
    }).then(manejarRespuesta),

  registrarIngreso: (userId, monto, mes, anio) =>
    fetch(`${BASE_URL}/finanzas/ingreso`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ userId, monto, mes, anio }),
    }).then(manejarRespuesta),

  registrarGasto: (userId, descripcion, monto, categoria, fecha) =>
    fetch(`${BASE_URL}/finanzas/gasto`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ userId, descripcion, monto, categoria, fecha }),
    }).then(manejarRespuesta),

  obtenerResumen: (userId, mes, anio) =>
    fetch(`${BASE_URL}/finanzas/resumen?userId=${userId}&mes=${mes}&anio=${anio}`).then(
      manejarRespuesta
    ),

  listarGastos: (userId, mes, anio) =>
    fetch(`${BASE_URL}/finanzas/gastos?userId=${userId}&mes=${mes}&anio=${anio}`).then(
      manejarRespuesta
    ),

  registrarDeuda: (userId, nombre, montoTotal, tasaInteres, cuotaMinima) =>
    fetch(`${BASE_URL}/deudas`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ userId, nombre, montoTotal, tasaInteres, cuotaMinima }),
    }).then(manejarRespuesta),

  listarDeudas: (userId) =>
    fetch(`${BASE_URL}/deudas?userId=${userId}`).then(manejarRespuesta),

  obtenerEstrategia: (userId, mes, anio) =>
    fetch(`${BASE_URL}/deudas/estrategia?userId=${userId}&mes=${mes}&anio=${anio}`).then(
      manejarRespuesta
    ),

  // metaAhorro es opcional: si viene, el backend agrega un plan para alcanzarla.
  obtenerRecomendaciones: (userId, mes, anio, metaAhorro) => {
    const meta = metaAhorro ? `&metaAhorro=${encodeURIComponent(metaAhorro)}` : "";
    return fetch(
      `${BASE_URL}/recomendaciones?userId=${userId}&mes=${mes}&anio=${anio}${meta}`
    ).then(manejarRespuesta);
  },

  // Plan de inversión: etapa actual, clases de instrumento sugeridas y proyección.
  obtenerPlanInversion: (userId, mes, anio, perfil, horizonteMeses, fondoEmergenciaActual) =>
    fetch(
      `${BASE_URL}/inversion?userId=${userId}&mes=${mes}&anio=${anio}` +
        `&perfil=${encodeURIComponent(perfil)}&horizonteMeses=${horizonteMeses}` +
        `&fondoEmergenciaActual=${fondoEmergenciaActual || 0}`
    ).then(manejarRespuesta),
};
