@echo off
cd C:\Users\Ariana\Desktop\TP_V4\BackendTPI-main
docker-compose down
docker-compose up -d --build
echo.
echo El sistema se está levantando. Espera 2-3 minutos para que todos los servicios inicien.
echo.
docker ps

