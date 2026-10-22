import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import Login from "./pages/Login.jsx";
import Register from "./pages/Register.jsx";
import Dashboard from "./pages/Dashboard.jsx";
import DebtManager from "./pages/DebtManager.jsx";
import Inversion from "./pages/Inversion.jsx";

function usuarioActual() {
  const raw = localStorage.getItem("fb_user");
  return raw ? JSON.parse(raw) : null;
}

function RutaProtegida({ children }) {
  return usuarioActual() ? children : <Navigate to="/login" replace />;
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
      </Routes>
    </BrowserRouter>
  );
}
