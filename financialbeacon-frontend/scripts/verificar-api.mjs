// Evita generar una app móvil que apunte a "localhost" (dentro del celular, localhost es el propio celular).
import { loadEnv } from "vite";

const env = loadEnv("production", process.cwd(), "VITE_");
const url = (process.env.VITE_API_URL || env.VITE_API_URL || "").trim();

if (!url) {
  console.error("\n✖ Falta VITE_API_URL.\n  Copia .env.example como .env.production y escribe la URL de tu backend (termina en /api).\n");
  process.exit(1);
}
if (!url.startsWith("https://")) {
  console.error(`\n✖ VITE_API_URL debe empezar con https:// (Android bloquea HTTP sin cifrar). Valor actual: ${url}\n`);
  process.exit(1);
}
if (!/\/api\/?$/.test(url)) {
  console.error(`\n✖ VITE_API_URL debe terminar en /api. Valor actual: ${url}\n`);
  process.exit(1);
}
console.log(`✔ API de la app móvil: ${url}`);
