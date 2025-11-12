@echo off
REM Script para inicializar eventos de tracking en la base de datos
REM Ejecutar este script cuando Docker esté corriendo

echo ================================================
echo Inicializando eventos de tracking...
echo ================================================
echo.

REM Ejecutar el script SQL en el contenedor de MySQL
docker exec -i backendtpi-main-mysql-1 mysql -uroot -prootpassword transportista_db < C:\Users\Ariana\Desktop\TP_V4\BackendTPI-main\database\init-scripts\04-populate-tracking-events.sql

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ================================================
    echo Eventos de tracking inicializados correctamente
    echo ================================================
) else (
    echo.
    echo ================================================
    echo Error al inicializar eventos de tracking
    echo Asegurate de que Docker este corriendo
    echo ================================================
)

pause

