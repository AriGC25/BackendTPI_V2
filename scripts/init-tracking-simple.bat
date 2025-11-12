@echo off
echo ================================================
echo Inicializando eventos de tracking...
echo ================================================
echo.

REM Leer el archivo SQL y ejecutarlo en PostgreSQL
docker exec -i transportista-postgres psql -U postgres -d transportista_db < "%~dp0..\database\init-scripts\04-populate-tracking-events.sql"

echo.
echo ================================================
echo Verificando eventos creados...
echo ================================================
docker exec transportista-postgres psql -U postgres -d transportista_db -c "SELECT COUNT(*) as Total_Eventos FROM tracking_eventos;"

echo.
echo ================================================
echo Mostrando algunos eventos de ejemplo...
echo ================================================
docker exec transportista-postgres psql -U postgres -d transportista_db -c "SELECT id, contenedor_id, solicitud_id, estado, LEFT(ubicacion, 30) as ubicacion, fecha_evento FROM tracking_eventos ORDER BY fecha_evento DESC LIMIT 5;"

echo.
echo Proceso completado!
pause
