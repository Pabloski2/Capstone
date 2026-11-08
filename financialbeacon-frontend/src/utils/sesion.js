// La sesión (datos básicos + token) vive en localStorage bajo "fb_user".
const CLAVE = "fb_user";

export function leerSesion() {
  try {
    return JSON.parse(localStorage.getItem(CLAVE) || "null");
  } catch {
    return null;
  }
}

export function guardarSesion(sesion) {
  localStorage.setItem(CLAVE, JSON.stringify(sesion));
}

// Cambia solo algunos campos de la sesión guardada (por ejemplo la foto o el nombre).
export function actualizarSesion(parcial) {
  const actual = leerSesion();
  if (actual) guardarSesion({ ...actual, ...parcial });
}

// Borra la sesión y lo guardado en este dispositivo para ese usuario.
export function cerrarSesionLocal() {
  const actual = leerSesion();
  localStorage.removeItem(CLAVE);
  localStorage.removeItem("fb_inversion");
  if (actual?.id) localStorage.removeItem(`fb_meta_${actual.id}`);
}

// Quien aún no completó sus datos personales debe pasar primero por la bienvenida.
export function rutaTrasLogin(sesion) {
  return sesion?.perfilCompleto === false ? "/bienvenida" : "/dashboard";
}
