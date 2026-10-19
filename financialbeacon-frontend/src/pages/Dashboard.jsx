import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { api } from "../api/client.js";
import BarraNavegacion from "../components/BarraNavegacion.jsx";
import BarraReparto from "../components/BarraReparto.jsx";
import GraficoDona from "../components/GraficoDona.jsx";
import ListaRecomendaciones from "../components/ListaRecomendaciones.jsx";
import TopGastos from "../components/TopGastos.jsx";
import { formatoCLP } from "../utils/formato.js";

const CATEGORIAS = ["FIJO", "VARIABLE", "HORMIGA"];

function hoy() {
  const d = new Date();
  return { mes: d.getMonth() + 1, anio: d.getFullYear(), fechaISO: d.toISOString().slice(0, 10) };
}

export default function Dashboard() {
  const navigate = useNavigate();
  const usuario = JSON.parse(localStorage.getItem("fb_user") || "{}");
  const { mes, anio, fechaISO } = hoy();

  const claveMeta = `fb_meta_${usuario.id}`;

  const [resumen, setResumen] = useState(null);
  const [gastos, setGastos] = useState([]);
  const [recomendaciones, setRecomendaciones] = useState([]);
  const [meta, setMeta] = useState(() => localStorage.getItem(claveMeta) || "");
  const [metaInput, setMetaInput] = useState(() => localStorage.getItem(claveMeta) || "");
  const [montoIngreso, setMontoIngreso] = useState("");
  const [nuevoGasto, setNuevoGasto] = useState({
    descripcion: "",
    monto: "",
    categoria: "FIJO",
    fecha: fechaISO,
  });
  const [error, setError] = useState("");
  const [cargando, setCargando] = useState(true);

  async function cargarDatos(metaActual = meta) {
    try {
      const [r, g, recs] = await Promise.all([
        api.obtenerResumen(usuario.id, mes, anio),
        api.listarGastos(usuario.id, mes, anio),
        // Si las recomendaciones fallan, el resto del dashboard igual se muestra.
        api.obtenerRecomendaciones(usuario.id, mes, anio, metaActual).catch(() => []),
      ]);
      setResumen(r);
      setGastos(g);
      setRecomendaciones(recs);
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

  async function guardarMeta(e) {
    e.preventDefault();
    const nueva = metaInput.trim();
    if (nueva) {
      localStorage.setItem(claveMeta, nueva);
    } else {
      localStorage.removeItem(claveMeta);
    }
    setMeta(nueva);
    await cargarDatos(nueva);
  }

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

  if (cargando) {
    return (
      <div className="pantalla-centrada">
        <p>Cargando tu resumen...</p>
      </div>
    );
  }

  const ingreso = Number(resumen?.ingresoMensual || 0);
  const fijos = Number(resumen?.totalGastosFijos || 0);
  const variables = Number(resumen?.totalGastosVariables || 0);
  const hormiga = Number(resumen?.totalGastosHormiga || 0);
  const totalGastos = Number(resumen?.totalGastos || 0);
  const ahorro = Number(resumen?.ahorroEstimado || 0);
  const ahorroPositivo = ahorro >= 0;

  return (
    <>
      <BarraNavegacion paginaActiva="dashboard" />
      <div className="contenedor-dashboard">
        <header className="encabezado-dashboard">
          <h1>Hola, {usuario.nombre}</h1>
          <p className="subtitulo">
            {new Date(anio, mes - 1).toLocaleDateString("es-CL", {
              month: "long",
              year: "numeric",
            })}
          </p>
        </header>

        {error && <div className="mensaje-error">{error}</div>}

        <div className={`metrica-hero${ahorroPositivo ? "" : " hero-negativo"}`}>
          <span className="etiqueta-metrica">
            {ahorroPositivo ? "Ahorro estimado este mes" : "Estás sobre tu ingreso este mes"}
          </span>
          <div className="valor-metrica">{formatoCLP(ahorro)}</div>
          <p className="nota-metrica">
            Ingreso {formatoCLP(ingreso)} · Gastos {formatoCLP(totalGastos)}
          </p>
          <BarraReparto ingreso={ingreso} fijos={fijos} variables={variables} hormiga={hormiga} />
        </div>

        <section className="grid-graficos">
          <div className="tarjeta">
            <h2>Gastos por categoría</h2>
            <GraficoDona fijos={fijos} variables={variables} hormiga={hormiga} />
          </div>
          <div className="tarjeta">
            <h2>Tus gastos más altos</h2>
            <TopGastos gastos={gastos} />
          </div>
        </section>

        <ListaRecomendaciones
          recomendaciones={recomendaciones}
          filtro={(r) => r.tipo === "AHORRO" || r.nivel === "ALERTA"}
          titulo="Recomendaciones para ti"
          vacio="Por ahora no hay recomendaciones. Sigue registrando tus gastos."
        >
          <form className="meta-ahorro" onSubmit={guardarMeta}>
            <div className="campo">
              <label htmlFor="meta">Mi meta de ahorro mensual ($)</label>
              <input
                id="meta"
                type="number"
                min="0"
                inputMode="numeric"
                value={metaInput}
                onChange={(e) => setMetaInput(e.target.value)}
                placeholder="Ej: 150000 (vacío = sin meta)"
              />
            </div>
            <button className="boton-secundario" type="submit">
              {meta ? "Actualizar meta" : "Fijar meta"}
            </button>
          </form>
        </ListaRecomendaciones>

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
                  inputMode="numeric"
                  value={montoIngreso}
                  onChange={(e) => setMontoIngreso(e.target.value)}
                  placeholder={ingreso ? String(ingreso) : "0"}
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
                  inputMode="numeric"
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
            <p className="estado-vacio">
              Todavía no registras gastos este mes. Agrega el primero arriba
              para empezar a ver tu resumen.
            </p>
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
                    <td className="celda-desc">{g.descripcion}</td>
                    <td className="celda-cat">
                      <span className={`etiqueta-categoria etiqueta-${g.categoria.toLowerCase()}`}>
                        <span className="punto-categoria" />
                        {g.categoria}
                      </span>
                    </td>
                    <td className="celda-fecha">{g.fecha}</td>
                    <td className="celda-monto">{formatoCLP(g.monto)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </section>
      </div>
    </>
  );
}
