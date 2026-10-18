import { Link, useNavigate } from "react-router-dom";

export default function BarraNavegacion({ paginaActiva }) {
  const navigate = useNavigate();

  function cerrarSesion() {
    localStorage.removeItem("fb_user");
    navigate("/login");
  }

  return (
    <nav className="barra-navegacion">
      <div className="auth-marca">
        <span className="auth-marca-punto" />
        Financial Beacon
      </div>
      <div className="nav-acciones">
        {paginaActiva === "deudas" ? (
          <Link className="boton-secundario" to="/dashboard">
            ← Dashboard
          </Link>
        ) : (
          <Link className="boton-secundario" to="/deudas">
            Gestión de deudas
          </Link>
        )}
        <button className="boton-secundario" onClick={cerrarSesion}>
          Cerrar sesión
        </button>
      </div>
    </nav>
  );
}
