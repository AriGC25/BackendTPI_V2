# Script de Verificación y Reinicio de Servicios

Write-Host "=== Verificando estado de servicios ===" -ForegroundColor Cyan

# Verificar si Docker está corriendo
$dockerRunning = docker ps 2>$null
if (-not $dockerRunning) {
    Write-Host "ERROR: Docker no está corriendo" -ForegroundColor Red
    exit 1
}

# Verificar servicios
Write-Host "`nServicios activos:" -ForegroundColor Green
docker ps --filter "name=transportista" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

# Preguntar si reiniciar
Write-Host "`n¿Deseas reiniciar los servicios de logística y solicitudes? (S/N)" -ForegroundColor Yellow
$respuesta = Read-Host

if ($respuesta -eq "S" -or $respuesta -eq "s") {
    Write-Host "`nReiniciando servicios..." -ForegroundColor Cyan

    Set-Location "C:\Users\Ariana\Desktop\TP_V4\BackendTPI-main"

    docker-compose restart logistica-service solicitudes-service

    Write-Host "`nEsperando 10 segundos para que los servicios inicien..." -ForegroundColor Yellow
    Start-Sleep -Seconds 10

    Write-Host "`nVerificando logs de logistica-service:" -ForegroundColor Green
    docker-compose logs --tail=20 logistica-service

    Write-Host "`nVerificando logs de solicitudes-service:" -ForegroundColor Green
    docker-compose logs --tail=20 solicitudes-service

    Write-Host "`n=== Servicios reiniciados ===" -ForegroundColor Green
    Write-Host "Ahora prueba el endpoint de asignar camión en Postman" -ForegroundColor Yellow
} else {
    Write-Host "`nNo se reiniciaron los servicios" -ForegroundColor Yellow
}

Write-Host "`nPRUEBA ESTO EN POSTMAN:" -ForegroundColor Cyan
Write-Host "1. Login como OPERADOR" -ForegroundColor White
Write-Host "2. PUT http://localhost:8000/tramos/{tramo_id}/asignar-camion?camionId=1&transportistaId=transportista1" -ForegroundColor White
Write-Host "   (Reemplaza {tramo_id} con el ID real del tramo)" -ForegroundColor Gray

