import { Link, useNavigate } from "react-router-dom";

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
  salir: (
    <svg {...SVG}>
      <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4M16 17l5-5-5-5M21 12H9" />
    </svg>
  ),
};

export default function BarraNavegacion({ paginaActiva }) {
  const navigate = useNavigate();

  function cerrarSesion() {
    localStorage.removeItem("fb_user");
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
        </nav>
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
        <button className="tab-movil" onClick={cerrarSesion}>
          {iconos.salir}
          <span>Salir</span>
        </button>
      </nav>
    </>
  );
}
