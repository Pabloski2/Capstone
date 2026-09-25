import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { api } from "../api/client.js";

const CATEGORIAS = ["FIJO", "VARIABLE", "HORMIGA"];

function hoy() {
  const d = new Date();
  return { mes: d.getMonth() + 1, anio: d.getFullYear(), fechaISO: d.toISOString().slice(0, 10) };
}

function formatoCLP(valor) {
  return new Intl.NumberFormat("es-CL", {
    style: "currency",
    currency: "CLP",
    maximumFractionDigits: 0,
  }).format(valor || 0);
}

export default function Dashboard() {
  const navigate = useNavigate();
  const usuario = JSON.parse(localStorage.getItem("fb_user") || "{}");
  const { mes, anio, fechaISO } = hoy();

  const [resumen, setResumen] = useState(null);
  const [gastos, setGastos] = useState([]);
  const [montoIngreso, setMontoIngreso] = useState("");
  const [nuevoGasto, setNuevoGasto] = useState({
    descripcion: "",
    monto: "",
    categoria: "FIJO",
    fecha: fechaISO,
  });
  const [error, setError] = useState("");
  const [cargando, setCargando] = useState(true);

  async function cargarDatos() {
    setCargando(true);
    try {
      const [r, g] = await Promise.all([
        api.obtenerResumen(usuario.id, mes, anio),
        api.listarGastos(usuario.id, mes, anio),
      ]);
      setResumen(r);
      setGastos(g);
    } catch (err) {
      setError(err.message || "No se pudo cargar la información");
    } finally {
      setCargando(false);
    }
  }

  useEffect(() => {
    if (!usuario.id) {
      navigate("/login");
      return;
    }
    cargarDatos();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  async function guardarIngreso(e) {
    e.preventDefault();
    setError("");
    try {
      await api.registrarIngreso(usuario.id, Number(montoIngreso), mes, anio);
      setMontoIngreso("");
      await cargarDatos();
    } catch (err) {
      setError(err.message || "No se pudo guardar el ingreso");
    }
  }

  async function agregarGasto(e) {
    e.preventDefault();
    setError("");
    try {
      await api.registrarGasto(
        usuario.id,
        nuevoGasto.descripcion,
        Number(nuevoGasto.monto),
        nuevoGasto.categoria,
        nuevoGasto.fecha
      );
      setNuevoGasto({ descripcion: "", monto: "", categoria: "FIJO", fecha: fechaISO });
      await cargarDatos();
    } catch (err) {
      setError(err.message || "No se pudo registrar el gasto");
    }
  }

  function cerrarSesion() {
    localStorage.removeItem("fb_user");
    navigate("/login");
  }

  if (cargando) {
    return (
      <div className="pantalla-centrada">
        <p>Cargando...</p>
      </div>
    );
  }

  return (
    <div className="contenedor-dashboard">
      <header className="encabezado-dashboard">
        <div>
          <h1>Hola, {usuario.nombre}</h1>
          <p className="subtitulo">
            {new Date(anio, mes - 1).toLocaleDateString("es-CL", {
              month: "long",
              year: "numeric",
            })}
          </p>
        </div>
        <button className="boton-secundario" onClick={cerrarSesion}>
          Cerrar sesión
        </button>
      </header>

      {error && <div className="mensaje-error">{error}</div>}

      <section className="grid-resumen">
        <div className="tarjeta-metrica">
          <span className="etiqueta-metrica">Ingreso del mes</span>
          <span className="valor-metrica">{formatoCLP(resumen?.ingresoMensual)}</span>
        </div>
        <div className="tarjeta-metrica">
          <span className="etiqueta-metrica">Gastos fijos</span>
          <span className="valor-metrica">{formatoCLP(resumen?.totalGastosFijos)}</span>
        </div>
        <div className="tarjeta-metrica">
          <span className="etiqueta-metrica">Gastos variables</span>
          <span className="valor-metrica">{formatoCLP(resumen?.totalGastosVariables)}</span>
        </div>
        <div className="tarjeta-metrica">
          <span className="etiqueta-metrica">Gastos hormiga</span>
          <span className="valor-metrica">{formatoCLP(resumen?.totalGastosHormiga)}</span>
        </div>
        <div className={`tarjeta-metrica ${resumen?.ahorroEstimado < 0 ? "metrica-negativa" : "metrica-positiva"}`}>
          <span className="etiqueta-metrica">Ahorro estimado</span>
          <span className="valor-metrica">{formatoCLP(resumen?.ahorroEstimado)}</span>
        </div>
      </section>

      <section className="grid-formularios">
        <div className="tarjeta">
          <h2>Ingreso mensual</h2>
          <form onSubmit={guardarIngreso}>
            <div className="campo">
              <label htmlFor="ingreso">Monto ($)</label>
              <input
                id="ingreso"
                type="number"
                min="0"
                value={montoIngreso}
                onChange={(e) => setMontoIngreso(e.target.value)}
                placeholder={resumen?.ingresoMensual ? String(resumen.ingresoMensual) : "0"}
                required
              />
            </div>
            <button className="boton-primario" type="submit">
              Guardar ingreso
            </button>
          </form>
        </div>

        <div className="tarjeta">
          <h2>Nuevo gasto</h2>
          <form onSubmit={agregarGasto}>
            <div className="campo">
              <label htmlFor="descripcion">Descripción</label>
              <input
                id="descripcion"
                type="text"
                value={nuevoGasto.descripcion}
                onChange={(e) => setNuevoGasto({ ...nuevoGasto, descripcion: e.target.value })}
                required
              />
            </div>
            <div className="campo">
              <label htmlFor="montoGasto">Monto ($)</label>
              <input
                id="montoGasto"
                type="number"
                min="0"
                value={nuevoGasto.monto}
                onChange={(e) => setNuevoGasto({ ...nuevoGasto, monto: e.target.value })}
                required
              />
            </div>
            <div className="campo">
              <label htmlFor="categoria">Categoría</label>
              <select
                id="categoria"
                value={nuevoGasto.categoria}
                onChange={(e) => setNuevoGasto({ ...nuevoGasto, categoria: e.target.value })}
              >
                {CATEGORIAS.map((c) => (
                  <option key={c} value={c}>
                    {c}
                  </option>
                ))}
              </select>
            </div>
            <div className="campo">
              <label htmlFor="fecha">Fecha</label>
              <input
                id="fecha"
                type="date"
                value={nuevoGasto.fecha}
                onChange={(e) => setNuevoGasto({ ...nuevoGasto, fecha: e.target.value })}
                required
              />
            </div>
            <button className="boton-primario" type="submit">
              Agregar gasto
            </button>
          </form>
        </div>
      </section>

      <section className="tarjeta">
        <h2>Gastos del mes</h2>
        {gastos.length === 0 ? (
          <p className="subtitulo">Todavía no registras gastos este mes.</p>
        ) : (
          <table className="tabla-gastos">
            <thead>
              <tr>
                <th>Descripción</th>
                <th>Categoría</th>
                <th>Fecha</th>
                <th>Monto</th>
              </tr>
            </thead>
            <tbody>
              {gastos.map((g) => (
                <tr key={g.id}>
                  <td>{g.descripcion}</td>
                  <td>
                    <span className={`etiqueta-categoria etiqueta-${g.categoria.toLowerCase()}`}>
                      {g.categoria}
                    </span>
                  </td>
                  <td>{g.fecha}</td>
                  <td>{formatoCLP(g.monto)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </section>
    </div>
  );
}
