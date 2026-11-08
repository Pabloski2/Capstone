import { cerrarSesionLocal, leerSesion } from "../utils/sesion.js";

// URL del backend:
//  - Web en desarrollo: mismo host desde el que se cargó la página, puerto 8080
//    (funciona en localhost y desde el celular usando la IP del PC).
//  - App móvil (Capacitor) y producción: se define en VITE_API_URL al compilar,
//    por ejemplo https://tu-backend.ejemplo.com/api (ver .env.example).
const BASE_URL = (
  import.meta.env.VITE_API_URL || `http://${window.location.hostname}:8080/api`
).replace(/\/+$/, "");

// Todas las peticiones pasan por aquí: agrega el token, traduce los errores
// a mensajes legibles y cierra la sesión si el servidor responde 401.
async function peticion(ruta, { metodo = "GET", cuerpo, conSesion = true } = {}) {
  const headers = {};
  if (cuerpo !== undefined) headers["Content-Type"] = "application/json";
  const token = conSesion ? leerSesion()?.token : null;
  if (token) headers.Authorization = `Bearer ${token}`;

  let res;
  try {
    res = await fetch(`${BASE_URL}${ruta}`, {
      method: metodo,
      headers,
      body: cuerpo !== undefined ? JSON.stringify(cuerpo) : undefined,
    });
  } catch {
    throw new Error("No se pudo conectar con el servidor. Revisa tu conexión a internet.");
  }

  const data = await res.json().catch(() => null); // las respuestas 204 no traen cuerpo

  if (res.status === 401 && conSesion) {
    cerrarSesionLocal();
    window.location.assign("/login");
    throw new Error("Tu sesión expiró. Inicia sesión nuevamente.");
  }
  if (!res.ok) {
    throw new Error(data?.mensaje || "Ocurrió un error");
  }
  return data;
}

// Nota: el backend toma al usuario desde el token, por eso ya no se envía userId.
// Las funciones conservan el parámetro `_userId` solo para no tener que cambiar las pantallas.
export const api = {
  // ---- Sesión y recuperación ----
  registrar: (email, password, nombre) =>
    peticion("/auth/register", { metodo: "POST", cuerpo: { email, password, nombre }, conSesion: false }),

  login: (email, password) =>
    peticion("/auth/login", { metodo: "POST", cuerpo: { email, password }, conSesion: false }),

  solicitarRecuperacion: (email) =>
    peticion("/auth/recuperar", { metodo: "POST", cuerpo: { email }, conSesion: false }),

  restablecerPassword: (email, codigo, nuevaPassword) =>
    peticion("/auth/restablecer", { metodo: "POST", cuerpo: { email, codigo, nuevaPassword }, conSesion: false }),

  // ---- Cuenta ----
  obtenerPerfil: () => peticion("/cuenta/perfil"),
  guardarPerfil: (datos) => peticion("/cuenta/perfil", { metodo: "PUT", cuerpo: datos }),
  cambiarEmail: (nuevoEmail, password) =>
    peticion("/cuenta/email", { metodo: "POST", cuerpo: { nuevoEmail, password } }),
  cambiarPassword: (actual, nueva) =>
    peticion("/cuenta/password", { metodo: "POST", cuerpo: { actual, nueva } }),
  guardarFoto: (foto) => peticion("/cuenta/foto", { metodo: "PUT", cuerpo: { foto } }),
  eliminarFoto: () => peticion("/cuenta/foto", { metodo: "DELETE" }),
  // Borra la cuenta y todos sus datos (exige la contraseña).
  eliminarCuenta: (password) => peticion("/cuenta/eliminar", { metodo: "POST", cuerpo: { password } }),

  // ---- Soporte ----
  crearTicket: (asunto, categoria, mensaje) =>
    peticion("/soporte", { metodo: "POST", cuerpo: { asunto, categoria, mensaje } }),
  listarTickets: () => peticion("/soporte"),

  // ---- Finanzas ----
  registrarIngreso: (_userId, monto, mes, anio) =>
    peticion("/finanzas/ingreso", { metodo: "POST", cuerpo: { monto, mes, anio } }),

  registrarGasto: (_userId, descripcion, monto, categoria, fecha) =>
    peticion("/finanzas/gasto", { metodo: "POST", cuerpo: { descripcion, monto, categoria, fecha } }),

  eliminarGasto: (id) => peticion(`/finanzas/gastos/${id}`, { metodo: "DELETE" }),

  obtenerResumen: (_userId, mes, anio) => peticion(`/finanzas/resumen?mes=${mes}&anio=${anio}`),

  listarGastos: (_userId, mes, anio) => peticion(`/finanzas/gastos?mes=${mes}&anio=${anio}`),

  registrarDeuda: (_userId, nombre, montoTotal, tasaInteres, cuotaMinima) =>
    peticion("/deudas", { metodo: "POST", cuerpo: { nombre, montoTotal, tasaInteres, cuotaMinima } }),

  eliminarDeuda: (id) => peticion(`/deudas/${id}`, { metodo: "DELETE" }),

  listarDeudas: () => peticion("/deudas"),

  obtenerEstrategia: (_userId, mes, anio) => peticion(`/deudas/estrategia?mes=${mes}&anio=${anio}`),

  // metaAhorro es opcional: si viene, el backend agrega un plan para alcanzarla.
  obtenerRecomendaciones: (_userId, mes, anio, metaAhorro) => {
    const meta = metaAhorro ? `&metaAhorro=${encodeURIComponent(metaAhorro)}` : "";
    return peticion(`/recomendaciones?mes=${mes}&anio=${anio}${meta}`);
  },

  // Plan de inversión: etapa actual, clases de instrumento sugeridas y proyección.
  obtenerPlanInversion: (_userId, mes, anio, perfil, horizonteMeses, fondoEmergenciaActual) =>
    peticion(
      `/inversion?mes=${mes}&anio=${anio}&perfil=${encodeURIComponent(perfil)}` +
        `&horizonteMeses=${horizonteMeses}&fondoEmergenciaActual=${fondoEmergenciaActual || 0}`
    ),
};
