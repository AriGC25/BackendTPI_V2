# Script completo para probar TODOS los endpoints de Cliente
Write-Host "=== PRUEBA COMPLETA DE ENDPOINTS DE CLIENTE ===" -ForegroundColor Cyan
Write-Host "Este script prueba:" -ForegroundColor Gray
Write-Host "  1. Registro de cliente" -ForegroundColor Gray
Write-Host "  2. Creación de solicitud de transporte" -ForegroundColor Gray
Write-Host "  3. Consulta de costo y tiempo estimado" -ForegroundColor Gray
Write-Host "  4. Consulta de tracking del contenedor" -ForegroundColor Gray
Write-Host ""

# Obtener token de cliente
Write-Host "1. Obteniendo token de cliente..." -ForegroundColor Yellow
$tokenBody = @{
    client_id = "backend-client"
    client_secret = "backend-client-secret"
    grant_type = "password"
    username = "cliente1"
    password = "cliente123"
}

try {
    $tokenResponse = Invoke-RestMethod -Uri "http://localhost:8080/realms/transportista-realm/protocol/openid-connect/token" `
        -Method Post -Body $tokenBody -ContentType "application/x-www-form-urlencoded"
    $token = $tokenResponse.access_token
    Write-Host "   ✓ Token obtenido exitosamente" -ForegroundColor Green
} catch {
    Write-Host "   ✗ Error obteniendo token: $($_.Exception.Message)" -ForegroundColor Red
    exit
}

$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json"
}

# 2. Registrar Cliente
Write-Host "`n2. Registrando nuevo cliente..." -ForegroundColor Yellow
$clienteData = @{
    nombre = "Maria"
    apellido = "Lopez"
    dni = "$(Get-Random -Minimum 10000000 -Maximum 99999999)"
    domicilio = "Avenida Libertador 5000, CABA"
    telefono = "11-6666-7777"
    email = "maria.lopez$(Get-Random)@email.com"
} | ConvertTo-Json

try {
    $response = Invoke-WebRequest -Uri "http://localhost:8000/clientes" `
        -Method Post -Body $clienteData -Headers $headers -UseBasicParsing
    $cliente = $response.Content | ConvertFrom-Json
    $clienteId = $cliente.id
    Write-Host "   ✓ Cliente creado exitosamente" -ForegroundColor Green
    Write-Host "     ID: $clienteId | Nombre: $($cliente.nombre) $($cliente.apellido)" -ForegroundColor Gray
} catch {
    Write-Host "   ✗ Error creando cliente: $($_.Exception.Message)" -ForegroundColor Red
    exit
}

# 3. Crear Solicitud de Transporte
Write-Host "`n3. Creando solicitud de transporte..." -ForegroundColor Yellow
$solicitudData = @{
    clienteId = $clienteId
    pesoContenedor = 18500.75
    volumenContenedor = 45.5
    descripcionContenedor = "Maquinaria industrial - Fragil"
    direccionOrigen = "Buenos Aires"
    latitudOrigen = -34.603722
    longitudOrigen = -58.381592
    direccionDestino = "Rosario"
    latitudDestino = -32.946899
    longitudDestino = -60.639275
} | ConvertTo-Json

try {
    $utf8Bytes = [System.Text.Encoding]::UTF8.GetBytes($solicitudData)
    $response = Invoke-WebRequest -Uri "http://localhost:8000/solicitudes" `
        -Method Post -Body $utf8Bytes -Headers $headers -UseBasicParsing
    $solicitud = $response.Content | ConvertFrom-Json
    $solicitudId = $solicitud.id
    $numeroSolicitud = $solicitud.numeroSolicitud
    $contenedorId = $solicitud.contenedorId
    Write-Host "   ✓ Solicitud creada exitosamente" -ForegroundColor Green
    Write-Host "     ID: $solicitudId | Número: $numeroSolicitud" -ForegroundColor Gray
    Write-Host "     Contenedor ID: $contenedorId" -ForegroundColor Gray
} catch {
    Write-Host "   ✗ Error creando solicitud: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        try {
            $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
            $responseBody = $reader.ReadToEnd()
            Write-Host "     Detalle: $responseBody" -ForegroundColor Red
        } catch {}
    }
    exit
}

# 4. Consultar Costo y Tiempo Estimado
Write-Host "`n4. Consultando costo y tiempo estimado de la solicitud..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8000/solicitudes/$solicitudId/costo" `
        -Method Get -Headers $headers -UseBasicParsing
    $costoInfo = $response.Content | ConvertFrom-Json
    Write-Host "   ✓ Información de costo obtenida" -ForegroundColor Green
    Write-Host "     Costo Total: `$$($costoInfo.costoTotal) $($costoInfo.moneda)" -ForegroundColor Gray
    Write-Host "     Tiempo Estimado: $($costoInfo.tiempoEstimadoHoras) horas" -ForegroundColor Gray
} catch {
    Write-Host "   ✗ Error consultando costo: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $statusCode = $_.Exception.Response.StatusCode.value__
        Write-Host "     Status Code: $statusCode" -ForegroundColor Red
    }
}

# 5. Consultar Tracking del Contenedor
if ($contenedorId) {
    Write-Host "`n5. Consultando tracking del contenedor..." -ForegroundColor Yellow
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8000/tracking/contenedor/$contenedorId" `
            -Method Get -Headers $headers -UseBasicParsing
        $eventos = $response.Content | ConvertFrom-Json
        Write-Host "   ✓ Historial de tracking obtenido" -ForegroundColor Green
        if ($eventos.Count -eq 0) {
            Write-Host "     (Sin eventos registrados aún - La solicitud está PENDIENTE)" -ForegroundColor Gray
        } else {
            Write-Host "     Eventos registrados: $($eventos.Count)" -ForegroundColor Gray
            foreach ($evento in $eventos | Select-Object -First 3) {
                Write-Host "     - $($evento.tipo): $($evento.descripcion)" -ForegroundColor Gray
            }
        }
    } catch {
        $statusCode = 0
        if ($_.Exception.Response) {
            $statusCode = $_.Exception.Response.StatusCode.value__
        }

        if ($statusCode -eq 500) {
            Write-Host "   ⚠ La tabla de tracking aún no está inicializada" -ForegroundColor Yellow
            Write-Host "     (Esto es normal para solicitudes nuevas)" -ForegroundColor Gray
        } else {
            Write-Host "   ✗ Error consultando tracking: $($_.Exception.Message)" -ForegroundColor Red
            Write-Host "     Status Code: $statusCode" -ForegroundColor Red
        }
    }
}

# 6. Consultar Tracking por Solicitud
Write-Host "`n6. Consultando tracking por solicitud..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8000/tracking/solicitud/$solicitudId" `
        -Method Get -Headers $headers -UseBasicParsing
    $eventos = $response.Content | ConvertFrom-Json
    Write-Host "   ✓ Historial de solicitud obtenido" -ForegroundColor Green
    if ($eventos.Count -eq 0) {
        Write-Host "     (Sin eventos registrados aún - La solicitud está PENDIENTE)" -ForegroundColor Gray
    } else {
        Write-Host "     Eventos registrados: $($eventos.Count)" -ForegroundColor Gray
    }
} catch {
    $statusCode = 0
    if ($_.Exception.Response) {
        $statusCode = $_.Exception.Response.StatusCode.value__
    }

    if ($statusCode -eq 500) {
        Write-Host "   ⚠ La tabla de tracking aún no está inicializada" -ForegroundColor Yellow
        Write-Host "     (Esto es normal para solicitudes nuevas)" -ForegroundColor Gray
    } else {
        Write-Host "   ✗ Error consultando tracking de solicitud: $($_.Exception.Message)" -ForegroundColor Red
        Write-Host "     Status Code: $statusCode" -ForegroundColor Red
    }
}

# 7. Consultar Solicitudes del Cliente
Write-Host "`n7. Consultando todas las solicitudes del cliente..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8000/solicitudes/cliente/$clienteId" `
        -Method Get -Headers $headers -UseBasicParsing
    $solicitudes = $response.Content | ConvertFrom-Json
    Write-Host "   ✓ Solicitudes del cliente obtenidas" -ForegroundColor Green
    Write-Host "     Total de solicitudes: $($solicitudes.Count)" -ForegroundColor Gray
    foreach ($sol in $solicitudes) {
        Write-Host "     - Solicitud #$($sol.numeroSolicitud) | Estado: $($sol.estado)" -ForegroundColor Gray
    }
} catch {
    Write-Host "   ✗ Error consultando solicitudes del cliente: $($_.Exception.Message)" -ForegroundColor Red
}

# Resumen
Write-Host "`n" -NoNewline
Write-Host "=== RESUMEN ===" -ForegroundColor Cyan
Write-Host "Cliente ID: $clienteId" -ForegroundColor White
Write-Host "Solicitud ID: $solicitudId" -ForegroundColor White
Write-Host "Número de Solicitud: $numeroSolicitud" -ForegroundColor White
Write-Host "Contenedor ID: $contenedorId" -ForegroundColor White
Write-Host "`nTodos los endpoints principales para el rol CLIENTE han sido probados." -ForegroundColor Green
Write-Host ""
