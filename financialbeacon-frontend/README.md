# Financial Beacon - Frontend (Paso 4-5: Login + Registro + Dashboard)

## Requisitos

- Node.js 18 o superior instalado (verifica con `node -v` en la terminal).
- El backend (`financialbeacon-backend`) corriendo en `http://localhost:8080`.

## Cómo correrlo

1. Abre esta carpeta en VS Code (`Archivo > Abrir carpeta...`), en una ventana **separada** de la del backend (o usa dos ventanas de VS Code, una para cada proyecto).
2. Abre una terminal (`Terminal > New Terminal`) y corre:
   ```
   npm install
   ```
   Esto descarga las dependencias (React, React Router, Vite). Puede tardar uno o dos minutos la primera vez.
3. Con el backend ya corriendo en otra ventana, ejecuta:
   ```
   npm run dev
   ```
4. Verás algo como `Local: http://localhost:5173/`. Ábrelo en el navegador.

## Qué probar

1. Ve a `http://localhost:5173/register`, crea una cuenta nueva (o usa una que ya tengas).
2. Deberías caer en el Dashboard real: 5 tarjetas de resumen (ingreso, gastos por categoría, ahorro estimado), un formulario para cargar el ingreso del mes, otro para agregar gastos, y la tabla de gastos del mes.
3. Prueba cargar un ingreso, agregar un par de gastos con distintas categorías y confirma que el "Ahorro estimado" se actualiza solo.
4. Dale a "Cerrar sesión" y prueba el login en `http://localhost:5173/login` con esa misma cuenta — el Dashboard debería mostrar los mismos datos que cargaste (persisten en la base de datos del backend).

## Ver la app en el celular (mismo WiFi)

1. Con `npm run dev` corriendo, busca la IP local de tu compu: `ipconfig` en PowerShell, busca "Dirección IPv4" (algo como `192.168.1.X`).
2. En el celular, conectado a la misma red WiFi, entra a `http://192.168.1.X:5173` en el navegador.
3. Ojo: el login no va a funcionar desde el celular todavía, porque el backend solo acepta peticiones de `http://localhost:5173` (ver `@CrossOrigin` en los controllers). Lo ajustamos cuando lleguemos al paso de despliegue/pruebas en celular.

## Siguiente paso

Agregar la pantalla de Gestión de Deudas (DebtManager), conectada a `/api/deudas` y `/api/deudas/estrategia`.
