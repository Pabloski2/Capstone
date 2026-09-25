const BASE_URL = "http://localhost:8080/api";

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
};
