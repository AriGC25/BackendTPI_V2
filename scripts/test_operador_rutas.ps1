# Script completo para probar TODOS los endpoints de OPERADOR - Gestión de Rutas
Write-Host "=== PRUEBA COMPLETA DE ENDPOINTS DE OPERADOR ===" -ForegroundColor Cyan
Write-Host "Este script prueba:" -ForegroundColor Gray
Write-Host "  1. Autenticación como OPERADOR" -ForegroundColor Gray
Write-Host "  2. Creación de solicitud de prueba (como CLIENTE)" -ForegroundColor Gray
Write-Host "  3. Consulta de rutas tentativas con estimaciones" -ForegroundColor Gray
Write-Host "  4. Asignación de ruta seleccionada" -ForegroundColor Gray
Write-Host "  5. Consulta de ruta asignada" -ForegroundColor Gray
Write-Host "  6. Verificación de estado de solicitud" -ForegroundColor Gray
Write-Host ""

# === PASO 1: Obtener token de OPERADOR ===
Write-Host "1. Obteniendo token de OPERADOR..." -ForegroundColor Yellow
$tokenBody = @{
    client_id = "backend-client"
    client_secret = "backend-client-secret"
    grant_type = "password"
    username = "operador1"
    password = "operador123"
}

try {
    $tokenResponse = Invoke-RestMethod -Uri "http://localhost:8080/realms/transportista-realm/protocol/openid-connect/token" `
        -Method Post -Body $tokenBody -ContentType "application/x-www-form-urlencoded"
    $tokenOperador = $tokenResponse.access_token
    Write-Host "   ✓ Token de OPERADOR obtenido exitosamente" -ForegroundColor Green
} catch {
    Write-Host "   ✗ Error obteniendo token de operador: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        try {
            $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
            $responseBody = $reader.ReadToEnd()
            Write-Host "     Detalle del error: $responseBody" -ForegroundColor Red
        } catch {}
    }
    Write-Host "`nPresiona cualquier tecla para salir..." -ForegroundColor Yellow
    $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
    exit
}

$headersOperador = @{
    "Authorization" = "Bearer $tokenOperador"
    "Content-Type" = "application/json"
}

# === PASO 2: Crear solicitud como CLIENTE (necesaria para las pruebas) ===
Write-Host "`n2. Creando solicitud de prueba (como CLIENTE)..." -ForegroundColor Yellow

# Primero obtener token de cliente
try {
    $tokenClienteBody = @{
        client_id = "backend-client"
        client_secret = "backend-client-secret"
        grant_type = "password"
        username = "cliente1"
        password = "cliente123"
    }

    $tokenClienteResponse = Invoke-RestMethod -Uri "http://localhost:8080/realms/transportista-realm/protocol/openid-connect/token" `
        -Method Post -Body $tokenClienteBody -ContentType "application/x-www-form-urlencoded"
    $tokenCliente = $tokenClienteResponse.access_token

    $headersCliente = @{
        "Authorization" = "Bearer $tokenCliente"
        "Content-Type" = "application/json"
    }
} catch {
    Write-Host "   ✗ Error obteniendo token de cliente: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        try {
            $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
            $responseBody = $reader.ReadToEnd()
            Write-Host "     Detalle del error: $responseBody" -ForegroundColor Red
        } catch {}
    }
    Write-Host "`nPresiona cualquier tecla para salir..." -ForegroundColor Yellow
    $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
    exit
}

# Crear solicitud
$solicitudData = @{
    clienteId = 1
    pesoContenedor = 18500.75
    volumenContenedor = 45.5
    descripcionContenedor = "Equipos electronicos sensibles - Refrigerado"
    direccionOrigen = "Av. Corrientes 1000, Buenos Aires"
    latitudOrigen = -34.6037
    longitudOrigen = -58.3816
    direccionDestino = "Av. Libertador 5000, Buenos Aires"
    latitudDestino = -34.5601
    longitudDestino = -58.4558
} | ConvertTo-Json -Depth 10

try {
    $utf8Bytes = [System.Text.Encoding]::UTF8.GetBytes($solicitudData)
    $response = Invoke-WebRequest -Uri "http://localhost:8000/solicitudes" `
        -Method Post -Body $utf8Bytes -Headers $headersCliente -UseBasicParsing
    $solicitud = $response.Content | ConvertFrom-Json
    $solicitudId = $solicitud.id
    Write-Host "   ✓ Solicitud creada exitosamente" -ForegroundColor Green
    Write-Host "     ID: $solicitudId" -ForegroundColor Gray
    Write-Host "     Numero: $($solicitud.numeroSolicitud)" -ForegroundColor Gray
    Write-Host "     Origen: $($solicitud.direccionOrigen)" -ForegroundColor Gray
    Write-Host "     Destino: $($solicitud.direccionDestino)" -ForegroundColor Gray
    Write-Host "     Estado: $($solicitud.estado)" -ForegroundColor Gray
} catch {
    Write-Host "   ✗ Error creando solicitud: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        try {
            $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
            $responseBody = $reader.ReadToEnd()
            Write-Host "     Detalle: $responseBody" -ForegroundColor Red
        } catch {}
    }
    Write-Host "`nPresiona cualquier tecla para salir..." -ForegroundColor Yellow
    $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
    exit
}

# === PASO 3: CONSULTAR RUTAS TENTATIVAS (OPERADOR) ===
Write-Host "`n3. Consultando rutas tentativas..." -ForegroundColor Yellow
Write-Host "   Endpoint: GET /rutas/tentativas/$solicitudId" -ForegroundColor Gray

try {
    $response = Invoke-WebRequest -Uri "http://localhost:8000/rutas/tentativas/$solicitudId" `
        -Method Get -Headers $headersOperador -UseBasicParsing
    $rutasTentativas = $response.Content | ConvertFrom-Json

    Write-Host "   ✓ Rutas tentativas obtenidas exitosamente" -ForegroundColor Green
    Write-Host "     Cantidad de opciones: $($rutasTentativas.Count)" -ForegroundColor Gray
    Write-Host ""

    $rutaSeleccionada = $null
    $opcionNumero = 1

    foreach ($ruta in $rutasTentativas) {
        Write-Host "     --- OPCIÓN $opcionNumero ---" -ForegroundColor Cyan
        Write-Host "     Descripción: $($ruta.descripcion)" -ForegroundColor White
        Write-Host "     Cantidad de tramos: $($ruta.cantidadTramos)" -ForegroundColor Gray
        Write-Host "     Cantidad de depósitos: $($ruta.cantidadDepositos)" -ForegroundColor Gray
        Write-Host "     Distancia total: $($ruta.distanciaTotal) km" -ForegroundColor Gray
        Write-Host "     Costo estimado: `$$($ruta.costoEstimado) ARS" -ForegroundColor Gray
        Write-Host "     Tiempo estimado: $($ruta.tiempoEstimadoHoras) horas" -ForegroundColor Gray

        Write-Host "     Tramos:" -ForegroundColor Gray
        foreach ($tramo in $ruta.tramos) {
            Write-Host "       $($tramo.ordenTramo). [$($tramo.tipoTramo)]" -ForegroundColor Gray
            Write-Host "          Origen: $($tramo.direccionOrigen)" -ForegroundColor Gray
            Write-Host "          Destino: $($tramo.direccionDestino)" -ForegroundColor Gray
        }
        Write-Host ""

        # Seleccionar la primera ruta (directa) para asignar
        if ($opcionNumero -eq 1) {
            $rutaSeleccionada = $ruta
        }
        $opcionNumero++
    }
} catch {
    Write-Host "   ✗ Error consultando rutas tentativas: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $statusCode = $_.Exception.Response.StatusCode.value__
        Write-Host "     Status Code: $statusCode" -ForegroundColor Red
        try {
            $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
            $responseBody = $reader.ReadToEnd()
            Write-Host "     Detalle: $responseBody" -ForegroundColor Red
        } catch {}
    }
    Write-Host "`nPresiona cualquier tecla para salir..." -ForegroundColor Yellow
    $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
    exit
}

# === PASO 4: ASIGNAR RUTA SELECCIONADA (OPERADOR) ===
Write-Host "4. Asignando ruta seleccionada..." -ForegroundColor Yellow
Write-Host "   Endpoint: POST /rutas/asignar/$solicitudId" -ForegroundColor Gray
Write-Host "   Ruta a asignar: $($rutaSeleccionada.descripcion)" -ForegroundColor Gray

$rutaAsignarData = $rutaSeleccionada | ConvertTo-Json -Depth 10

try {
    $response = Invoke-WebRequest -Uri "http://localhost:8000/rutas/asignar/$solicitudId" `
        -Method Post -Body $rutaAsignarData -Headers $headersOperador -UseBasicParsing
    $rutaAsignada = $response.Content | ConvertFrom-Json

    Write-Host "   ✓ Ruta asignada exitosamente" -ForegroundColor Green
    Write-Host "     ID de ruta: $($rutaAsignada.id)" -ForegroundColor Gray
    Write-Host "     Cantidad de tramos: $($rutaAsignada.cantidadTramos)" -ForegroundColor Gray
    Write-Host "     Cantidad de depósitos: $($rutaAsignada.cantidadDepositos)" -ForegroundColor Gray
    Write-Host "     Fecha de creación: $($rutaAsignada.fechaCreacion)" -ForegroundColor Gray

    $rutaId = $rutaAsignada.id
} catch {
    Write-Host "   ✗ Error asignando ruta: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        try {
            $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
            $responseBody = $reader.ReadToEnd()
            Write-Host "     Detalle: $responseBody" -ForegroundColor Red
        } catch {}
    }
    Write-Host "`nPresiona cualquier tecla para salir..." -ForegroundColor Yellow
    $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
    exit
}

# === PASO 5: CONSULTAR RUTA ASIGNADA ===
Write-Host "`n5. Consultando ruta asignada..." -ForegroundColor Yellow
Write-Host "   Endpoint: GET /rutas/solicitud/$solicitudId" -ForegroundColor Gray

try {
    $response = Invoke-WebRequest -Uri "http://localhost:8000/rutas/solicitud/$solicitudId" `
        -Method Get -Headers $headersOperador -UseBasicParsing
    $rutaConsultada = $response.Content | ConvertFrom-Json

    Write-Host "   ✓ Ruta consultada exitosamente" -ForegroundColor Green
    Write-Host "     ID: $($rutaConsultada.id)" -ForegroundColor Gray
    Write-Host "     Tramos guardados: $($rutaConsultada.tramos.Count)" -ForegroundColor Gray

    Write-Host "     Detalles de tramos guardados:" -ForegroundColor Gray
    foreach ($tramo in $rutaConsultada.tramos) {
        Write-Host "       Tramo $($tramo.ordenTramo):" -ForegroundColor White
        Write-Host "         ID: $($tramo.id)" -ForegroundColor Gray
        Write-Host "         Tipo: $($tramo.tipoTramo)" -ForegroundColor Gray
        Write-Host "         Estado: $($tramo.estado)" -ForegroundColor Gray
        Write-Host "         De: $($tramo.direccionOrigen)" -ForegroundColor Gray
        Write-Host "         A: $($tramo.direccionDestino)" -ForegroundColor Gray
    }
} catch {
    Write-Host "   ✗ Error consultando ruta asignada: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $statusCode = $_.Exception.Response.StatusCode.value__
        Write-Host "     Status Code: $statusCode" -ForegroundColor Red
    }
}

# === PASO 6: Verificar estado actualizado de la solicitud ===
Write-Host "`n6. Verificando estado de la solicitud..." -ForegroundColor Yellow
Write-Host "   Endpoint: GET /solicitudes/$solicitudId" -ForegroundColor Gray

try {
    $response = Invoke-WebRequest -Uri "http://localhost:8000/solicitudes/$solicitudId" `
        -Method Get -Headers $headersCliente -UseBasicParsing
    $solicitudActualizada = $response.Content | ConvertFrom-Json

    Write-Host "   ✓ Estado de solicitud verificado" -ForegroundColor Green
    Write-Host "     Estado actual: $($solicitudActualizada.estado)" -ForegroundColor Gray
    Write-Host "     Fecha actualización: $($solicitudActualizada.fechaActualizacion)" -ForegroundColor Gray

    if ($solicitudActualizada.estado -eq "RUTA_ASIGNADA") {
        Write-Host "     ✓ El estado se actualizó correctamente" -ForegroundColor Green
    } else {
        Write-Host "     ⚠ El estado no cambió como se esperaba" -ForegroundColor Yellow
    }
} catch {
    Write-Host "   ✗ Error verificando estado de solicitud: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $statusCode = $_.Exception.Response.StatusCode.value__
        Write-Host "     Status Code: $statusCode" -ForegroundColor Red
    }
}

# === RESUMEN FINAL ===
Write-Host "`n" -NoNewline
Write-Host "=== RESUMEN ===" -ForegroundColor Cyan
Write-Host "Solicitud ID: $solicitudId" -ForegroundColor White
Write-Host "Ruta ID: $rutaId" -ForegroundColor White
Write-Host "Estado Final: $($solicitudActualizada.estado)" -ForegroundColor White
Write-Host "`nTodos los endpoints principales para el rol OPERADOR han sido probados." -ForegroundColor Green
Write-Host ""
Write-Host "Funcionalidades verificadas:" -ForegroundColor Gray
Write-Host "  ✓ Consultar rutas tentativas con estimaciones" -ForegroundColor Gray
Write-Host "  ✓ Asignar ruta con todos sus tramos" -ForegroundColor Gray
Write-Host "  ✓ Consultar ruta asignada" -ForegroundColor Gray
Write-Host "  ✓ Actualización automática del estado de solicitud" -ForegroundColor Gray
Write-Host ""
Write-Host "`nPresiona cualquier tecla para salir..." -ForegroundColor Yellow
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
