import { useNavigate } from "react-router-dom";

export default function DashboardPlaceholder() {
  const navigate = useNavigate();
  const usuario = JSON.parse(localStorage.getItem("fb_user") || "{}");

  function cerrarSesion() {
    localStorage.removeItem("fb_user");
    navigate("/login");
  }

  return (
    <div className="pantalla-centrada">
      <div className="tarjeta">
        <h1>¡Hola, {usuario.nombre}!</h1>
        <p className="subtitulo">
          El login funciona. El dashboard con ingresos, gastos y deudas se
          arma en el próximo paso.
        </p>
        <button className="boton-primario" onClick={cerrarSesion}>
          Cerrar sesión
        </button>
      </div>
    </div>
  );
}
