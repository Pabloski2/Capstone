import { Link, useNavigate } from "react-router-dom";
import { cerrarSesionLocal, leerSesion } from "../utils/sesion.js";
import Avatar from "./Avatar.jsx";

const SVG = {
  fill: "none",
  stroke: "currentColor",
  strokeWidth: 2,
  strokeLinecap: "round",
  strokeLinejoin: "round",
  viewBox: "0 0 24 24",
  "aria-hidden": "true",
};

const iconos = {
  resumen: (
    <svg {...SVG}>
      <path d="M4 20V10M10 20V4M16 20v-7M22 20H2" />
    </svg>
  ),
  deudas: (
    <svg {...SVG}>
      <rect x="2" y="5" width="20" height="14" rx="3" />
      <path d="M2 10h20M6 15h4" />
    </svg>
  ),
  inversion: (
    <svg {...SVG}>
      <path d="M3 17l6-6 4 4 8-8" />
      <path d="M15 7h6v6" />
    </svg>
  ),
  cuenta: (
    <svg {...SVG}>
      <circle cx="12" cy="8" r="4" />
      <path d="M4 21a8 8 0 0 1 16 0" />
    </svg>
  ),
};

export default function BarraNavegacion({ paginaActiva }) {
  const navigate = useNavigate();

  const sesion = leerSesion();

  function cerrarSesion() {
    cerrarSesionLocal();
    navigate("/login");
  }

  const claseEnlace = (p) => `nav-enlace${paginaActiva === p ? " activo" : ""}`;
  const claseTab = (p) => `tab-movil${paginaActiva === p ? " activo" : ""}`;
  const actual = (p) => (paginaActiva === p ? "page" : undefined);

  return (
    <>
      {/* PC: barra superior. Celular: solo la marca, la navegación pasa abajo. */}
      <header className="barra-navegacion">
        <div className="auth-marca">
          <span className="auth-marca-punto" />
          Financial Beacon
        </div>
        <nav className="nav-escritorio" aria-label="Principal">
          <Link className={claseEnlace("dashboard")} to="/dashboard" aria-current={actual("dashboard")}>
            Resumen
          </Link>
          <Link className={claseEnlace("deudas")} to="/deudas" aria-current={actual("deudas")}>
            Deudas
          </Link>
          <Link className={claseEnlace("inversion")} to="/inversion" aria-current={actual("inversion")}>
            Inversión
          </Link>
          <Link className={claseEnlace("soporte")} to="/soporte" aria-current={actual("soporte")}>
            Soporte
          </Link>
        </nav>
        <Link
          className={`nav-avatar nav-avatar-escritorio${paginaActiva === "cuenta" ? " activo" : ""}`}
          to="/cuenta"
          aria-current={actual("cuenta")}
        >
          <Avatar nombre={sesion?.nombre} foto={sesion?.fotoPerfil} tamano={34} />
          <span>Mi cuenta</span>
        </Link>
        <button className="boton-secundario boton-salir" onClick={cerrarSesion}>
          Cerrar sesión
        </button>
      </header>

      <nav className="tabs-movil" aria-label="Navegación móvil">
        <Link className={claseTab("dashboard")} to="/dashboard" aria-current={actual("dashboard")}>
          {iconos.resumen}
          <span>Resumen</span>
        </Link>
        <Link className={claseTab("deudas")} to="/deudas" aria-current={actual("deudas")}>
          {iconos.deudas}
          <span>Deudas</span>
        </Link>
        <Link className={claseTab("inversion")} to="/inversion" aria-current={actual("inversion")}>
          {iconos.inversion}
          <span>Inversión</span>
        </Link>
        <Link className={claseTab("cuenta")} to="/cuenta" aria-current={actual("cuenta")}>
          {sesion?.fotoPerfil ? <Avatar nombre={sesion?.nombre} foto={sesion.fotoPerfil} tamano={24} /> : iconos.cuenta}
          <span>Cuenta</span>
        </Link>
      </nav>
    </>
  );
}
