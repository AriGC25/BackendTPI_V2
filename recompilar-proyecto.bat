@echo off
echo ========================================
echo Recompilando proyecto Backend TPI
echo ========================================
echo.

cd /d "%~dp0"

echo Limpiando compilaciones anteriores...
mvn clean

echo.
echo Compilando todos los servicios...
mvn package -DskipTests

echo.
echo ========================================
echo Compilacion completada!
echo Los archivos JAR estan en target/ de cada servicio
echo ========================================
pause
