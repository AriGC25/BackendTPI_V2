# Test de endpoints de Solicitudes y Tracking vía API Gateway
Write-Output "Solicitando token a Keycloak..."
$tokenResponse = Invoke-RestMethod -Uri 'http://localhost:8080/realms/transportista-realm/protocol/openid-connect/token' -Method POST -ContentType 'application/x-www-form-urlencoded' -Body 'client_id=backend-client&client_secret=backend-client-secret&grant_type=password&username=operador1&password=operador123'
$token = $tokenResponse.access_token
Write-Output "Token recibido (longitud): $($token.Length)"
$headers = @{ Authorization = "Bearer $token" }

function Do-Req($method, $uri, $body=$null) {
    Write-Output "\n=== $method $uri ==="
    try {
        if ($body) {
            $resp = Invoke-WebRequest -Uri $uri -Method $method -Headers $headers -Body $body -ContentType 'application/json' -UseBasicParsing -ErrorAction Stop
        } else {
            $resp = Invoke-WebRequest -Uri $uri -Method $method -Headers $headers -UseBasicParsing -ErrorAction Stop
        }
        Write-Output "StatusCode: $($resp.StatusCode)"
        if ($resp.Content) { Write-Output "Content: $($resp.Content)" }
    } catch {
        if ($_.Exception.Response -ne $null) {
            $r = $_.Exception.Response
            $sr = New-Object System.IO.StreamReader($r.GetResponseStream())
            $body = $sr.ReadToEnd()
            Write-Output "ERROR StatusCode: $($r.StatusCode.value__)"
            Write-Output "Body: $body"
        } else {
            Write-Output "ERROR: $_"
        }
    }
}

# Pruebas
Do-Req -method GET -uri 'http://localhost:8000/api/solicitudes/solicitudes'
Do-Req -method GET -uri 'http://localhost:8000/api/solicitudes/solicitudes/1'
Do-Req -method POST -uri 'http://localhost:8000/api/solicitudes/solicitudes' -body '{"origen":1,"destino":2,"clienteId":1}'

Do-Req -method GET -uri 'http://localhost:8000/api/tracking/tracking/solicitud/1'
Do-Req -method GET -uri 'http://localhost:8000/api/tracking/tracking/contenedor/1'

Write-Output "\nTests completados."
