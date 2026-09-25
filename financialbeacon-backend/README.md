# Financial Beacon - Backend (Paso 1: Base + Autenticación)

## Cómo correrlo

1. Abre esta carpeta en VS Code (`Archivo > Abrir carpeta...`).
2. Espera a que la extensión de Java indique "cargando proyecto" y termine.
3. Abre `FinancialbeaconApplication.java` y presiona `F5`, o ejecuta en la terminal:
   ```
   mvn spring-boot:run
   ```
4. El servidor queda arriba en `http://localhost:8080`.

## Probar el registro

```
curl -X POST http://localhost:8080/api/auth/register -H "Content-Type: application/json" -d "{\"email\":\"test@test.com\",\"password\":\"1234\",\"nombre\":\"Pablo\"}"
```

## Probar el login

```
curl -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" -d "{\"email\":\"test@test.com\",\"password\":\"1234\"}"
```

## Consola H2 (ver los datos guardados)

`http://localhost:8080/h2-console` — JDBC URL: `jdbc:h2:mem:financialbeacon`, usuario `sa`, sin contraseña.

## Paso 2: Ingresos y Gastos

### Registrar el ingreso del mes (se puede volver a llamar para actualizarlo)

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/finanzas/ingreso" -Method Post -ContentType "application/json" -Body '{"userId":1,"monto":800000,"mes":9,"anio":2026}'
```

### Registrar un gasto

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/finanzas/gasto" -Method Post -ContentType "application/json" -Body '{"userId":1,"descripcion":"Arriendo","monto":300000,"categoria":"FIJO","fecha":"2026-09-05"}'
```

`categoria` acepta: `FIJO`, `VARIABLE`, `HORMIGA`.

### Ver el resumen del mes (lo que alimenta el dashboard)

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/finanzas/resumen?userId=1&mes=9&anio=2026" -Method Get
```

Devuelve: `ingresoMensual`, `totalGastosFijos`, `totalGastosVariables`, `totalGastosHormiga`, `totalGastos`, `ahorroEstimado`.

### Listar los gastos del mes

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/finanzas/gastos?userId=1&mes=9&anio=2026" -Method Get
```

## Paso 3: Gestión de Deudas

### Registrar una deuda

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/deudas" -Method Post -ContentType "application/json" -Body '{"userId":1,"nombre":"Tarjeta Falabella","montoTotal":500000,"tasaInteres":45.0,"cuotaMinima":50000}'
```

Registra un par más para que la estrategia tenga sentido, por ejemplo:

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/deudas" -Method Post -ContentType "application/json" -Body '{"userId":1,"nombre":"Crédito de consumo","montoTotal":1200000,"tasaInteres":22.0,"cuotaMinima":80000}'
```

### Ver la estrategia recomendada (necesita el resumen del mes ya cargado)

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/deudas/estrategia?userId=1&mes=9&anio=2026" -Method Get
```

Devuelve `estrategiaRecomendada` (`AVALANCHA` o `BOLA_DE_NIEVE`), la `justificacion` de por qué, el `montoDisponibleParaDeudas` (viene del ahorro estimado del mes) y el `ordenDePago` con cuánto pagarle a cada deuda este mes.

### Listar deudas

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/deudas?userId=1" -Method Get
```

## Siguiente paso

Con los 3 módulos del backend funcionando, seguimos con el frontend en React (Login, Dashboard, DebtManager) para poder verla en el navegador y el celular.
