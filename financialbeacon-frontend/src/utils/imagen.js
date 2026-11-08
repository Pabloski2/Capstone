// Reduce una foto del celular a un cuadrado de 256 px (JPEG) antes de enviarla:
// pesa unos 20-40 KB en vez de varios MB y carga rápido en cualquier conexión.
export function redimensionarFoto(archivo, lado = 256, calidad = 0.85) {
  return new Promise((resolve, reject) => {
    if (!archivo || !archivo.type.startsWith("image/")) {
      reject(new Error("Elige un archivo de imagen."));
      return;
    }
    if (archivo.size > 15 * 1024 * 1024) {
      reject(new Error("La imagen es demasiado grande (máximo 15 MB)."));
      return;
    }

    const url = URL.createObjectURL(archivo);
    const img = new Image();
    img.onload = () => {
      URL.revokeObjectURL(url);
      const canvas = document.createElement("canvas");
      canvas.width = lado;
      canvas.height = lado;
      const ctx = canvas.getContext("2d");
      // Recorte centrado (cover) para que quede cuadrada sin deformarse.
      const ladoOrigen = Math.min(img.width, img.height);
      const sx = (img.width - ladoOrigen) / 2;
      const sy = (img.height - ladoOrigen) / 2;
      ctx.fillStyle = "#ffffff";
      ctx.fillRect(0, 0, lado, lado);
      ctx.drawImage(img, sx, sy, ladoOrigen, ladoOrigen, 0, 0, lado, lado);
      resolve(canvas.toDataURL("image/jpeg", calidad));
    };
    img.onerror = () => {
      URL.revokeObjectURL(url);
      reject(new Error("No se pudo leer la imagen. Prueba con otra (JPG o PNG)."));
    };
    img.src = url;
  });
}
