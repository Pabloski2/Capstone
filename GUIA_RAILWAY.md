# Desplegar el backend en Railway

Resumen: 1 proyecto con 2 servicios (PostgreSQL + el backend desde tu repo de GitHub).

## 1. Subir el código a GitHub
```bash
git add -A
git commit -m "App móvil: seguridad, cuenta, soporte y despliegue"
git push
```

## 2. Crear el proyecto
1. railway.com > **New Project** > **Deploy PostgreSQL**. Se crea un servicio llamado `Postgres`.
2. En el mismo proyecto: **New > GitHub Repo** > elige tu repositorio (autoriza a Railway si te lo pide).
3. Abre ese servicio > **Settings** > **Root Directory** = `/financialbeacon-backend`. Railway detecta el `Dockerfile` solo.

## 3. Variables del backend (pestaña Variables > Raw Editor)
```
DATABASE_URL=jdbc:postgresql://${{Postgres.PGHOST}}:${{Postgres.PGPORT}}/${{Postgres.PGDATABASE}}
DATABASE_USER=${{Postgres.PGUSER}}
DATABASE_PASSWORD=${{Postgres.PGPASSWORD}}
JWT_SECRET=<texto aleatorio de 40+ caracteres>
MAIL_PROVIDER=brevo
BREVO_API_KEY=<tu api key de Brevo>
MAIL_FROM=<remitente verificado en Brevo>
SOPORTE_EMAIL=<correo que recibe soporte>
JAVA_TOOL_OPTIONS=-XX:MaxRAMPercentage=70
```
Si `Postgres` se llama distinto en tu proyecto, cambia ese nombre dentro de `${{ }}`.
Generar el `JWT_SECRET`: `openssl rand -base64 48`.

## 4. Dominio y comprobación
1. Servicio del backend > **Settings > Networking > Generate Domain**.
2. Abre `https://TU-DOMINIO.up.railway.app/health`: debe responder `{"estado":"ok"}`.
3. Tu `VITE_API_URL` para la app es `https://TU-DOMINIO.up.railway.app/api`.

## Si algo falla
- **Build falla:** revisa que el Root Directory sea `/financialbeacon-backend`.
- **No arranca con "Correo sin configurar":** faltan `BREVO_API_KEY` o `MAIL_FROM`.
- **No arranca con "Could not resolve placeholder":** falta alguna variable de la lista.
- **Se reinicia por memoria:** el plan Free da 0,5 GB de RAM, justo para Java; el plan Hobby da más holgura.
- **No llegan los correos:** mira los logs del servicio; los errores de Brevo aparecen ahí.
