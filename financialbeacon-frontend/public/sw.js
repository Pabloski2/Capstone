const CACHE_NAME = "financial-beacon-v1";

self.addEventListener("install", (event) => {
  self.skipWaiting();
});

self.addEventListener("activate", (event) => {
  self.clients.claim();
});

// Passthrough simple: deja pasar todas las peticiones a la red normalmente.
// Es el mínimo necesario para que Chrome/Edge ofrezcan "Instalar app".
self.addEventListener("fetch", (event) => {
  event.respondWith(fetch(event.request));
});
