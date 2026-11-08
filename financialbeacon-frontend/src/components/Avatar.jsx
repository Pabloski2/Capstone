import "./Perfil.css";

// Muestra la foto de perfil o, si no hay, las iniciales del nombre.
export default function Avatar({ nombre = "", foto, tamano = 40 }) {
  const iniciales = nombre
    .trim()
    .split(/\s+/)
    .slice(0, 2)
    .map((p) => p[0]?.toUpperCase() || "")
    .join("");

  const estilo = { width: tamano, height: tamano, fontSize: Math.round(tamano * 0.38) };

  return foto ? (
    <img className="avatar" style={estilo} src={foto} alt={`Foto de ${nombre || "perfil"}`} />
  ) : (
    <span className="avatar avatar-iniciales" style={estilo} aria-hidden="true">
      {iniciales || "?"}
    </span>
  );
}
