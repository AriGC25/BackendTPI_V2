# Script para verificar roles en el token
Write-Output "=== VERIFICACION DE ROLES EN TOKEN ==="
Write-Output "Solicitando token a Keycloak..."
$tokenResponse = Invoke-RestMethod -Uri 'http://localhost:8080/realms/transportista-realm/protocol/openid-connect/token' -Method POST -ContentType 'application/x-www-form-urlencoded' -Body 'client_id=backend-client&client_secret=backend-client-secret&grant_type=password&username=operador1&password=operador123'
$token = $tokenResponse.access_token

Write-Output "`nDecodificando token JWT..."
$parts = $token.Split('.')
$payload = $parts[1]

# Ajustar padding Base64
$payload = $payload.Replace('-','+').Replace('_','/')
switch ($payload.Length % 4) {
    0 { break }
    2 { $payload += '==' }
    3 { $payload += '=' }
}

$decodedBytes = [System.Convert]::FromBase64String($payload)
$decodedJson = [System.Text.Encoding]::UTF8.GetString($decodedBytes)
$tokenData = $decodedJson | ConvertFrom-Json

Write-Output "`nInformacion del token:"
Write-Output "Usuario: $($tokenData.preferred_username)"
Write-Output "Nombre: $($tokenData.name)"
Write-Output "Email: $($tokenData.email)"
Write-Output "`nRoles encontrados:"
if ($tokenData.roles) {
    $tokenData.roles | ForEach-Object { Write-Output "  - $_" }
} else {
    Write-Output "  (No se encontro el claim 'roles')"
}

Write-Output "`nRealm Access Roles:"
if ($tokenData.realm_access -and $tokenData.realm_access.roles) {
    $tokenData.realm_access.roles | ForEach-Object { Write-Output "  - $_" }
} else {
    Write-Output "  (No se encontro realm_access.roles)"
}

Write-Output "`n=== ANALISIS ==="
Write-Output "Los endpoints requieren:"
Write-Output "  - Clientes (POST/GET): rol 'OPERADOR'"
Write-Output "  - Solicitudes (GET listar): rol 'OPERADOR'"
Write-Output "`nRoles necesarios: ROLE_OPERADOR o OPERADOR"

