# Actualización: seguridad reforzada y sección "Dónde invertir"

## Qué cambia

**Seguridad (se corrigen las 4 fallas graves y las medias)**
- Las contraseñas se guardan cifradas con BCrypt (antes estaban en texto plano).
- Ya no se devuelve al usuario ni su contraseña en ninguna respuesta.
- Inicio de sesión con token (JWT): el servidor identifica al usuario por el token y ya no confía en el `userId`
  que envía el navegador. Cada persona solo ve y modifica sus propios datos.
- El registro ya no acepta un `id` enviado por el cliente (no se puede tomar la cuenta de otro).
- Validación de datos en el backend y mensajes de error seguros (sin detalles internos).
- Contraseña de 8 a 72 caracteres, con letras y números. Mismo error para correo inexistente y clave incorrecta,
  y bloqueo de 15 minutos tras 5 intentos fallidos.
- CORS restringido: solo la app local y las redes privadas (para el celular) pueden usar la API.
- Cabeceras de seguridad, Spring Boot actualizado a 3.5 (la 3.3 ya no recibe soporte) y usuario de base de datos limitado.

**Sección "Dónde invertir"** (pestaña Invertir)
- Tabla con 9 opciones: depósitos a plazo (pesos y UF), fondos mutuos, APV, acciones/ETF y criptomonedas.
  Para cada una: riesgo, rentabilidad anual de referencia (escenario bajo y alto) y cuánto supera la inflación,
  plazo recomendado, monto mínimo, liquidez y una proyección con tu monto. Cada fila tiene un detalle con qué es,
  dónde se contrata, qué tener en cuenta y la fuente de los datos.
- Simulador: monto inicial, aporte mensual y plazo. Filtros por riesgo y "solo las que alcanzo con mi monto".
- Usa tus datos: el dinero disponible es tu ahorro del mes menos las cuotas mínimas de tus deudas. Si una deuda cobra
  más que la rentabilidad máxima de las opciones de riesgo bajo, te lo muestra antes de invertir.
- Es información educativa y referencial. No recomienda ninguna opción ni es asesoría financiera.

## Cómo instalarlo
1. Detén el backend (⏹) y el frontend (Ctrl+C).
2. Descomprime este zip DENTRO de la carpeta `FinancialBeacon` y elige "Reemplazar los archivos en el destino".
3. **Cuentas antiguas:** las claves en texto plano ya no sirven. Borra las cuentas viejas en phpMyAdmin
   (pestaña SQL de la base `financialbeacon`: `DELETE FROM users;`) y regístrate de nuevo. O importa
   `database/datos_demo.sql` y entra con **demo@financialbeacon.cl** y clave **Demo1234!**.
4. En VS Code: Ctrl+Shift+P, "Java: Clean Java Language Server Workspace", "Restart and delete". El `pom.xml` cambió,
   así que descargará librerías nuevas (necesita internet). Luego F5. MySQL debe estar encendido en XAMPP.
   La tabla de inversiones y sus datos se crean solos al iniciar.
5. Frontend: no hace falta `npm install`. Ejecuta `npm run dev`, recarga con Ctrl+Shift+R y vuelve a iniciar sesión.

Si el backend no compila por la versión de Spring Boot, cambia `3.5.16` por `3.3.13` en el `pom.xml` (primera línea
`<version>` del bloque `<parent>`) y avísame.

## Comprobar la seguridad (evidencia para tu informe)
Con el backend corriendo, desde la carpeta donde guardes `pruebas-seguridad.ps1`:

    powershell -ExecutionPolicy Bypass -File .\pruebas-seguridad.ps1

Muestra una lista de pruebas con PASA o FALLA (sin sesión, token falso, clave débil, toma de cuenta, bloqueo por
intentos, orígenes no permitidos, errores sin detalles internos). Toma una captura de esa salida.

## Configuración opcional (variables de entorno)
- `JWT_SECRET`: clave de firma de sesiones (mínimo 32 caracteres). Sin ella se genera una al azar en cada arranque,
  lo que cierra las sesiones al reiniciar el backend (la app te lleva al login). No la subas a GitHub.
- `DB_USER`, `DB_PASSWORD`: usuario de base de datos. Fuera de desarrollo usa `database/crear_usuario_bd.sql`.
- `CORS_ORIGINS` y `CORS_RED_LOCAL=false`: al publicar la app, indica su dirección y desactiva las redes privadas.

## Datos de inversión
Los datos de referencia están al 8 de noviembre de 2026 (tasa del Banco Central 4,5%, inflación 4,1%, tasas de depósitos
de tasas.cl, reglas del APV y desempeño del IPSA). Para actualizarlos, edita
`src/main/resources/db/datos_inversion.sql` (y `app.inversion.tpm` e `inflacion` en `application.properties`) y reinicia.

## Pendiente (no incluido)
HTTPS al publicar (hoy todo viaja por HTTP en tu red), registrar pagos de deuda, y editar o eliminar registros.
