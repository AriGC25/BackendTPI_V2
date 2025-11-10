# Script para inspeccionar el token JWT
Write-Host "=== INSPECCIONANDO TOKEN JWT ===" -ForegroundColor Cyan

# Obtener token de cliente
Write-Host "`n1. Obteniendo token de cliente..." -ForegroundColor Yellow
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
    Write-Host "✓ Token obtenido exitosamente" -ForegroundColor Green

    # Decodificar el payload del JWT (parte central)
    Write-Host "`n2. Decodificando payload del JWT..." -ForegroundColor Yellow
    $parts = $token.Split('.')
    if ($parts.Length -eq 3) {
        $payload = $parts[1]
        # Agregar padding si es necesario
        while ($payload.Length % 4 -ne 0) {
            $payload += "="
        }
        $payloadBytes = [System.Convert]::FromBase64String($payload.Replace('-', '+').Replace('_', '/'))
        $payloadJson = [System.Text.Encoding]::UTF8.GetString($payloadBytes)
        $payloadObj = $payloadJson | ConvertFrom-Json

        Write-Host "`n3. Contenido del token JWT:" -ForegroundColor Cyan
        Write-Host $payloadJson | ConvertTo-Json -Depth 10

        Write-Host "`n4. Buscando roles..." -ForegroundColor Yellow

        # Buscar en diferentes lugares donde pueden estar los roles
        if ($payloadObj.roles) {
            Write-Host "✓ Encontrados en 'roles': $($payloadObj.roles -join ', ')" -ForegroundColor Green
        }

        if ($payloadObj.realm_access.roles) {
            Write-Host "✓ Encontrados en 'realm_access.roles': $($payloadObj.realm_access.roles -join ', ')" -ForegroundColor Green
        }

        if ($payloadObj.resource_access.'backend-client'.roles) {
            Write-Host "✓ Encontrados en 'resource_access.backend-client.roles': $($payloadObj.resource_access.'backend-client'.roles -join ', ')" -ForegroundColor Green
        }

        if ($payloadObj.scope) {
            Write-Host "Scope: $($payloadObj.scope)" -ForegroundColor Gray
        }
    }

} catch {
    Write-Host "✗ Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n=== FIN INSPECCIÓN ===" -ForegroundColor Cyan

