import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import Login from "./pages/Login.jsx";
import Register from "./pages/Register.jsx";
import Dashboard from "./pages/Dashboard.jsx";
import DebtManager from "./pages/DebtManager.jsx";
import Inversion from "./pages/Inversion.jsx";
import Cuenta from "./pages/Cuenta.jsx";
import Soporte from "./pages/Soporte.jsx";
import Bienvenida from "./pages/Bienvenida.jsx";
import RecuperarPassword from "./pages/RecuperarPassword.jsx";

function usuarioActual() {
  try {
    return JSON.parse(localStorage.getItem("fb_user") || "null");
  } catch {
    return null;
  }
}

// Sin sesión -> login. Con sesión pero sin datos personales -> bienvenida (primer paso obligatorio).
function RutaProtegida({ children, permitirPerfilIncompleto = false }) {
  const usuario = usuarioActual();
  if (!usuario) return <Navigate to="/login" replace />;
  if (usuario.perfilCompleto === false && !permitirPerfilIncompleto) {
    return <Navigate to="/bienvenida" replace />;
  }
  return children;
}

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Navigate to="/login" replace />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route
          path="/dashboard"
          element={
            <RutaProtegida>
              <Dashboard />
            </RutaProtegida>
          }
        />
        <Route
          path="/deudas"
          element={
            <RutaProtegida>
              <DebtManager />
            </RutaProtegida>
          }
        />
        <Route
          path="/inversion"
          element={
            <RutaProtegida>
              <Inversion />
            </RutaProtegida>
          }
        />
        <Route path="/recuperar" element={<RecuperarPassword />} />
        <Route
          path="/bienvenida"
          element={
            <RutaProtegida permitirPerfilIncompleto>
              <Bienvenida />
            </RutaProtegida>
          }
        />
        <Route
          path="/soporte"
          element={
            <RutaProtegida>
              <Soporte />
            </RutaProtegida>
          }
        />
        <Route
          path="/cuenta"
          element={
            <RutaProtegida>
              <Cuenta />
            </RutaProtegida>
          }
        />
      </Routes>
    </BrowserRouter>
  );
}
