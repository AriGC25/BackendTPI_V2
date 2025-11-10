# ============================================
# Script de Prueba de Endpoints del OPERADOR
# Gestión de Camiones, Depósitos y Tarifas
# ============================================

# Evitar que la terminal se cierre en caso de error
$ErrorActionPreference = "Continue"

Write-Host "================================================" -ForegroundColor Cyan
Write-Host "   SCRIPT DE PRUEBA - ENDPOINTS OPERADOR" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""

# Configuración - URLs completas y explícitas
$GATEWAY_URL = "http://localhost:8000"
$LOGISTICA_URL = "http://localhost:8082"
$TARIFAS_URL = "http://localhost:8083"
$KEYCLOAK_URL = "http://localhost:8080"

Write-Host "Configuración de URLs:" -ForegroundColor Cyan
Write-Host "  Gateway: $GATEWAY_URL" -ForegroundColor Gray
Write-Host "  Logística: $LOGISTICA_URL" -ForegroundColor Gray
Write-Host "  Tarifas: $TARIFAS_URL" -ForegroundColor Gray
Write-Host "  Keycloak: $KEYCLOAK_URL" -ForegroundColor Gray
Write-Host ""

# ============================================
# PASO 1: Obtener Token de OPERADOR
# ============================================
Write-Host "PASO 1: Obteniendo token de OPERADOR..." -ForegroundColor Yellow

$tokenBody = @{
    client_id = "backend-client"
    client_secret = "backend-client-secret"
    grant_type = "password"
    username = "operador1"
    password = "operador123"
}

$TOKEN = $null

try {
    $tokenUrl = $KEYCLOAK_URL + "/realms/transportista-realm/protocol/openid-connect/token"
    Write-Host "  URL: $tokenUrl" -ForegroundColor Gray

    $tokenResponse = Invoke-RestMethod -Uri $tokenUrl -Method Post -Body $tokenBody -ContentType "application/x-www-form-urlencoded" -ErrorAction Stop

    $TOKEN = $tokenResponse.access_token
    Write-Host "✓ Token obtenido exitosamente" -ForegroundColor Green
    $tokenPreview = $TOKEN.Substring(0, [Math]::Min(50, $TOKEN.Length))
    Write-Host "  Token: $tokenPreview..." -ForegroundColor Gray
} catch {
    Write-Host "✗ ERROR al obtener token:" -ForegroundColor Red
    Write-Host "  Message: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        Write-Host "  StatusCode: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
    }
    Write-Host "  Verifica que Keycloak esté corriendo en $KEYCLOAK_URL" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "CONTINUANDO SIN TOKEN (algunos tests pueden fallar)..." -ForegroundColor Yellow
}

Write-Host ""
Start-Sleep -Seconds 2

# ============================================
# SECCIÓN: PRUEBAS DE CAMIONES
# ============================================
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "   PRUEBAS DE GESTIÓN DE CAMIONES" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""

# --------------------------------------------
# Test 1: Crear Camión (SIN TOKEN - Directo al servicio)
# --------------------------------------------
Write-Host "Test 1: Crear Camión (Directo al servicio - Sin token)" -ForegroundColor Yellow

$camionData = @{
    patente = "ABC123"
    modelo = "Mercedes-Benz Actros 2651"
    capacidadPeso = 25000.0
    capacidadVolumen = 80.0
    disponible = $true
} | ConvertTo-Json

try {
    $url = $LOGISTICA_URL + "/camiones"
    Write-Host "  URL: $url" -ForegroundColor Gray

    $response = Invoke-RestMethod -Uri $url -Method Post -Body $camionData -ContentType "application/json" -ErrorAction Stop

    Write-Host "✓ Camión creado exitosamente (DIRECTO)" -ForegroundColor Green
    Write-Host "  ID: $($response.id)" -ForegroundColor Gray
    Write-Host "  Patente: $($response.patente)" -ForegroundColor Gray
    Write-Host "  Modelo: $($response.modelo)" -ForegroundColor Gray
    $CAMION_ID = $response.id
} catch {
    Write-Host "✗ ERROR al crear camión (DIRECTO):" -ForegroundColor Red
    if ($_.Exception.Response) {
        Write-Host "  StatusCode: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
    }
    Write-Host "  Message: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.ErrorDetails.Message) {
        Write-Host "  Details: $($_.ErrorDetails.Message)" -ForegroundColor Red
    }
}

Write-Host ""
Start-Sleep -Seconds 1

# --------------------------------------------
# Test 2: Crear Camión (CON TOKEN - Directo al servicio)
# --------------------------------------------
Write-Host "Test 2: Crear Camión (Directo al servicio - Con token)" -ForegroundColor Yellow

if ($null -eq $TOKEN) {
    Write-Host "⊘ SALTADO - No hay token disponible" -ForegroundColor Yellow
} else {
    $camionData2 = @{
        patente = "XYZ789"
        modelo = "Volvo FH16"
        capacidadPeso = 30000.0
        capacidadVolumen = 90.0
        disponible = $true
    } | ConvertTo-Json

    $headers = @{
        "Authorization" = "Bearer $TOKEN"
        "Content-Type" = "application/json"
    }

    try {
        $url = $LOGISTICA_URL + "/camiones"
        Write-Host "  URL: $url" -ForegroundColor Gray

        $response = Invoke-RestMethod -Uri $url -Method Post -Headers $headers -Body $camionData2 -ErrorAction Stop

        Write-Host "✓ Camión creado exitosamente (DIRECTO CON TOKEN)" -ForegroundColor Green
        Write-Host "  ID: $($response.id)" -ForegroundColor Gray
        Write-Host "  Patente: $($response.patente)" -ForegroundColor Gray
    } catch {
        Write-Host "✗ ERROR al crear camión (DIRECTO CON TOKEN):" -ForegroundColor Red
        if ($_.Exception.Response) {
            Write-Host "  StatusCode: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
        }
        Write-Host "  Message: $($_.Exception.Message)" -ForegroundColor Red
        if ($_.ErrorDetails.Message) {
            Write-Host "  Details: $($_.ErrorDetails.Message)" -ForegroundColor Red
        }
    }
}

Write-Host ""
Start-Sleep -Seconds 1

# --------------------------------------------
# Test 3: Crear Camión (A través del Gateway - Sin token)
# --------------------------------------------
Write-Host "Test 3: Crear Camión (A través del Gateway - Sin token)" -ForegroundColor Yellow

$camionData3 = @{
    patente = "GHI456"
    modelo = "Scania R500"
    capacidadPeso = 28000.0
    capacidadVolumen = 85.0
    disponible = $true
} | ConvertTo-Json

try {
    $url = $GATEWAY_URL + "/camiones"
    Write-Host "  URL: $url" -ForegroundColor Gray

    $response = Invoke-RestMethod -Uri $url -Method Post -Body $camionData3 -ContentType "application/json" -ErrorAction Stop

    Write-Host "✓ Camión creado exitosamente (GATEWAY SIN TOKEN)" -ForegroundColor Green
    Write-Host "  ID: $($response.id)" -ForegroundColor Gray
    Write-Host "  Patente: $($response.patente)" -ForegroundColor Gray
} catch {
    Write-Host "✗ ERROR al crear camión (GATEWAY SIN TOKEN):" -ForegroundColor Red
    if ($_.Exception.Response) {
        Write-Host "  StatusCode: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
    }
    Write-Host "  Message: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.ErrorDetails.Message) {
        Write-Host "  Details: $($_.ErrorDetails.Message)" -ForegroundColor Red
    }
}

Write-Host ""
Start-Sleep -Seconds 1

# --------------------------------------------
# Test 4: Crear Camión (A través del Gateway - Con token)
# --------------------------------------------
Write-Host "Test 4: Crear Camión (A través del Gateway - Con token)" -ForegroundColor Yellow

if ($null -eq $TOKEN) {
    Write-Host "⊘ SALTADO - No hay token disponible" -ForegroundColor Yellow
} else {
    $camionData4 = @{
        patente = "DEF321"
        modelo = "MAN TGX"
        capacidadPeso = 26000.0
        capacidadVolumen = 82.0
        disponible = $true
    } | ConvertTo-Json

    try {
        $url = $GATEWAY_URL + "/camiones"
        Write-Host "  URL: $url" -ForegroundColor Gray

        $response = Invoke-RestMethod -Uri $url -Method Post -Headers $headers -Body $camionData4 -ErrorAction Stop

        Write-Host "✓ Camión creado exitosamente (GATEWAY CON TOKEN)" -ForegroundColor Green
        Write-Host "  ID: $($response.id)" -ForegroundColor Gray
        Write-Host "  Patente: $($response.patente)" -ForegroundColor Gray
        $CAMION_ID = $response.id
    } catch {
        Write-Host "✗ ERROR al crear camión (GATEWAY CON TOKEN):" -ForegroundColor Red
        if ($_.Exception.Response) {
            Write-Host "  StatusCode: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
        }
        Write-Host "  Message: $($_.Exception.Message)" -ForegroundColor Red
        if ($_.ErrorDetails.Message) {
            Write-Host "  Details: $($_.ErrorDetails.Message)" -ForegroundColor Red
        }
    }
}

Write-Host ""
Start-Sleep -Seconds 1

# --------------------------------------------
# Test 5: Listar Camiones (Directo al servicio)
# --------------------------------------------
Write-Host "Test 5: Listar Camiones (Directo al servicio)" -ForegroundColor Yellow

try {
    $url = $LOGISTICA_URL + "/camiones"
    Write-Host "  URL: $url" -ForegroundColor Gray

    $response = Invoke-RestMethod -Uri $url -Method Get -ErrorAction Stop

    Write-Host "✓ Camiones listados exitosamente (DIRECTO)" -ForegroundColor Green
    Write-Host "  Total de camiones: $($response.Count)" -ForegroundColor Gray
    foreach ($camion in $response) {
        Write-Host "    - ID: $($camion.id) | Patente: $($camion.patente) | Modelo: $($camion.modelo)" -ForegroundColor Gray
    }
} catch {
    Write-Host "✗ ERROR al listar camiones (DIRECTO):" -ForegroundColor Red
    if ($_.Exception.Response) {
        Write-Host "  StatusCode: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
    }
    Write-Host "  Message: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Start-Sleep -Seconds 1

# --------------------------------------------
# Test 6: Listar Camiones (A través del Gateway)
# --------------------------------------------
Write-Host "Test 6: Listar Camiones (A través del Gateway)" -ForegroundColor Yellow

try {
    $url = $GATEWAY_URL + "/camiones"
    Write-Host "  URL: $url" -ForegroundColor Gray

    $response = Invoke-RestMethod -Uri $url -Method Get -ErrorAction Stop

    Write-Host "✓ Camiones listados exitosamente (GATEWAY)" -ForegroundColor Green
    Write-Host "  Total de camiones: $($response.Count)" -ForegroundColor Gray
    foreach ($camion in $response) {
        Write-Host "    - ID: $($camion.id) | Patente: $($camion.patente) | Modelo: $($camion.modelo)" -ForegroundColor Gray
    }
} catch {
    Write-Host "✗ ERROR al listar camiones (GATEWAY):" -ForegroundColor Red
    if ($_.Exception.Response) {
        Write-Host "  StatusCode: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
    }
    Write-Host "  Message: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Start-Sleep -Seconds 1

# ============================================
# SECCIÓN: PRUEBAS DE DEPÓSITOS
# ============================================
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "   PRUEBAS DE GESTIÓN DE DEPÓSITOS" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""

# --------------------------------------------
# Test 7: Crear Depósito (Directo al servicio)
# --------------------------------------------
Write-Host "Test 7: Crear Depósito (Directo al servicio)" -ForegroundColor Yellow

$depositoData = @{
    nombre = "Depósito Central CABA"
    direccion = "Av. del Libertador 1000, CABA"
    latitud = -34.5875
    longitud = -58.4189
    capacidadMaxima = 50000.0
    activo = $true
} | ConvertTo-Json

try {
    $url = $LOGISTICA_URL + "/depositos"
    Write-Host "  URL: $url" -ForegroundColor Gray

    $response = Invoke-RestMethod -Uri $url -Method Post -Body $depositoData -ContentType "application/json" -ErrorAction Stop

    Write-Host "✓ Depósito creado exitosamente (DIRECTO)" -ForegroundColor Green
    Write-Host "  ID: $($response.id)" -ForegroundColor Gray
    Write-Host "  Nombre: $($response.nombre)" -ForegroundColor Gray
    $DEPOSITO_ID = $response.id
} catch {
    Write-Host "✗ ERROR al crear depósito (DIRECTO):" -ForegroundColor Red
    if ($_.Exception.Response) {
        Write-Host "  StatusCode: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
    }
    Write-Host "  Message: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.ErrorDetails.Message) {
        Write-Host "  Details: $($_.ErrorDetails.Message)" -ForegroundColor Red
    }
}

Write-Host ""
Start-Sleep -Seconds 1

# --------------------------------------------
# Test 8: Crear Depósito (A través del Gateway)
# --------------------------------------------
Write-Host "Test 8: Crear Depósito (A través del Gateway)" -ForegroundColor Yellow

$depositoData2 = @{
    nombre = "Depósito Zona Norte"
    direccion = "Av. Gral. Paz 5000, Vicente López"
    latitud = -34.5234
    longitud = -58.4711
    capacidadMaxima = 60000.0
    activo = $true
} | ConvertTo-Json

try {
    $url = $GATEWAY_URL + "/depositos"
    Write-Host "  URL: $url" -ForegroundColor Gray

    $response = Invoke-RestMethod -Uri $url -Method Post -Body $depositoData2 -ContentType "application/json" -ErrorAction Stop

    Write-Host "✓ Depósito creado exitosamente (GATEWAY)" -ForegroundColor Green
    Write-Host "  ID: $($response.id)" -ForegroundColor Gray
    Write-Host "  Nombre: $($response.nombre)" -ForegroundColor Gray
} catch {
    Write-Host "✗ ERROR al crear depósito (GATEWAY):" -ForegroundColor Red
    if ($_.Exception.Response) {
        Write-Host "  StatusCode: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
    }
    Write-Host "  Message: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.ErrorDetails.Message) {
        Write-Host "  Details: $($_.ErrorDetails.Message)" -ForegroundColor Red
    }
}

Write-Host ""
Start-Sleep -Seconds 1

# --------------------------------------------
# Test 9: Listar Depósitos
# --------------------------------------------
Write-Host "Test 9: Listar Depósitos (A través del Gateway)" -ForegroundColor Yellow

try {
    $url = $GATEWAY_URL + "/depositos"
    Write-Host "  URL: $url" -ForegroundColor Gray

    $response = Invoke-RestMethod -Uri $url -Method Get -ErrorAction Stop

    Write-Host "✓ Depósitos listados exitosamente" -ForegroundColor Green
    Write-Host "  Total de depósitos: $($response.Count)" -ForegroundColor Gray
    foreach ($deposito in $response) {
        Write-Host "    - ID: $($deposito.id) | Nombre: $($deposito.nombre)" -ForegroundColor Gray
    }
} catch {
    Write-Host "✗ ERROR al listar depósitos:" -ForegroundColor Red
    if ($_.Exception.Response) {
        Write-Host "  StatusCode: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
    }
    Write-Host "  Message: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Start-Sleep -Seconds 1

# ============================================
# SECCIÓN: PRUEBAS DE TARIFAS
# ============================================
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "   PRUEBAS DE GESTIÓN DE TARIFAS" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""

# --------------------------------------------
# Test 10: Crear Tarifa (Directo al servicio)
# --------------------------------------------
Write-Host "Test 10: Crear Tarifa (Directo al servicio)" -ForegroundColor Yellow

$tarifaData = @{
    nombre = "Tarifa Estándar 2025"
    descripcion = "Tarifa base para transporte nacional"
    costoPorKm = 150.0
    costoBaseCombustible = 500.0
    costoEstadia = 1000.0
    factorPeso = 0.05
    factorVolumen = 0.03
    activa = $true
} | ConvertTo-Json

try {
    $url = $TARIFAS_URL + "/tarifas"
    Write-Host "  URL: $url" -ForegroundColor Gray

    $response = Invoke-RestMethod -Uri $url -Method Post -Body $tarifaData -ContentType "application/json" -ErrorAction Stop

    Write-Host "✓ Tarifa creada exitosamente (DIRECTO)" -ForegroundColor Green
    Write-Host "  ID: $($response.id)" -ForegroundColor Gray
    Write-Host "  Nombre: $($response.nombre)" -ForegroundColor Gray
    $TARIFA_ID = $response.id
} catch {
    Write-Host "✗ ERROR al crear tarifa (DIRECTO):" -ForegroundColor Red
    if ($_.Exception.Response) {
        Write-Host "  StatusCode: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
    }
    Write-Host "  Message: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.ErrorDetails.Message) {
        Write-Host "  Details: $($_.ErrorDetails.Message)" -ForegroundColor Red
    }
}

Write-Host ""
Start-Sleep -Seconds 1

# --------------------------------------------
# Test 11: Crear Tarifa (A través del Gateway)
# --------------------------------------------
Write-Host "Test 11: Crear Tarifa (A través del Gateway)" -ForegroundColor Yellow

$tarifaData2 = @{
    nombre = "Tarifa Premium 2025"
    descripcion = "Tarifa para servicio express"
    costoPorKm = 200.0
    costoBaseCombustible = 600.0
    costoEstadia = 1500.0
    factorPeso = 0.07
    factorVolumen = 0.04
    activa = $true
} | ConvertTo-Json

try {
    $url = $GATEWAY_URL + "/tarifas"
    Write-Host "  URL: $url" -ForegroundColor Gray

    $response = Invoke-RestMethod -Uri $url -Method Post -Body $tarifaData2 -ContentType "application/json" -ErrorAction Stop

    Write-Host "✓ Tarifa creada exitosamente (GATEWAY)" -ForegroundColor Green
    Write-Host "  ID: $($response.id)" -ForegroundColor Gray
    Write-Host "  Nombre: $($response.nombre)" -ForegroundColor Gray
} catch {
    Write-Host "✗ ERROR al crear tarifa (GATEWAY):" -ForegroundColor Red
    if ($_.Exception.Response) {
        Write-Host "  StatusCode: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
    }
    Write-Host "  Message: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.ErrorDetails.Message) {
        Write-Host "  Details: $($_.ErrorDetails.Message)" -ForegroundColor Red
    }
}

Write-Host ""
Start-Sleep -Seconds 1

# --------------------------------------------
# Test 12: Listar Tarifas
# --------------------------------------------
Write-Host "Test 12: Listar Tarifas (A través del Gateway)" -ForegroundColor Yellow

try {
    $url = $GATEWAY_URL + "/tarifas"
    Write-Host "  URL: $url" -ForegroundColor Gray

    $response = Invoke-RestMethod -Uri $url -Method Get -ErrorAction Stop

    Write-Host "✓ Tarifas listadas exitosamente" -ForegroundColor Green
    Write-Host "  Total de tarifas: $($response.Count)" -ForegroundColor Gray
    foreach ($tarifa in $response) {
        Write-Host "    - ID: $($tarifa.id) | Nombre: $($tarifa.nombre) | Costo/km: $($tarifa.costoPorKm)" -ForegroundColor Gray
    }
} catch {
    Write-Host "✗ ERROR al listar tarifas:" -ForegroundColor Red
    if ($_.Exception.Response) {
        Write-Host "  StatusCode: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
    }
    Write-Host "  Message: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Start-Sleep -Seconds 1

# ============================================
# RESUMEN FINAL
# ============================================
Write-Host ""
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "   RESUMEN DE PRUEBAS COMPLETADAS" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Se ejecutaron 12 tests de endpoints:" -ForegroundColor White
Write-Host "  - 6 tests de Gestión de Camiones" -ForegroundColor Gray
Write-Host "  - 3 tests de Gestión de Depósitos" -ForegroundColor Gray
Write-Host "  - 3 tests de Gestión de Tarifas" -ForegroundColor Gray
Write-Host ""
Write-Host "Revisa los resultados arriba para identificar qué funciona y qué falla." -ForegroundColor Yellow
Write-Host ""
Write-Host "LEYENDA:" -ForegroundColor White
Write-Host "  ✓ = Funcionó correctamente" -ForegroundColor Green
Write-Host "  ✗ = Falló (revisa el error)" -ForegroundColor Red
Write-Host "  ⊘ = Saltado (falta token)" -ForegroundColor Yellow
Write-Host ""
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Presiona cualquier tecla para cerrar..." -ForegroundColor Yellow
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
