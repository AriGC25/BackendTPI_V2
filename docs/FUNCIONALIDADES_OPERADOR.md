# Funcionalidades del OPERADOR - Gestión de Rutas

## Descripción General

El rol **OPERADOR** tiene acceso a funcionalidades especializadas para la gestión de rutas de transporte. Puede consultar múltiples opciones de rutas tentativas con estimaciones detalladas y asignar la ruta más conveniente a cada solicitud.

---

## Funcionalidades Implementadas

### 1. Consultar Rutas Tentativas

**Endpoint:** `GET /rutas/tentativas/{solicitudId}`

**Descripción:** Genera automáticamente múltiples opciones de rutas para una solicitud, incluyendo:
- Ruta directa (origen → destino)
- Ruta con depósito intermedio (origen → depósito → destino)

**Roles permitidos:** `OPERADOR`

**Parámetros:**
- `solicitudId` (path): ID de la solicitud

**Respuesta exitosa (200 OK):**
```json
[
  {
    "solicitudId": 1,
    "cantidadTramos": 1,
    "cantidadDepositos": 0,
    "descripcion": "Ruta directa sin depósitos intermedios",
    "distanciaTotal": 125.50,
    "costoEstimado": 22500.75,
    "tiempoEstimadoHoras": 4.09,
    "tramos": [
      {
        "tipoTramo": "ORIGEN_DESTINO",
        "ordenTramo": 1,
        "estado": "PENDIENTE",
        "direccionOrigen": "Av. Corrientes 1000, Buenos Aires",
        "latitudOrigen": -34.6037,
        "longitudOrigen": -58.3816,
        "direccionDestino": "Av. Libertador 5000, Buenos Aires",
        "latitudDestino": -34.5601,
        "longitudDestino": -58.4558
      }
    ]
  },
  {
    "solicitudId": 1,
    "cantidadTramos": 2,
    "cantidadDepositos": 1,
    "descripcion": "Ruta con 1 depósito intermedio (Depósito Central)",
    "distanciaTotal": 145.80,
    "costoEstimado": 25800.50,
    "tiempoEstimadoHoras": 6.43,
    "tramos": [
      {
        "tipoTramo": "ORIGEN_DEPOSITO",
        "ordenTramo": 1,
        "estado": "PENDIENTE",
        "direccionOrigen": "Av. Corrientes 1000, Buenos Aires",
        "latitudOrigen": -34.6037,
        "longitudOrigen": -58.3816,
        "direccionDestino": "Depósito Central - Av. General Paz 1000",
        "latitudDestino": -34.6037,
        "longitudDestino": -58.3816
      },
      {
        "tipoTramo": "DEPOSITO_DESTINO",
        "ordenTramo": 2,
        "estado": "PENDIENTE",
        "direccionOrigen": "Depósito Central - Av. General Paz 1000",
        "latitudOrigen": -34.6037,
        "longitudOrigen": -58.3816,
        "direccionDestino": "Av. Libertador 5000, Buenos Aires",
        "latitudDestino": -34.5601,
        "longitudDestino": -58.4558
      }
    ]
  }
]
```

**Cálculos de estimaciones:**

1. **Distancia Total:**
   - Se calcula usando Google Maps Directions API
   - Fallback a fórmula de Haversine si la API no está disponible
   - Suma de distancias de todos los tramos

2. **Costo Estimado:**
   - Costo base por km: **120 ARS/km**
   - Costo de combustible: **52.5 ARS/km** (0.35 L/km × 150 ARS/L)
   - Costo de estadía en depósito: **500 ARS** por depósito
   - Factor por peso del contenedor: basado en peso/20000
   - Factor por volumen del contenedor: basado en volumen/50
   - Factor máximo aplicable: 2x el costo base

3. **Tiempo Estimado:**
   - Velocidad promedio: **60 km/h**
   - Tiempo de carga/descarga: **2 horas** por tramo
   - Fórmula: `(distancia / velocidad) + tiempo_carga_descarga`

**Ejemplo de uso con cURL:**
```bash
curl -X GET "http://localhost:8080/rutas/tentativas/1" \
  -H "Authorization: Bearer {TOKEN_OPERADOR}"
```

---

### 2. Asignar Ruta a Solicitud

**Endpoint:** `POST /rutas/asignar/{solicitudId}`

**Descripción:** Asigna una ruta definitiva a la solicitud con todos sus tramos. Una vez asignada, la ruta se guarda en la base de datos y la solicitud cambia al estado `RUTA_ASIGNADA`.

**Roles permitidos:** `OPERADOR`

**Parámetros:**
- `solicitudId` (path): ID de la solicitud

**Body (JSON):**
```json
{
  "solicitudId": 1,
  "cantidadTramos": 1,
  "cantidadDepositos": 0,
  "descripcion": "Ruta directa sin depósitos intermedios",
  "tramos": [
    {
      "tipoTramo": "ORIGEN_DESTINO",
      "ordenTramo": 1,
      "estado": "PENDIENTE",
      "direccionOrigen": "Av. Corrientes 1000, Buenos Aires",
      "latitudOrigen": -34.6037,
      "longitudOrigen": -58.3816,
      "direccionDestino": "Av. Libertador 5000, Buenos Aires",
      "latitudDestino": -34.5601,
      "longitudDestino": -58.4558
    }
  ]
}
```

**Respuesta exitosa (200 OK):**
```json
{
  "id": 15,
  "solicitudId": 1,
  "cantidadTramos": 1,
  "cantidadDepositos": 0,
  "fechaCreacion": "2025-11-07T10:30:00",
  "fechaActualizacion": "2025-11-07T10:30:00",
  "tramos": [
    {
      "id": 25,
      "rutaId": 15,
      "tipoTramo": "ORIGEN_DESTINO",
      "ordenTramo": 1,
      "estado": "PENDIENTE",
      "direccionOrigen": "Av. Corrientes 1000, Buenos Aires",
      "latitudOrigen": -34.6037,
      "longitudOrigen": -58.3816,
      "direccionDestino": "Av. Libertador 5000, Buenos Aires",
      "latitudDestino": -34.5601,
      "longitudDestino": -58.4558,
      "fechaCreacion": "2025-11-07T10:30:00",
      "fechaActualizacion": "2025-11-07T10:30:00"
    }
  ]
}
```

**Validaciones:**
- La solicitud debe existir
- La solicitud NO debe tener ya una ruta asignada
- Los tramos deben tener coordenadas válidas

**Efectos secundarios:**
- Se crea la ruta en la base de datos
- Se crean todos los tramos asociados
- El estado de la solicitud cambia a `RUTA_ASIGNADA`
- Se actualiza la fecha de actualización de la solicitud

**Ejemplo de uso con cURL:**
```bash
curl -X POST "http://localhost:8080/rutas/asignar/1" \
  -H "Authorization: Bearer {TOKEN_OPERADOR}" \
  -H "Content-Type: application/json" \
  -d '{
    "solicitudId": 1,
    "cantidadTramos": 1,
    "cantidadDepositos": 0,
    "tramos": [...]
  }'
```

---

### 3. Obtener Ruta Asignada

**Endpoint:** `GET /rutas/solicitud/{solicitudId}`

**Descripción:** Consulta la ruta que fue asignada a una solicitud específica.

**Roles permitidos:** `OPERADOR`, `CLIENTE`

**Parámetros:**
- `solicitudId` (path): ID de la solicitud

**Respuesta exitosa (200 OK):**
```json
{
  "id": 15,
  "solicitudId": 1,
  "cantidadTramos": 1,
  "cantidadDepositos": 0,
  "fechaCreacion": "2025-11-07T10:30:00",
  "fechaActualizacion": "2025-11-07T10:30:00",
  "tramos": [
    {
      "id": 25,
      "rutaId": 15,
      "tipoTramo": "ORIGEN_DESTINO",
      "ordenTramo": 1,
      "estado": "PENDIENTE",
      "direccionOrigen": "Av. Corrientes 1000, Buenos Aires",
      "latitudOrigen": -34.6037,
      "longitudOrigen": -58.3816,
      "direccionDestino": "Av. Libertador 5000, Buenos Aires",
      "latitudDestino": -34.5601,
      "longitudDestino": -58.4558,
      "camionId": null,
      "transportistaId": null,
      "fechaInicio": null,
      "fechaFin": null,
      "fechaCreacion": "2025-11-07T10:30:00",
      "fechaActualizacion": "2025-11-07T10:30:00"
    }
  ]
}
```

**Ejemplo de uso con cURL:**
```bash
curl -X GET "http://localhost:8080/rutas/solicitud/1" \
  -H "Authorization: Bearer {TOKEN_OPERADOR}"
```

---

## Flujo de Trabajo Típico

1. **Cliente crea solicitud** → Estado: `PENDIENTE`
2. **Operador consulta rutas tentativas** → Visualiza múltiples opciones con estimaciones
3. **Operador compara opciones** → Analiza costos, tiempos y distancias
4. **Operador asigna ruta** → Estado cambia a: `RUTA_ASIGNADA`
5. **Cliente/Operador consulta ruta asignada** → Ve los detalles completos

---

## Tipos de Tramos

| Tipo | Descripción |
|------|-------------|
| `ORIGEN_DESTINO` | Tramo directo desde origen hasta destino final |
| `ORIGEN_DEPOSITO` | Desde origen hasta un depósito intermedio |
| `DEPOSITO_DEPOSITO` | Entre dos depósitos |
| `DEPOSITO_DESTINO` | Desde depósito hasta destino final |

---

## Estados de Tramos

| Estado | Descripción |
|--------|-------------|
| `PENDIENTE` | Tramo creado pero sin asignar |
| `ASIGNADO` | Tramo asignado a un transportista/camión |
| `EN_CURSO` | Tramo en ejecución |
| `COMPLETADO` | Tramo finalizado |

---

## Pruebas

Para probar estas funcionalidades, ejecuta el script de prueba:

```powershell
cd C:\Users\Ariana\Desktop\TP_V4\BackendTPI-main\scripts
.\test_operador_rutas.ps1
```

Este script:
1. Autentica como OPERADOR
2. Crea una solicitud de prueba
3. Consulta rutas tentativas
4. Asigna una ruta
5. Verifica que todo funcione correctamente

---

## Configuración de Google Maps API

El servicio usa Google Maps Directions API para cálculos precisos de distancias. Si la API no está disponible, usa la fórmula de Haversine como fallback.

**Configurar API Key** (opcional):
```yaml
# application.yml
google:
  maps:
    api-key: TU_API_KEY_AQUI
    directions-url: https://maps.googleapis.com/maps/api/directions/json
```

Sin API key, el sistema seguirá funcionando con cálculos aproximados.

---

## Errores Comunes

| Error | Causa | Solución |
|-------|-------|----------|
| 403 Forbidden | Token sin rol OPERADOR | Verificar que el usuario tenga el rol correcto |
| 404 Not Found | Solicitud no existe | Verificar el ID de solicitud |
| 400 Bad Request | Solicitud ya tiene ruta | Una solicitud solo puede tener una ruta asignada |
| 401 Unauthorized | Token inválido o expirado | Renovar el token de autenticación |

---

## Próximas Mejoras

- [ ] Múltiples depósitos intermedios
- [ ] Optimización automática de rutas
- [ ] Cálculo de emisiones de CO2
- [ ] Integración con sistema de peajes
- [ ] Preferencias de rutas (evitar autopistas, etc.)

