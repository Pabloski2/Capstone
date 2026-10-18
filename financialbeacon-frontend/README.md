# Financial Beacon - Frontend (Paso 4-6: Login + Registro + Dashboard + Deudas)

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

## Ver la app en el celular e instalarla como aplicación (PWA)

1. Levanta el backend (`F5` en VS Code) y el frontend (`npm run dev`) como siempre, en tu PC.
2. Busca la IP local de tu PC: en PowerShell, `ipconfig`, busca "Dirección IPv4" (algo como `192.168.1.X`). Debe ser la misma red WiFi en tu celular y tu PC.
3. En el celular, abre el navegador (Chrome en Android, Safari en iPhone) y entra a `http://192.168.1.X:5173` (con tu IP real).
4. **Android (Chrome):** te va a aparecer un aviso o un ícono de menú (⋮) con la opción **"Instalar app"** o **"Añadir a pantalla de inicio"**. Tócalo.
5. **iPhone (Safari):** toca el botón de compartir (el cuadrado con la flecha hacia arriba) → **"Añadir a pantalla de inicio"**.
6. Va a quedar un ícono de Financial Beacon en tu pantalla de inicio. Al abrirlo, carga en pantalla completa, sin barra de navegador — se ve y se siente como una app instalada.

**Importante:** el backend y el frontend tienen que seguir corriendo en tu PC para que la app del celular funcione (no es una app 100% independiente todavía, depende de tu PC como servidor). Si cambias de red WiFi, la IP cambia y hay que reinstalar apuntando a la IP nueva.

## Siguiente paso

Agregar la pantalla de Gestión de Deudas (DebtManager), conectada a `/api/deudas` y `/api/deudas/estrategia`.
