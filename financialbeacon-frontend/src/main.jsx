import React from "react";
import ReactDOM from "react-dom/client";
import App from "./App.jsx";
import "./index.css";

ReactDOM.createRoot(document.getElementById("root")).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);

// Dentro de la app nativa (Capacitor) no hace falta el service worker.
const esAppNativa = Boolean(window.Capacitor?.isNativePlatform?.());

if ("serviceWorker" in navigator && !esAppNativa) {
  window.addEventListener("load", () => {
    navigator.serviceWorker.register("/sw.js").catch(() => {
      // Si falla el registro, la app sigue funcionando normal como web.
    });
  });
}
