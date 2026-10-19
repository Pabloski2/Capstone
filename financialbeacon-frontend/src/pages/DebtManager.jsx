import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { api } from "../api/client.js";
import BarraNavegacion from "../components/BarraNavegacion.jsx";
import ListaRecomendaciones from "../components/ListaRecomendaciones.jsx";
import { formatoCLP } from "../utils/formato.js";

const TASA_ALTA = 30; // % anual: desde aquí se resalta en rojo

function hoy() {
  const d = new Date();
  return { mes: d.getMonth() + 1, anio: d.getFullYear() };
}

const ETIQUETA_ESTRATEGIA = {
  AVALANCHA: "Estrategia avalancha",
  BOLA_DE_NIEVE: "Estrategia bola de nieve",
  SIN_DEUDAS: "Sin deudas registradas",
};

export default function DebtManager() {
  const navigate = useNavigate();
  const usuario = JSON.parse(localStorage.getItem("fb_user") || "{}");
  const { mes, anio } = hoy();

  const [deudas, setDeudas] = useState([]);
  const [estrategia, setEstrategia] = useState(null);
  const [recomendaciones, setRecomendaciones] = useState([]);
  const [nuevaDeuda, setNuevaDeuda] = useState({
    nombre: "",
    montoTotal: "",
    tasaInteres: "",
    cuotaMinima: "",
  });
  const [error, setError] = useState("");
  const [cargando, setCargando] = useState(true);

  async function cargarDatos() {
    try {
      const [listaDeudas, estrategiaCalculada, recs] = await Promise.all([
        api.listarDeudas(usuario.id),
        api.obtenerEstrategia(usuario.id, mes, anio),
        // La meta de ahorro no afecta a las recomendaciones de deuda.
        api.obtenerRecomendaciones(usuario.id, mes, anio).catch(() => []),
      ]);
      setDeudas(listaDeudas);
      setEstrategia(estrategiaCalculada);
      setRecomendaciones(recs);
    } catch (err) {
      setError(err.message || "No se pudo cargar la información de deudas");
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

  async function agregarDeuda(e) {
    e.preventDefault();
    setError("");
    try {
      await api.registrarDeuda(
        usuario.id,
        nuevaDeuda.nombre,
        Number(nuevaDeuda.montoTotal),
        Number(nuevaDeuda.tasaInteres),
        Number(nuevaDeuda.cuotaMinima)
      );
      setNuevaDeuda({ nombre: "", montoTotal: "", tasaInteres: "", cuotaMinima: "" });
      await cargarDatos();
    } catch (err) {
      setError(err.message || "No se pudo registrar la deuda");
    }
  }

  if (cargando) {
    return (
      <div className="pantalla-centrada">
        <p>Cargando tus deudas...</p>
      </div>
    );
  }

  // Métricas calculadas a partir de las deudas registradas.
  const deudaTotal = deudas.reduce((s, d) => s + Number(d.montoTotal || 0), 0);
  const cuotasMinimas = deudas.reduce((s, d) => s + Number(d.cuotaMinima || 0), 0);
  const interesMensual = deudas.reduce(
    (s, d) => s + (Number(d.montoTotal || 0) * Number(d.tasaInteres || 0)) / 100 / 12,
    0
  );

  const orden = estrategia?.ordenDePago || [];
  const saldoMaximo = Math.max(...orden.map((d) => Number(d.montoTotal || 0)), 1);

  return (
    <>
      <BarraNavegacion paginaActiva="deudas" />
      <div className="contenedor-dashboard">
        <header className="encabezado-dashboard">
          <h1>Gestión de deudas</h1>
          <p className="subtitulo">Estrategia sugerida para este mes</p>
        </header>

        {error && <div className="mensaje-error">{error}</div>}

        {deudas.length > 0 && (
          <section className="grid-resumen">
            <div className="tarjeta-metrica">
              <span className="etiqueta-metrica">Deuda total</span>
              <span className="valor-metrica">{formatoCLP(deudaTotal)}</span>
            </div>
            <div className="tarjeta-metrica">
              <span className="etiqueta-metrica">Cuotas mínimas al mes</span>
              <span className="valor-metrica">{formatoCLP(cuotasMinimas)}</span>
            </div>
            <div className="tarjeta-metrica">
              <span className="etiqueta-metrica">Intereses al mes (aprox.)</span>
              <span className="valor-metrica metrica-negativa">{formatoCLP(interesMensual)}</span>
            </div>
          </section>
        )}

        {estrategia && estrategia.estrategiaRecomendada !== "SIN_DEUDAS" && (
          <section className="tarjeta tarjeta-estrategia">
            <span className="etiqueta-estrategia">
              {ETIQUETA_ESTRATEGIA[estrategia.estrategiaRecomendada]}
            </span>
            <p className="texto-justificacion">{estrategia.justificacion}</p>
            <p className="monto-disponible">
              Disponible este mes para pagar deudas:{" "}
              <strong>{formatoCLP(estrategia.montoDisponibleParaDeudas)}</strong>
            </p>
          </section>
        )}

        {estrategia && estrategia.estrategiaRecomendada === "SIN_DEUDAS" && (
          <section className="tarjeta" style={{ marginBottom: 24 }}>
            <p className="estado-vacio">
              Todavía no registras deudas. Agrega la primera en el formulario
              de abajo y aquí verás la estrategia sugerida para pagarlas.
            </p>
          </section>
        )}

        <ListaRecomendaciones
          recomendaciones={recomendaciones}
          filtro={(r) => r.tipo === "DEUDA"}
          titulo="Qué te recomendamos hacer"
        />

        <div className="grid-formularios">
          <div className="tarjeta">
            <h2>Nueva deuda</h2>
            <form onSubmit={agregarDeuda}>
              <div className="campo">
                <label htmlFor="nombreDeuda">Nombre</label>
                <input
                  id="nombreDeuda"
                  type="text"
                  value={nuevaDeuda.nombre}
                  onChange={(e) => setNuevaDeuda({ ...nuevaDeuda, nombre: e.target.value })}
                  required
                />
              </div>
              <div className="campo">
                <label htmlFor="montoTotal">Monto total ($)</label>
                <input
                  id="montoTotal"
                  type="number"
                  min="0"
                  inputMode="numeric"
                  value={nuevaDeuda.montoTotal}
                  onChange={(e) => setNuevaDeuda({ ...nuevaDeuda, montoTotal: e.target.value })}
                  required
                />
              </div>
              <div className="campo">
                <label htmlFor="tasaInteres">Tasa de interés anual (%)</label>
                <input
                  id="tasaInteres"
                  type="number"
                  min="0"
                  step="0.1"
                  inputMode="decimal"
                  value={nuevaDeuda.tasaInteres}
                  onChange={(e) => setNuevaDeuda({ ...nuevaDeuda, tasaInteres: e.target.value })}
                  required
                />
              </div>
              <div className="campo">
                <label htmlFor="cuotaMinima">Cuota mínima mensual ($)</label>
                <input
                  id="cuotaMinima"
                  type="number"
                  min="0"
                  inputMode="numeric"
                  value={nuevaDeuda.cuotaMinima}
                  onChange={(e) => setNuevaDeuda({ ...nuevaDeuda, cuotaMinima: e.target.value })}
                  required
                />
              </div>
              <button className="boton-primario" type="submit">
                Agregar deuda
              </button>
            </form>
          </div>

          <div className="tarjeta">
            <h2>Orden de pago sugerido</h2>
            {orden.length === 0 ? (
              <p className="estado-vacio">Registra al menos una deuda para ver el plan de pago.</p>
            ) : (
              <ol className="lista-orden-pago">
                {orden.map((d, i) => (
                  <li key={d.deudaId}>
                    <div className="fila-orden-pago">
                      <span className="numero-orden">{i + 1}</span>
                      <div className="cuerpo-orden">
                        <strong>{d.nombre}</strong>
                        <div className="detalle-orden-pago">
                          <span className={`chip-tasa${Number(d.tasaInteres) >= TASA_ALTA ? " alta" : ""}`}>
                            Tasa {d.tasaInteres}%
                          </span>
                          <span>Saldo {formatoCLP(d.montoTotal)}</span>
                        </div>
                        <div
                          className="pista pista-deuda"
                          role="img"
                          aria-label={`Saldo de ${d.nombre}: ${formatoCLP(d.montoTotal)}`}
                        >
                          <span style={{ width: `${(Number(d.montoTotal) / saldoMaximo) * 100}%` }} />
                        </div>
                      </div>
                      <span className="pago-sugerido">{formatoCLP(d.pagoSugeridoEsteMes)}</span>
                    </div>
                  </li>
                ))}
              </ol>
            )}
          </div>
        </div>

        <section className="tarjeta">
          <h2>Todas tus deudas</h2>
          {deudas.length === 0 ? (
            <p className="estado-vacio">Todavía no registras deudas.</p>
          ) : (
            <table className="tabla-gastos tabla-deudas">
              <thead>
                <tr>
                  <th>Nombre</th>
                  <th>Monto total</th>
                  <th>Tasa interés</th>
                  <th>Cuota mínima</th>
                </tr>
              </thead>
              <tbody>
                {deudas.map((d) => (
                  <tr key={d.id}>
                    <td data-label="Nombre">{d.nombre}</td>
                    <td data-label="Monto total">{formatoCLP(d.montoTotal)}</td>
                    <td data-label="Tasa interés">{d.tasaInteres}%</td>
                    <td data-label="Cuota mínima">{formatoCLP(d.cuotaMinima)}</td>
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
