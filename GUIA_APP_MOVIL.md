# Financial Beacon: de código a APK

La app web (React + Vite) está empaquetada con **Capacitor**. El proyecto Android ya viene generado en `financialbeacon-frontend/android` (con ícono y pantalla de inicio). Solo falta publicar el backend y compilar.

> En el celular, `localhost` es el propio celular. Por eso el backend debe estar en internet con **HTTPS** antes de generar el APK.

## 1. Publicar el backend

Necesitas un servicio que ejecute Docker (Render, Railway, Fly.io…) y una base PostgreSQL.

1. Crea la base PostgreSQL y anota su URL JDBC: `jdbc:postgresql://HOST:5432/financialbeacon`.
2. Despliega `financialbeacon-backend` (trae `Dockerfile`) con estas variables de entorno:

| Variable | Valor |
|---|---|
| `DATABASE_URL` | URL JDBC de PostgreSQL |
| `DATABASE_USER` / `DATABASE_PASSWORD` | credenciales de la base |
| `JWT_SECRET` | texto aleatorio de 32+ caracteres (sin él el servidor no arranca) |
| `BREVO_API_KEY` y `MAIL_FROM` | para enviar correos (ver sección "Correo") |
| `SOPORTE_EMAIL` | opcional; bandeja que recibe las solicitudes de soporte (por defecto, `MAIL_FROM`) |
| `CORS_ORIGINS` | opcional; por defecto acepta la app móvil |

3. Comprueba que `https://TU-BACKEND/health` responde `{"estado":"ok"}`.

### Correo (recuperación de contraseña y soporte)

El backend envía correos de dos formas, según la variable `MAIL_PROVIDER`:

| Proveedor | Cuándo usarlo | Variables |
|---|---|---|
| `brevo` (por defecto en producción) | **Railway** (planes Trial, Free y Hobby) y cualquier hosting | `BREVO_API_KEY`, `MAIL_FROM` |
| `smtp` | Hostings que permiten SMTP (Railway solo con plan Pro) | `MAIL_USERNAME`, `MAIL_PASSWORD` |

**Railway bloquea el SMTP (también el de Gmail) fuera del plan Pro.** Por eso en Railway se usa Brevo, que funciona por HTTPS y tiene plan gratuito de 300 correos al día, sin tarjeta.

Configurar Brevo:
1. Crea una cuenta gratis en https://www.brevo.com y confirma tu correo.
2. Agrega un **remitente** (Senders) con el correo desde el que quieres enviar y confirma el correo que Brevo te manda. Ese correo es tu `MAIL_FROM`.
3. Crea una **API key** (SMTP & API > API Keys) y úsala como `BREVO_API_KEY`.
4. Opcional: `SOPORTE_EMAIL` (donde llegan las solicitudes de soporte; por defecto, `MAIL_FROM`).

Si el remitente es una dirección `@gmail.com`, algunos correos pueden caer en spam. Con un dominio propio autenticado en Brevo (SPF/DKIM) la entrega mejora.

Con `MAIL_PROVIDER=smtp` y Gmail: activa la verificación en 2 pasos, crea una contraseña de aplicación en https://myaccount.google.com/apppasswords y úsala como `MAIL_PASSWORD`.

**En producción, si faltan las credenciales del proveedor elegido, el servidor no arranca** (a propósito). **En desarrollo** (sin credenciales) no se envía nada: el correo, con el código, se escribe en la consola del servidor.

Prueba: en la app pulsa "¿Olvidaste tu contraseña?" con un correo registrado y verifica que llegue el código (revisa spam). Si no llega, los logs del servidor muestran el motivo.

## 2. Generar el APK

Necesitas **Node 22+** y **Android Studio** (incluye el JDK y el SDK de Android).

```bash
cd financialbeacon-frontend
cp .env.example .env.production     # edita VITE_API_URL: https://tu-backend/api
npm install
npm run android                     # compila, sincroniza y abre Android Studio
```

`npm run android` se detiene con un mensaje claro si `VITE_API_URL` falta o no es HTTPS, para no generar una app rota.

En Android Studio, espera a que termine "Gradle sync" y luego:

- **Para probar en tu teléfono:** Build > Build Bundle(s) / APK(s) > **Build APK(s)**. Pasa el `.apk` al teléfono y ábrelo (Android pedirá permitir instalar apps de origen desconocido).
- **Para Google Play:** Build > **Generate Signed Bundle / APK** > Android App Bundle. Crea un *keystore* y **guárdalo junto a su contraseña en un lugar seguro**: sin él no podrás actualizar la app. No lo subas al repositorio.

Cada vez que cambies el frontend, vuelve a correr `npm run android`. Para una nueva versión en Play, sube `versionCode` en `android/app/build.gradle`.

## 3. Probar en desarrollo (sin publicar)

- Backend: `mvn spring-boot:run` (usa H2 en `./data`).
- Frontend: `npm run dev` y abre `http://IP-DE-TU-PC:5173` desde el celular en la misma red.

## Para publicar en Google Play

- Política de privacidad pública: usa `POLITICA_PRIVACIDAD.md` como plantilla, complétala y publícala en una URL.
- La app ya permite eliminar la cuenta desde "Mi cuenta y privacidad" (Play lo exige).
- Cuenta de desarrollador de Google Play (pago único) y fichas de la tienda: capturas, descripción, clasificación de contenido.
- iOS: `npx cap add ios` y `npm run ios` (requiere Mac con Xcode y cuenta Apple Developer).

## Limitaciones conocidas

- Los códigos de recuperación dependen de que el correo esté bien configurado; si el proveedor rechaza el envío, el usuario no recibe nada (revisa los logs del servidor).
- Las solicitudes de soporte se responden desde la bandeja de correo; no hay panel de administración para cambiar su estado.
- La foto de perfil se guarda en la base de datos (reducida a 256 px); para muchos usuarios conviene un almacenamiento de archivos.
- El token de sesión se guarda en el almacenamiento de la app (privado de la app, pero no cifrado con hardware).
- `ddl-auto=update` crea las tablas solas; para una versión más madura conviene Flyway o Liquibase.
- El límite de intentos de login vive en memoria: con varias instancias del backend habría que moverlo a la base de datos.
- Los gastos y deudas no se editan, se eliminan y se vuelven a crear.
