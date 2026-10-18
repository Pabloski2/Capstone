@echo off
title Financial Beacon
color 0B
echo ============================================
echo   Financial Beacon
echo ============================================
echo.
echo IMPORTANTE: el backend debe estar corriendo
echo (F5 en VS Code, en la carpeta financialbeacon-backend)
echo antes de continuar.
echo.
pause

echo.
echo Iniciando el servidor del frontend...
start "Financial Beacon - Servidor (no cerrar)" cmd /k "npm run dev"

echo Esperando a que el servidor levante...
timeout /t 6 /nobreak > nul

echo Abriendo Financial Beacon como aplicacion...
start msedge --app=http://localhost:5173 --window-size=430,900

exit
