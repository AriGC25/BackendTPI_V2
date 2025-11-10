# 🚚 Guía Completa de Prueba - TRANSPORTISTA

Esta guía te ayudará a probar el flujo completo del rol TRANSPORTISTA paso a paso.

## 📋 Flujo Completo del Sistema

```
1. CLIENTE → Crea solicitud de transporte
2. OPERADOR → Consulta rutas tentativas  
3. OPERADOR → Asigna ruta a la solicitud
4. OPERADOR → Asigna camión al tramo ← EL TRAMO PASA A "ASIGNADO"
5. TRANSPORTISTA → Ve sus tramos asignados
6. TRANSPORTISTA → Inicia el tramo ← EL TRAMO PASA A "EN_CURSO"
7. TRANSPORTISTA → Finaliza el tramo ← EL TRAMO PASA A "COMPLETADO"
```

---

## 🔐 PASO 0: Autenticación

### Login CLIENTE
```http
POST http://localhost:8080/realms/transportista-realm/protocol/openid-connect/token
Content-Type: application/x-www-form-urlencoded

client_id=backend-client&client_secret=backend-client-secret&grant_type=password&username=cliente1&password=cliente123
```
**Guardar el `access_token` como `{{token_cliente}}`**

### Login OPERADOR
```http
POST http://localhost:8080/realms/transportista-realm/protocol/openid-connect/token
Content-Type: application/x-www-form-urlencoded

client_id=backend-client&client_secret=backend-client-secret&grant_type=password&username=operador1&password=operador123
```
**Guardar el `access_token` como `{{token_operador}}`**

### Login TRANSPORTISTA
```http
POST http://localhost:8080/realms/transportista-realm/protocol/openid-connect/token
Content-Type: application/x-www-form-urlencoded

client_id=backend-client&client_secret=backend-client-secret&grant_type=password&username=transportista1&password=transportista123
```
**Guardar el `access_token` como `{{token_transportista}}`**

---

## 👤 PASO 1: CLIENTE - Crear Solicitud

```http
POST http://localhost:8000/solicitudes
Authorization: Bearer {{token_cliente}}
Content-Type: application/json

{
  "clienteId": 1,
  "pesoContenedor": 18500.75,
  "volumenContenedor": 45.5,
  "descripcionContenedor": "Equipos electronicos - Prueba Transportista",
  "direccionOrigen": "Av. Corrientes 1000, Buenos Aires",
  "latitudOrigen": -34.6037,
  "longitudOrigen": -58.3816,
  "direccionDestino": "Av. Libertador 5000, Buenos Aires",
  "latitudDestino": -34.5601,
  "longitudDestino": -58.4558
}
```

**Respuesta esperada:** Status 201
**Guardar:** `solicitud_id`, `contenedor_id`

---

## ⚙️ PASO 2: OPERADOR - Consultar Rutas Tentativas

```http
GET http://localhost:8000/rutas/tentativas/{{solicitud_id}}
Authorization: Bearer {{token_operador}}
```

**Respuesta esperada:** Array de opciones de rutas
**Guardar:** Todo el objeto de la primera ruta como `{{ruta_tentativa}}`

---

## ⚙️ PASO 3: OPERADOR - Asignar Ruta

```http
POST http://localhost:8000/rutas/asignar/{{solicitud_id}}
Authorization: Bearer {{token_operador}}
Content-Type: application/json

{{ruta_tentativa}}
```

**Respuesta esperada:** Ruta con tramos creados
**Guardar:** `ruta_id` y `tramo_id` (del primer tramo)

---

## 🚛 PASO 4: OPERADOR - Crear Camión (si no existe)

```http
POST http://localhost:8000/camiones
Authorization: Bearer {{token_operador}}
Content-Type: application/json

{
  "patente": "ABC123",
  "modelo": "Mercedes-Benz Actros",
  "capacidadPeso": 25000.0,
  "capacidadVolumen": 80.0,
  "disponible": true
}
```

**Respuesta esperada:** Status 201
**Guardar:** `camion_id`

---

## ⚙️ PASO 5: OPERADOR - Asignar Camión al Tramo ⭐ IMPORTANTE

Este paso cambia el estado del tramo de **PENDIENTE** a **ASIGNADO**

```http
PUT http://localhost:8000/tramos/{{tramo_id}}/asignar-camion?camionId={{camion_id}}&transportistaId=transportista1
Authorization: Bearer {{token_operador}}
```

**Respuesta esperada:** 
```json
{
  "id": 1,
  "estado": "ASIGNADO",
  "camionId": 1,
  "transportistaId": "transportista1",
  ...
}
```

**✅ AHORA EL TRAMO ESTÁ EN ESTADO "ASIGNADO"**

---

## 🚚 PASO 6: TRANSPORTISTA - Ver Mis Tramos Asignados

```http
GET http://localhost:8000/tramos/transportista/transportista1
Authorization: Bearer {{token_transportista}}
```

**Respuesta esperada:** Array con los tramos asignados a transportista1
```json
[
  {
    "id": 1,
    "estado": "ASIGNADO",
    "camionId": 1,
    "transportistaId": "transportista1",
    "direccionOrigen": "...",
    "direccionDestino": "...",
    ...
  }
]
```

**✅ AHORA EL TRANSPORTISTA VE SUS TRAMOS**

---

## 🚚 PASO 7: TRANSPORTISTA - Iniciar Tramo ⭐

Este paso cambia el estado del tramo de **ASIGNADO** a **EN_CURSO**

```http
PUT http://localhost:8000/tramos/{{tramo_id}}/iniciar
Authorization: Bearer {{token_transportista}}
```

**Respuesta esperada:**
```json
{
  "id": 1,
  "estado": "EN_CURSO",
  "fechaInicio": "2025-11-08T02:30:00",
  ...
}
```

**✅ AHORA EL TRAMO ESTÁ EN_CURSO**

---

## 🚚 PASO 8: TRANSPORTISTA - Finalizar Tramo ⭐

Este paso cambia el estado del tramo de **EN_CURSO** a **COMPLETADO**

```http
PUT http://localhost:8000/tramos/{{tramo_id}}/finalizar
Authorization: Bearer {{token_transportista}}
```

**Respuesta esperada:**
```json
{
  "id": 1,
  "estado": "COMPLETADO",
  "fechaInicio": "2025-11-08T02:30:00",
  "fechaFin": "2025-11-08T03:45:00",
  ...
}
```

**✅ TRAMO COMPLETADO**

---

## 🔍 Verificaciones Adicionales

### Ver detalle de un tramo específico
```http
GET http://localhost:8000/tramos/{{tramo_id}}
Authorization: Bearer {{token_transportista}}
```

### Ver solo tramos ASIGNADOS
```http
GET http://localhost:8000/tramos/transportista/transportista1?estado=ASIGNADO
Authorization: Bearer {{token_transportista}}
```

### Ver solo tramos EN_CURSO
```http
GET http://localhost:8000/tramos/transportista/transportista1?estado=EN_CURSO
Authorization: Bearer {{token_transportista}}
```

### Ver solo tramos COMPLETADOS
```http
GET http://localhost:8000/tramos/transportista/transportista1?estado=COMPLETADO
Authorization: Bearer {{token_transportista}}
```

---

## ❌ Errores Comunes y Soluciones

### Error: "Solo se pueden iniciar tramos en estado ASIGNADO"
**Causa:** El tramo no está en estado ASIGNADO
**Solución:** Ejecutar PASO 5 (Asignar Camión al Tramo)

### Error: "Solo se pueden finalizar tramos en estado EN_CURSO"
**Causa:** El tramo no está en estado EN_CURSO
**Solución:** Ejecutar PASO 7 (Iniciar Tramo)

### Error: Array vacío al ver tramos asignados
**Causa:** No hay tramos asignados a ese transportista
**Solución:** Ejecutar PASOS 1-5 para crear y asignar un tramo

### Error: 403 Forbidden al asignar camión
**Causa:** Comunicación entre servicios bloqueada
**Solución:** Verificar que los servicios se hayan reiniciado correctamente:
```bash
docker-compose restart logistica-service solicitudes-service
```

### Error: 401 Unauthorized
**Causa:** Token expirado o rol incorrecto
**Solución:** Generar un nuevo token (PASO 0)

---

## 📝 Notas Importantes

1. **Los tramos solo aparecen después de asignar una ruta** (PASO 3)
2. **El camión debe tener capacidad suficiente** para el contenedor
3. **Solo el transportista asignado** puede ver, iniciar y finalizar sus tramos
4. **El flujo de estados es estricto:**
   - PENDIENTE → ASIGNADO (asignar camión)
   - ASIGNADO → EN_CURSO (iniciar tramo)
   - EN_CURSO → COMPLETADO (finalizar tramo)

---

## 🎯 Resumen Rápido

Si quieres probar rápidamente el flujo completo:

1. ✅ Login como los 3 roles
2. ✅ Cliente crea solicitud
3. ✅ Operador consulta rutas y asigna una
4. ✅ Operador crea/obtiene un camión
5. ⭐ **Operador asigna camión al tramo** ← CRÍTICO
6. ✅ Transportista ve sus tramos (debería aparecer 1)
7. ✅ Transportista inicia el tramo
8. ✅ Transportista finaliza el tramo

**El paso 5 es el más importante** - sin él, el transportista no verá ningún tramo asignado.

