# ============================================
# Script de Prueba SIMPLIFICADO - OPERADOR
# ============================================

$ErrorActionPreference = "Continue"

Write-Host "================================================" -ForegroundColor Cyan
Write-Host "   TEST SIMPLIFICADO - ENDPOINTS OPERADOR" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""

# ============================================
# Test 1: Verificar conectividad básica
# ============================================
Write-Host "Test 1: Verificar que los servicios respondan" -ForegroundColor Yellow
Write-Host ""

# Test Keycloak
Write-Host "  Probando Keycloak (puerto 8080)..." -ForegroundColor Gray
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080" -Method Get -TimeoutSec 5 -ErrorAction Stop
    Write-Host "  ✓ Keycloak responde" -ForegroundColor Green
} catch {
    Write-Host "  ✗ Keycloak NO responde" -ForegroundColor Red
    Write-Host "    Error: $($_.Exception.Message)" -ForegroundColor Red
}

# Test Gateway
Write-Host "  Probando Gateway (puerto 8000)..." -ForegroundColor Gray
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8000/actuator/health" -Method Get -TimeoutSec 5 -ErrorAction Stop
    Write-Host "  ✓ Gateway responde" -ForegroundColor Green
} catch {
    Write-Host "  ✗ Gateway NO responde" -ForegroundColor Red
    Write-Host "    Error: $($_.Exception.Message)" -ForegroundColor Red
}

# Test Logística
Write-Host "  Probando Logística (puerto 8082)..." -ForegroundColor Gray
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8082/actuator/health" -Method Get -TimeoutSec 5 -ErrorAction Stop
    Write-Host "  ✓ Logística responde" -ForegroundColor Green
} catch {
    Write-Host "  ✗ Logística NO responde" -ForegroundColor Red
    Write-Host "    Error: $($_.Exception.Message)" -ForegroundColor Red
}

# Test Tarifas
Write-Host "  Probando Tarifas (puerto 8083)..." -ForegroundColor Gray
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8083/actuator/health" -Method Get -TimeoutSec 5 -ErrorAction Stop
    Write-Host "  ✓ Tarifas responde" -ForegroundColor Green
} catch {
    Write-Host "  ✗ Tarifas NO responde" -ForegroundColor Red
    Write-Host "    Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "================================================" -ForegroundColor Cyan
Start-Sleep -Seconds 2

# ============================================
# Test 2: Obtener Token
# ============================================
Write-Host "Test 2: Obtener Token de OPERADOR" -ForegroundColor Yellow
Write-Host ""

$TOKEN = $null

try {
    Write-Host "  Llamando a Keycloak..." -ForegroundColor Gray

    $body = "client_id=backend-client&client_secret=backend-client-secret&grant_type=password&username=operador1&password=operador123"

    $response = Invoke-RestMethod -Uri "http://localhost:8080/realms/transportista-realm/protocol/openid-connect/token" -Method Post -Body $body -ContentType "application/x-www-form-urlencoded" -ErrorAction Stop

    $TOKEN = $response.access_token
    Write-Host "  ✓ Token obtenido" -ForegroundColor Green
    Write-Host "    Longitud del token: $($TOKEN.Length) caracteres" -ForegroundColor Gray
} catch {
    Write-Host "  ✗ ERROR al obtener token" -ForegroundColor Red
    Write-Host "    Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "================================================" -ForegroundColor Cyan
Start-Sleep -Seconds 2

# ============================================
# Test 3: Crear Camión (DIRECTO - Sin token)
# ============================================
Write-Host "Test 3: Crear Camión en Logística (Directo - SIN token)" -ForegroundColor Yellow
Write-Host ""

try {
    Write-Host "  Preparando datos del camión..." -ForegroundColor Gray

    $camion = @{
        patente = "ABC123"
        modelo = "Mercedes-Benz Actros"
        capacidadPeso = 25000.0
        capacidadVolumen = 80.0
        disponible = $true
    }

    $json = $camion | ConvertTo-Json
    Write-Host "  JSON: $json" -ForegroundColor Gray

    Write-Host "  Llamando a http://localhost:8082/camiones" -ForegroundColor Gray

    $response = Invoke-RestMethod -Uri "http://localhost:8082/camiones" -Method Post -Body $json -ContentType "application/json" -ErrorAction Stop

    Write-Host "  ✓ Camión creado exitosamente" -ForegroundColor Green
    Write-Host "    ID: $($response.id)" -ForegroundColor Gray
    Write-Host "    Patente: $($response.patente)" -ForegroundColor Gray
    Write-Host "    Modelo: $($response.modelo)" -ForegroundColor Gray
} catch {
    Write-Host "  ✗ ERROR al crear camión" -ForegroundColor Red
    if ($_.Exception.Response) {
        $statusCode = $_.Exception.Response.StatusCode.value__
        Write-Host "    StatusCode: $statusCode" -ForegroundColor Red
    }
    Write-Host "    Error: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.ErrorDetails.Message) {
        Write-Host "    Detalles: $($_.ErrorDetails.Message)" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "================================================" -ForegroundColor Cyan
Start-Sleep -Seconds 2

# ============================================
# Test 4: Listar Camiones (DIRECTO)
# ============================================
Write-Host "Test 4: Listar Camiones en Logística (Directo)" -ForegroundColor Yellow
Write-Host ""

try {
    Write-Host "  Llamando a http://localhost:8082/camiones" -ForegroundColor Gray

    $response = Invoke-RestMethod -Uri "http://localhost:8082/camiones" -Method Get -ErrorAction Stop

    Write-Host "  ✓ Camiones listados exitosamente" -ForegroundColor Green
    Write-Host "    Total: $($response.Count) camiones" -ForegroundColor Gray

    if ($response.Count -gt 0) {
        foreach ($camion in $response) {
            Write-Host "      - ID: $($camion.id) | Patente: $($camion.patente)" -ForegroundColor Gray
        }
    }
} catch {
    Write-Host "  ✗ ERROR al listar camiones" -ForegroundColor Red
    if ($_.Exception.Response) {
        $statusCode = $_.Exception.Response.StatusCode.value__
        Write-Host "    StatusCode: $statusCode" -ForegroundColor Red
    }
    Write-Host "    Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "================================================" -ForegroundColor Cyan
Start-Sleep -Seconds 2

# ============================================
# Test 5: Crear Camión a través del GATEWAY
# ============================================
Write-Host "Test 5: Crear Camión a través del Gateway (SIN token)" -ForegroundColor Yellow
Write-Host ""

try {
    Write-Host "  Preparando datos del camión..." -ForegroundColor Gray

    $camion = @{
        patente = "XYZ789"
        modelo = "Volvo FH16"
        capacidadPeso = 30000.0
        capacidadVolumen = 90.0
        disponible = $true
    }

    $json = $camion | ConvertTo-Json

    Write-Host "  Llamando a http://localhost:8000/camiones" -ForegroundColor Gray

    $response = Invoke-RestMethod -Uri "http://localhost:8000/camiones" -Method Post -Body $json -ContentType "application/json" -ErrorAction Stop

    Write-Host "  ✓ Camión creado exitosamente (GATEWAY)" -ForegroundColor Green
    Write-Host "    ID: $($response.id)" -ForegroundColor Gray
    Write-Host "    Patente: $($response.patente)" -ForegroundColor Gray
} catch {
    Write-Host "  ✗ ERROR al crear camión (GATEWAY)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $statusCode = $_.Exception.Response.StatusCode.value__
        Write-Host "    StatusCode: $statusCode" -ForegroundColor Red
    }
    Write-Host "    Error: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.ErrorDetails.Message) {
        Write-Host "    Detalles: $($_.ErrorDetails.Message)" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "================================================" -ForegroundColor Cyan
Start-Sleep -Seconds 2

# ============================================
# Test 6: Listar Camiones a través del GATEWAY
# ============================================
Write-Host "Test 6: Listar Camiones a través del Gateway" -ForegroundColor Yellow
Write-Host ""

try {
    Write-Host "  Llamando a http://localhost:8000/camiones" -ForegroundColor Gray

    $response = Invoke-RestMethod -Uri "http://localhost:8000/camiones" -Method Get -ErrorAction Stop

    Write-Host "  ✓ Camiones listados exitosamente (GATEWAY)" -ForegroundColor Green
    Write-Host "    Total: $($response.Count) camiones" -ForegroundColor Gray

    if ($response.Count -gt 0) {
        foreach ($camion in $response) {
            Write-Host "      - ID: $($camion.id) | Patente: $($camion.patente)" -ForegroundColor Gray
        }
    }
} catch {
    Write-Host "  ✗ ERROR al listar camiones (GATEWAY)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $statusCode = $_.Exception.Response.StatusCode.value__
        Write-Host "    StatusCode: $statusCode" -ForegroundColor Red
    }
    Write-Host "    Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "================================================" -ForegroundColor Cyan
Start-Sleep -Seconds 2

# ============================================
# Test 7: Crear Depósito (DIRECTO)
# ============================================
Write-Host "Test 7: Crear Depósito en Logística (Directo)" -ForegroundColor Yellow
Write-Host ""

try {
    Write-Host "  Preparando datos del depósito..." -ForegroundColor Gray

    $deposito = @{
        nombre = "Depósito Central"
        direccion = "Av. Libertador 1000, CABA"
        latitud = -34.5875
        longitud = -58.4189
        capacidadMaxima = 50000.0
        activo = $true
    }

    $json = $deposito | ConvertTo-Json

    Write-Host "  Llamando a http://localhost:8082/depositos" -ForegroundColor Gray

    $response = Invoke-RestMethod -Uri "http://localhost:8082/depositos" -Method Post -Body $json -ContentType "application/json" -ErrorAction Stop

    Write-Host "  ✓ Depósito creado exitosamente" -ForegroundColor Green
    Write-Host "    ID: $($response.id)" -ForegroundColor Gray
    Write-Host "    Nombre: $($response.nombre)" -ForegroundColor Gray
} catch {
    Write-Host "  ✗ ERROR al crear depósito" -ForegroundColor Red
    if ($_.Exception.Response) {
        $statusCode = $_.Exception.Response.StatusCode.value__
        Write-Host "    StatusCode: $statusCode" -ForegroundColor Red
    }
    Write-Host "    Error: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.ErrorDetails.Message) {
        Write-Host "    Detalles: $($_.ErrorDetails.Message)" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "================================================" -ForegroundColor Cyan
Start-Sleep -Seconds 2

# ============================================
# Test 8: Crear Tarifa (DIRECTO)
# ============================================
Write-Host "Test 8: Crear Tarifa en Tarifas Service (Directo)" -ForegroundColor Yellow
Write-Host ""

try {
    Write-Host "  Preparando datos de la tarifa..." -ForegroundColor Gray

    $tarifa = @{
        nombre = "Tarifa Estándar 2025"
        descripcion = "Tarifa base"
        costoPorKm = 150.0
        costoBaseCombustible = 500.0
        costoEstadia = 1000.0
        factorPeso = 0.05
        factorVolumen = 0.03
        activa = $true
    }

    $json = $tarifa | ConvertTo-Json

    Write-Host "  Llamando a http://localhost:8083/tarifas" -ForegroundColor Gray

    $response = Invoke-RestMethod -Uri "http://localhost:8083/tarifas" -Method Post -Body $json -ContentType "application/json" -ErrorAction Stop

    Write-Host "  ✓ Tarifa creada exitosamente" -ForegroundColor Green
    Write-Host "    ID: $($response.id)" -ForegroundColor Gray
    Write-Host "    Nombre: $($response.nombre)" -ForegroundColor Gray
} catch {
    Write-Host "  ✗ ERROR al crear tarifa" -ForegroundColor Red
    if ($_.Exception.Response) {
        $statusCode = $_.Exception.Response.StatusCode.value__
        Write-Host "    StatusCode: $statusCode" -ForegroundColor Red
    }
    Write-Host "    Error: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.ErrorDetails.Message) {
        Write-Host "    Detalles: $($_.ErrorDetails.Message)" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "================================================" -ForegroundColor Cyan

# ============================================
# RESUMEN
# ============================================
Write-Host ""
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "   PRUEBAS COMPLETADAS" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Revisa los resultados arriba:" -ForegroundColor Yellow
Write-Host "  ✓ = Funcionó" -ForegroundColor Green
Write-Host "  ✗ = Falló" -ForegroundColor Red
Write-Host ""
Write-Host "Si TODOS los tests de 'Directo' funcionan:" -ForegroundColor Yellow
Write-Host "  -> Los servicios están OK, el problema está en el Gateway" -ForegroundColor Gray
Write-Host ""
Write-Host "Si los tests de 'Directo' fallan:" -ForegroundColor Yellow
Write-Host "  -> El problema está en los servicios mismos" -ForegroundColor Gray
Write-Host ""
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Presiona cualquier tecla para cerrar..." -ForegroundColor Yellow
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")

