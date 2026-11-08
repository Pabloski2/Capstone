import { useState } from "react";
import "./Perfil.css";

const TIPOS_INGRESO = [
  { valor: "FIJO", etiqueta: "Fijo (sueldo todos los meses)" },
  { valor: "VARIABLE", etiqueta: "Variable (honorarios, independiente, comisiones)" },
  { valor: "MIXTO", etiqueta: "Mixto (parte fija y parte variable)" },
];

// Fecha máxima de nacimiento para ser mayor de 18 años.
function maximaFechaNacimiento() {
  const d = new Date();
  d.setFullYear(d.getFullYear() - 18);
  const dos = (n) => String(n).padStart(2, "0");
  return `${d.getFullYear()}-${dos(d.getMonth() + 1)}-${dos(d.getDate())}`;
}

// Formulario de datos personales, compartido por la bienvenida (primer ingreso) y "Mi cuenta".
export default function FormularioPerfil({ inicial = {}, textoBoton, onGuardar }) {
  const [datos, setDatos] = useState({
    nombre: inicial.nombre || "",
    apellido: inicial.apellido || "",
    fechaNacimiento: inicial.fechaNacimiento || "",
    telefono: inicial.telefono || "",
    ciudad: inicial.ciudad || "",
    ocupacion: inicial.ocupacion || "",
    tipoIngreso: inicial.tipoIngreso || "",
  });
  const [error, setError] = useState("");
  const [guardando, setGuardando] = useState(false);

  const cambiar = (campo) => (e) => setDatos({ ...datos, [campo]: e.target.value });

  async function enviar(e) {
    e.preventDefault();
    setError("");
    setGuardando(true);
    try {
      await onGuardar(datos);
    } catch (err) {
      setError(err.message || "No se pudieron guardar los datos");
    } finally {
      setGuardando(false);
    }
  }

  return (
    <form onSubmit={enviar}>
      {error && <div className="mensaje-error">{error}</div>}

      <div className="formulario-grid">
        <div className="campo">
          <label htmlFor="p-nombre">Nombre</label>
          <input id="p-nombre" autoComplete="given-name" value={datos.nombre} onChange={cambiar("nombre")} maxLength={100} required />
        </div>
        <div className="campo">
          <label htmlFor="p-apellido">Apellido</label>
          <input id="p-apellido" autoComplete="family-name" value={datos.apellido} onChange={cambiar("apellido")} maxLength={100} required />
        </div>
        <div className="campo">
          <label htmlFor="p-nacimiento">Fecha de nacimiento</label>
          <input
            id="p-nacimiento"
            type="date"
            autoComplete="bday"
            value={datos.fechaNacimiento}
            onChange={cambiar("fechaNacimiento")}
            min="1900-01-01"
            max={maximaFechaNacimiento()}
            required
          />
        </div>
        <div className="campo">
          <label htmlFor="p-telefono">Teléfono</label>
          <input
            id="p-telefono"
            type="tel"
            autoComplete="tel"
            placeholder="+56 9 1234 5678"
            value={datos.telefono}
            onChange={cambiar("telefono")}
            maxLength={20}
            required
          />
        </div>
        <div className="campo">
          <label htmlFor="p-ciudad">Ciudad o comuna</label>
          <input id="p-ciudad" autoComplete="address-level2" value={datos.ciudad} onChange={cambiar("ciudad")} maxLength={100} required />
        </div>
        <div className="campo">
          <label htmlFor="p-ocupacion">Ocupación</label>
          <input id="p-ocupacion" placeholder="Ej: Diseñador, estudiante, comerciante" value={datos.ocupacion} onChange={cambiar("ocupacion")} maxLength={100} required />
        </div>
      </div>

      <div className="campo">
        <label htmlFor="p-ingreso">¿Cómo recibes tus ingresos?</label>
        <select id="p-ingreso" value={datos.tipoIngreso} onChange={cambiar("tipoIngreso")} required>
          <option value="" disabled>
            Selecciona una opción
          </option>
          {TIPOS_INGRESO.map((t) => (
            <option key={t.valor} value={t.valor}>
              {t.etiqueta}
            </option>
          ))}
        </select>
        <small className="ayuda-campo">Nos ayuda a ajustar tus recomendaciones. Debes ser mayor de 18 años.</small>
      </div>

      <button className="boton-primario" type="submit" disabled={guardando}>
        {guardando ? "Guardando..." : textoBoton}
      </button>
    </form>
  );
}
