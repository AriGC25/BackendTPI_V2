# 📘 Guía para Probar Endpoints en Postman

## 🚀 Configuración Inicial

### 1. Importar la Colección

1. Abre Postman
2. Click en **Import**
3. Selecciona el archivo: `docs/endpoints-paso-a-paso.json`
4. La colección se importará con todas las variables y scripts configurados

### 2. Verificar Variables de Entorno

Asegúrate de que las variables estén configuradas correctamente:

**Variables principales:**
- `gateway_url`: `http://localhost:8000`
- `keycloak_url`: `http://localhost:8080`
- `cliente_id`: `1` (valor por defecto, se actualizará automáticamente)

**Variables que se llenan automáticamente:**
- `token_cliente`
- `token_operador`
- `token_transportista`
- `solicitud_id`
- `contenedor_id`
- `ruta_id`
- `tramo_id`

---

## ⚠️ Consideraciones Importantes

### 🔐 1. Autenticación - Tokens JWT

**IMPORTANTE:** Los tokens JWT tienen **tiempo de expiración** (generalmente 5-10 minutos).

#### Síntomas de token expirado:
- Error `401 Unauthorized`
- Mensaje: `"Token is expired"`

#### Solución:
1. Ejecuta nuevamente el endpoint de login correspondiente:
   - `Login Cliente` para endpoints de cliente
   - `Login Operador` para endpoints de operador
2. El token se guardará automáticamente en las variables

**💡 Tip:** Ejecuta el login al inicio de cada sesión de pruebas.

---

### 📊 2. Orden de Ejecución - Flujo Correcto

Los endpoints tienen **dependencias**. Sigue este orden:

#### Para CLIENTE:

```
1. Login Cliente
   ↓
2. Registrar Cliente (solo la primera vez)
   ↓ (guarda cliente_id)
3. Crear Solicitud de Transporte
   ↓ (guarda solicitud_id y contenedor_id)
4. Consultar Costo y Tiempo Estimado
5. Consultar Tracking del Contenedor
6. Consultar Solicitudes del Cliente
```

#### Para OPERADOR:

```
1. Login Operador
   ↓
2. Login Cliente + Crear Solicitud (preparación)
   ↓ (necesitas una solicitud_id)
3. Consultar Rutas Tentativas
   ↓ (guarda ruta_tentativa)
4. Asignar Ruta a Solicitud
   ↓ (guarda ruta_id y tramo_id)
5. Consultar Ruta Asignada
   ↓
6. Asignar Camión a Tramo
   ↓
7. Listar/Consultar Tramos y Solicitudes
```

**⚠️ CRÍTICO:** No puedes asignar una ruta sin antes tener una solicitud creada.

---

### 🔄 3. Variables Dinámicas

Las variables se actualizan automáticamente con los **scripts de Tests** en cada request.

#### Cómo verificar las variables:

1. Click en el ícono del **ojo** (👁️) en la esquina superior derecha
2. Verás las variables de colección con sus valores actuales

#### Variables más importantes:

| Variable | Se actualiza en | Uso |
|----------|----------------|-----|
| `cliente_id` | Registrar Cliente | Crear solicitudes |
| `solicitud_id` | Crear Solicitud | Todos los endpoints de rutas |
| `contenedor_id` | Crear Solicitud | Tracking del contenedor |
| `ruta_tentativa` | Consultar Rutas Tentativas | Asignar ruta |
| `tramo_id` | Asignar Ruta | Asignar camión |

**💡 Tip:** Si una variable está vacía, vuelve al endpoint que la genera.

---

### 🔢 4. IDs Válidos

#### Cliente ID

**Opción 1 - Usar cliente existente:**
```json
{
  "clienteId": 1  // Asume que ya existe
}
```

**Opción 2 - Crear nuevo cliente:**
1. Ejecuta `Registrar Cliente`
2. El `cliente_id` se guarda automáticamente
3. Se usa en las siguientes solicitudes

#### Camión ID

Para `Asignar Camión a Tramo`, necesitas un camión existente en el servicio de logística.

**Validación automática:**
El endpoint valida que:
- El camión existe
- Tiene capacidad de peso suficiente
- Tiene capacidad de volumen suficiente

**Si no tienes camiones creados:**
1. Usa el ID `1` por defecto (si existe en tu BD)
2. O crea camiones usando el servicio de logística primero

---

### 📝 5. Formato JSON - Encoding

**IMPORTANTE:** Los caracteres especiales pueden causar errores.

#### ✅ Correcto:
```json
{
  "descripcionContenedor": "Equipos electronicos sensibles"
}
```

#### ❌ Incorrecto (puede fallar):
```json
{
  "descripcionContenedor": "Equipos electrónicos sensibles"  // ó con tilde
}
```

**Solución:** 
- Evita tildes y caracteres especiales en el JSON
- O asegúrate de que Postman esté configurado en UTF-8

---

### 🔄 6. Estados de Solicitud

Las solicitudes cambian de estado automáticamente:

```
PENDIENTE (inicial)
    ↓
RUTA_ASIGNADA (después de asignar ruta)
    ↓
EN_PROCESO (cuando inicia el transporte)
    ↓
COMPLETADA (al finalizar)
```

**⚠️ Restricciones:**
- No puedes asignar una ruta a una solicitud que ya tiene ruta
- Para probar de nuevo, crea una nueva solicitud

---

### 🚦 7. Estados de Tramo

Los tramos también tienen estados:

```
PENDIENTE (creado)
    ↓
ASIGNADO (después de asignar camión)
    ↓
EN_CURSO (cuando se inicia)
    ↓
COMPLETADO (cuando finaliza)
```

**⚠️ Restricción:** Solo puedes asignar camión a tramos en estado `PENDIENTE`.

---

### 🧪 8. Validaciones Automáticas

Algunos endpoints tienen **validaciones automáticas**:

#### Asignar Camión a Tramo:
- ✅ Valida que el camión existe (consulta al servicio de logística)
- ✅ Valida capacidad de peso
- ✅ Valida capacidad de volumen
- ✅ Valida que el tramo esté en estado PENDIENTE

**Errores comunes:**
```json
{
  "message": "El camión no soporta el peso del contenedor. Capacidad: 25000 kg, Contenedor: 30000 kg"
}
```

**Solución:** Usa un camión con mayor capacidad o reduce el peso del contenedor.

---

### 📍 9. Coordenadas Geográficas

Las coordenadas deben ser **válidas** para Argentina:

**Formato correcto:**
```json
{
  "latitudOrigen": -34.6037,      // Negativo para Sur
  "longitudOrigen": -58.3816,     // Negativo para Oeste
  "latitudDestino": -34.5601,
  "longitudDestino": -58.4558
}
```

**Ejemplos de ubicaciones:**
- Buenos Aires: `-34.6037, -58.3816`
- Córdoba: `-31.4201, -64.1888`
- Rosario: `-32.9468, -60.6393`

---

### 🔍 10. Debugging - Ver Respuestas

#### En la consola de Postman:
1. Abre **Console** (View → Show Postman Console)
2. Ejecuta el request
3. Verás:
   - Request completo
   - Response completo
   - Scripts ejecutados
   - Variables actualizadas

#### Mensajes útiles en Console:
```javascript
✓ Token de cliente guardado
✓ Cliente creado con ID: 1
✓ Solicitud: SOL-20251107153045
✓ Ruta asignada con ID: 15
```

---

### 🎯 11. Pruebas Paso a Paso

**Primera vez - Flujo completo:**

1. ✅ **Login Cliente**
2. ✅ **Registrar Cliente** (anota el ID)
3. ✅ **Login Operador**
4. ✅ **Crear Solicitud** (como cliente)
5. ✅ **Consultar Rutas Tentativas** (como operador)
6. ✅ **Asignar Ruta** (como operador)
7. ✅ **Asignar Camión a Tramo** (como operador)
8. ✅ **Consultar Tracking** (como cliente)

---

### 🔧 12. Troubleshooting

#### Error: "Solicitud no encontrada"
**Causa:** La `solicitud_id` es inválida o vacía
**Solución:** Ejecuta `Crear Solicitud de Transporte` primero

#### Error: "Ruta no encontrada"
**Causa:** No se ha asignado ruta a la solicitud
**Solución:** Ejecuta `Asignar Ruta a Solicitud` primero

#### Error: "Tramo no encontrado"
**Causa:** La `tramo_id` es inválida o vacía
**Solución:** Ejecuta `Asignar Ruta a Solicitud` que genera los tramos

#### Error: "Cliente no encontrado"
**Causa:** El `cliente_id` no existe
**Solución:** Ejecuta `Registrar Cliente` o usa `cliente_id = 1`

#### Error: "La solicitud ya tiene una ruta asignada"
**Causa:** Intentas asignar una segunda ruta
**Solución:** Crea una nueva solicitud o consulta la ruta existente

#### Error 500: "Error interno del servidor"
**Posibles causas:**
- Servicios no están corriendo
- Base de datos no está disponible
- Error en el código backend

**Solución:**
1. Verifica que los servicios estén corriendo: `docker-compose ps`
2. Revisa los logs: `docker-compose logs solicitudes-service`
3. Reinicia si es necesario: `docker-compose restart`

---

### 📱 13. Servicios Externos

#### Google Maps API (opcional)

El servicio usa Google Maps para calcular distancias reales.

**Sin API Key:**
- ✅ Usa fórmula de Haversine (aproximada)
- ⚠️ Distancias pueden ser menos precisas

**Con API Key:**
- ✅ Distancias exactas usando rutas reales
- ✅ Considera tráfico y tipo de camino

**Configurar API Key:**
```yaml
# application.yml del solicitudes-service
google:
  maps:
    api-key: TU_API_KEY_AQUI
```

---

### 🎨 14. Tips de Productividad

#### Usar Variables en el Body:

✅ **Correcto:**
```json
{
  "clienteId": {{cliente_id}},
  "solicitudId": {{solicitud_id}}
}
```

❌ **Incorrecto:**
```json
{
  "clienteId": 1,  // Hardcodeado
  "solicitudId": 5  // Hardcodeado
}
```

#### Crear un Environment:

1. Click en **Environments** (izquierda)
2. Crea un nuevo environment: "Desarrollo Local"
3. Agrega las variables base
4. Selecciónalo en el dropdown (arriba a la derecha)

#### Guardar Ejemplos:

Después de ejecutar exitosamente un endpoint:
1. Click en **Save Response**
2. Dale un nombre: "Ejemplo exitoso"
3. Se guardará como ejemplo en la colección

---

### 🔐 15. Seguridad

**NUNCA compartas:**
- ❌ Tokens JWT activos
- ❌ Client secrets reales
- ❌ API Keys de producción

**Para pruebas locales:**
- ✅ Usa las credenciales de desarrollo
- ✅ Los tokens son válidos solo localmente
- ✅ Expiran automáticamente

---

## 📋 Checklist Antes de Probar

```
□ Servicios corriendo (docker-compose up -d)
□ Colección importada en Postman
□ Variables de entorno verificadas
□ Token obtenido (Login ejecutado)
□ Cliente creado o ID válido disponible
□ Orden de endpoints claro
□ Console de Postman abierta para debugging
```

---

## 🆘 Soporte Rápido

**Si algo no funciona:**

1. ✅ Verifica los servicios: `docker-compose ps`
2. ✅ Revisa los logs: `docker-compose logs -f solicitudes-service`
3. ✅ Verifica las variables en Postman (ícono del ojo 👁️)
4. ✅ Revisa la consola de Postman
5. ✅ Ejecuta login de nuevo (token expirado)
6. ✅ Verifica el orden de ejecución

---

## 🎯 Ejemplo de Flujo Exitoso

```bash
# Terminal 1: Iniciar servicios
cd BackendTPI-main
docker-compose up -d

# Esperar 30 segundos

# Postman:
1. Login Cliente ✓
2. Registrar Cliente ✓
3. Login Operador ✓
4. Crear Solicitud ✓
5. Consultar Rutas Tentativas ✓
6. Asignar Ruta ✓
7. Asignar Camión a Tramo ✓
8. Consultar Tracking ✓

# ¡Todo funcionando! 🎉
```

---

**¡Ya estás listo para probar todos los endpoints!** 🚀

