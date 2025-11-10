# ============================================
# Script de Diagnóstico de Keycloak
# ============================================

$ErrorActionPreference = "Continue"

Write-Host "================================================" -ForegroundColor Cyan
Write-Host "   DIAGNÓSTICO DE KEYCLOAK" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""

# ============================================
# Test 1: Verificar que el contenedor está corriendo
# ============================================
Write-Host "Test 1: Estado del contenedor de Keycloak" -ForegroundColor Yellow
Write-Host ""

try {
    $containerInfo = docker ps --filter "name=keycloak" --format "{{.Names}}\t{{.Status}}\t{{.Ports}}"
    if ($containerInfo) {
        Write-Host "  ✓ Contenedor encontrado:" -ForegroundColor Green
        Write-Host "    $containerInfo" -ForegroundColor Gray
    } else {
        Write-Host "  ✗ Contenedor NO encontrado o detenido" -ForegroundColor Red
    }
} catch {
    Write-Host "  ✗ Error al verificar contenedor: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "================================================" -ForegroundColor Cyan
Start-Sleep -Seconds 2

# ============================================
# Test 2: Verificar conectividad básica
# ============================================
Write-Host "Test 2: Conectividad HTTP a localhost:8080" -ForegroundColor Yellow
Write-Host ""

try {
    Write-Host "  Intentando conectar a http://localhost:8080..." -ForegroundColor Gray
    $response = Invoke-WebRequest -Uri "http://localhost:8080" -Method Get -TimeoutSec 10 -UseBasicParsing -ErrorAction Stop
    Write-Host "  ✓ Keycloak responde en localhost:8080" -ForegroundColor Green
    Write-Host "    Status Code: $($response.StatusCode)" -ForegroundColor Gray
} catch {
    Write-Host "  ✗ NO se pudo conectar a Keycloak" -ForegroundColor Red
    Write-Host "    Error: $($_.Exception.Message)" -ForegroundColor Red

    if ($_.Exception.Response) {
        $statusCode = $_.Exception.Response.StatusCode.value__
        Write-Host "    Status Code: $statusCode" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "================================================" -ForegroundColor Cyan
Start-Sleep -Seconds 2

# ============================================
# Test 3: Verificar endpoint del realm
# ============================================
Write-Host "Test 3: Verificar endpoint del realm transportista-realm" -ForegroundColor Yellow
Write-Host ""

try {
    Write-Host "  Intentando acceder al realm..." -ForegroundColor Gray
    $realmUrl = "http://localhost:8080/realms/transportista-realm"
    $response = Invoke-RestMethod -Uri $realmUrl -Method Get -TimeoutSec 10 -ErrorAction Stop
    Write-Host "  ✓ Realm accesible" -ForegroundColor Green
    Write-Host "    Realm: $($response.realm)" -ForegroundColor Gray
    if ($response.issuer) {
        Write-Host "    Issuer: $($response.issuer)" -ForegroundColor Gray
    }
} catch {
    Write-Host "  ✗ NO se pudo acceder al realm" -ForegroundColor Red
    Write-Host "    Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "================================================" -ForegroundColor Cyan
Start-Sleep -Seconds 2

# ============================================
# Test 4: Verificar endpoint de token
# ============================================
Write-Host "Test 4: Verificar endpoint de obtención de token" -ForegroundColor Yellow
Write-Host ""

$tokenUrl = "http://localhost:8080/realms/transportista-realm/protocol/openid-connect/token"
Write-Host "  URL del token: $tokenUrl" -ForegroundColor Gray

try {
    Write-Host "  Intentando obtener token para operador1..." -ForegroundColor Gray

    $body = @{
        client_id = "backend-client"
        client_secret = "backend-client-secret"
        grant_type = "password"
        username = "operador1"
        password = "operador123"
    }

    $response = Invoke-RestMethod -Uri $tokenUrl -Method Post -Body $body -ContentType "application/x-www-form-urlencoded" -TimeoutSec 15 -ErrorAction Stop

    $token = $response.access_token
    Write-Host "  ✓ Token obtenido exitosamente" -ForegroundColor Green
    Write-Host "    Longitud del token: $($token.Length) caracteres" -ForegroundColor Gray
    Write-Host "    Tipo de token: $($response.token_type)" -ForegroundColor Gray
    Write-Host "    Expira en: $($response.expires_in) segundos" -ForegroundColor Gray

    # Guardar el token para usar después
    $global:OPERADOR_TOKEN = $token

} catch {
    Write-Host "  ✗ NO se pudo obtener el token" -ForegroundColor Red
    Write-Host "    Error: $($_.Exception.Message)" -ForegroundColor Red

    if ($_.Exception.Response) {
        $statusCode = $_.Exception.Response.StatusCode.value__
        Write-Host "    Status Code: $statusCode" -ForegroundColor Red
    }

    if ($_.ErrorDetails.Message) {
        Write-Host "    Detalles: $($_.ErrorDetails.Message)" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "================================================" -ForegroundColor Cyan
Start-Sleep -Seconds 2

# ============================================
# Test 5: Probar token con un endpoint protegido
# ============================================
if ($global:OPERADOR_TOKEN) {
    Write-Host "Test 5: Probar token con Gateway" -ForegroundColor Yellow
    Write-Host ""

    try {
        Write-Host "  Probando endpoint /camiones a través del Gateway..." -ForegroundColor Gray

        $headers = @{
            Authorization = "Bearer $global:OPERADOR_TOKEN"
            "Content-Type" = "application/json"
        }

        $response = Invoke-RestMethod -Uri "http://localhost:8000/camiones" -Method Get -Headers $headers -TimeoutSec 10 -ErrorAction Stop

        Write-Host "  ✓ Endpoint respondió correctamente" -ForegroundColor Green
        Write-Host "    Camiones encontrados: $($response.Count)" -ForegroundColor Gray

    } catch {
        Write-Host "  ✗ Error al llamar al endpoint" -ForegroundColor Red
        Write-Host "    Error: $($_.Exception.Message)" -ForegroundColor Red

        if ($_.Exception.Response) {
            $statusCode = $_.Exception.Response.StatusCode.value__
            Write-Host "    Status Code: $statusCode" -ForegroundColor Red
        }
    }

    Write-Host ""
    Write-Host "================================================" -ForegroundColor Cyan
}

# ============================================
# Test 6: Verificar logs recientes de Keycloak
# ============================================
Write-Host "Test 6: Últimas líneas de logs de Keycloak" -ForegroundColor Yellow
Write-Host ""

try {
    Write-Host "  Obteniendo últimos logs..." -ForegroundColor Gray
    $logs = docker logs transportista-keycloak --tail 5 2>&1
    Write-Host $logs -ForegroundColor Gray
} catch {
    Write-Host "  ✗ Error al obtener logs: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "================================================" -ForegroundColor Cyan

# ============================================
# RESUMEN
# ============================================
Write-Host ""
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "   RESUMEN DEL DIAGNÓSTICO" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Si todos los tests pasaron:" -ForegroundColor Yellow
Write-Host "  ✓ Keycloak está accesible y funcionando correctamente" -ForegroundColor Green
Write-Host ""
Write-Host "Si el Test 2 o 3 falló:" -ForegroundColor Yellow
Write-Host "  -> Keycloak no es accesible desde localhost:8080" -ForegroundColor Gray
Write-Host "  -> Verifica que el contenedor esté corriendo" -ForegroundColor Gray
Write-Host "  -> Verifica el mapeo de puertos en docker-compose.yml" -ForegroundColor Gray
Write-Host ""
Write-Host "Si el Test 4 falló:" -ForegroundColor Yellow
Write-Host "  -> Problema de autenticación" -ForegroundColor Gray
Write-Host "  -> Verifica que el realm y los usuarios estén creados" -ForegroundColor Gray
Write-Host ""
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Presiona cualquier tecla para cerrar..." -ForegroundColor Yellow
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")

