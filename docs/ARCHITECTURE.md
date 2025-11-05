# Arquitectura del Sistema - Backend TPI 2025

## Vista General

El sistema implementa una arquitectura de microservicios completa para gestión de transporte de contenedores.

## Componentes Principales

### 1. API Gateway (Puerto 8000)
**Responsabilidades:**
- Punto de entrada único para todas las peticiones
- Enrutamiento inteligente hacia microservicios
- Validación de tokens JWT
- CORS habilitado

**Tecnologías:**
- Spring Cloud Gateway
- OAuth2 Resource Server

**Rutas:**
```
/api/solicitudes/** → Solicitudes Service (8081)
/api/logistica/**   → Logística Service (8082)
/api/tarifas/**     → Tarifas Service (8083)
/api/tracking/**    → Tracking Service (8084)
```

### 2. Solicitudes Service (Puerto 8081)
**Responsabilidades:**
- Gestión de solicitudes de transporte
- Administración de contenedores
- Gestión de rutas y tramos
- Coordinación de traslados

**Entidades:**
- Contenedor
- Solicitud
- Ruta
- Tramo

**Endpoints Principales:**
```
POST   /solicitudes                    - Crear solicitud (CLIENTE/OPERADOR)
GET    /solicitudes                    - Listar todas (OPERADOR)
GET    /solicitudes/{id}               - Obtener detalle (CLIENTE/OPERADOR/TRANSPORTISTA)
GET    /solicitudes/numero/{numero}    - Obtener por número
GET    /solicitudes/estado/{estado}    - Filtrar por estado (OPERADOR)
GET    /solicitudes/{id}/costo         - Calcular costo total (CLIENTE/OPERADOR) ✨ NUEVO
GET    /solicitudes/{id}/tiempo-estimado - Calcular tiempo estimado (CLIENTE/OPERADOR) ✨ NUEVO
PUT    /solicitudes/{id}/estado        - Actualizar estado (OPERADOR) ✨ NUEVO

# Rutas - ✨ COMPLETAMENTE NUEVOS
GET    /rutas/tentativas/{solicitudId} - Consultar rutas tentativas (OPERADOR)
POST   /rutas/asignar/{solicitudId}    - Asignar ruta a solicitud (OPERADOR)
GET    /rutas/solicitud/{solicitudId}  - Obtener ruta por solicitud (OPERADOR/CLIENTE)

# Tramos - ✨ COMPLETAMENTE NUEVOS
GET    /tramos                         - Listar todos los tramos (OPERADOR)
GET    /tramos/transportista/{id}      - Ver tramos asignados (OPERADOR/TRANSPORTISTA)
POST   /tramos                         - Crear tramo (OPERADOR)
GET    /tramos/{id}                    - Obtener tramo por ID (OPERADOR/TRANSPORTISTA)
PUT    /tramos/{id}/asignar-camion     - Asignar camión a tramo (OPERADOR)
PUT    /tramos/{id}/iniciar            - Iniciar tramo (OPERADOR/TRANSPORTISTA)
PUT    /tramos/{id}/finalizar          - Finalizar tramo (OPERADOR/TRANSPORTISTA)
```

### 3. Logística Service (Puerto 8082)
**Responsabilidades:**
- Gestión de depósitos
- Administración de camiones
- Control de disponibilidad
- Gestión de capacidades

**Entidades:**
- Depósito (ubicación, coordenadas, capacidad)
- Camión (patente, transportista, capacidades)

**Endpoints Principales:**
```
GET    /depositos            - Listar depósitos
GET    /depositos/activos    - Depósitos disponibles
POST   /depositos            - Crear depósito (OPERADOR)
GET    /camiones/disponibles - Camiones libres
POST   /camiones             - Registrar camión (OPERADOR)
```

### 4. Tarifas Service (Puerto 8083)
**Responsabilidades:**
- Gestión de clientes
- Configuración de tarifas
- Cálculo de costos
- Integración con Google Maps (preparado)

**Entidades:**
- Cliente (datos personales y contacto)
- Tarifa (configuración de costos)

**Endpoints Principales:**
```
# Clientes
POST   /clientes             - Crear cliente (OPERADOR)
GET    /clientes             - Listar clientes (OPERADOR)
GET    /clientes/{id}        - Obtener cliente por ID (OPERADOR)
PUT    /clientes/{id}        - Actualizar cliente (OPERADOR)
DELETE /clientes/{id}        - Eliminar cliente (OPERADOR)

# Tarifas - ✨ COMPLETAMENTE NUEVOS
GET    /tarifas              - Listar todas las tarifas (OPERADOR)
GET    /tarifas/activas      - Listar tarifas activas (OPERADOR)
GET    /tarifas/{id}         - Obtener tarifa por ID (OPERADOR)
GET    /tarifas/tipo/{tipo}  - Obtener tarifa por tipo de tramo (OPERADOR)
POST   /tarifas              - Crear nueva tarifa (OPERADOR)
PUT    /tarifas/{id}         - Actualizar tarifa (OPERADOR)
DELETE /tarifas/{id}         - Eliminar tarifa (OPERADOR)
```

### 5. Tracking Service (Puerto 8084)
**Responsabilidades:**
- Seguimiento en tiempo real
- Registro de eventos
- Historial de estados
- Notificaciones de cambios

**Entidades:**
- TrackingEvento (eventos de cambio de estado)

**Endpoints Principales:**
```
GET    /tracking/contenedor/{id}  - Historial por contenedor
GET    /tracking/solicitud/{id}   - Historial por solicitud
```

## Infraestructura

### PostgreSQL (Puerto 5432)
**Base de datos única compartida:**
- `transportista_db` - Base principal
- `keycloak_db` - Base para Keycloak

**Esquemas por servicio:**
```
Tables:
├── clientes            (tarifas-service)
├── tarifas             (tarifas-service)
├── depositos           (logistica-service)
├── camiones            (logistica-service)
├── contenedores        (solicitudes-service)
├── solicitudes         (solicitudes-service)
├── rutas               (solicitudes-service)
├── tramos              (solicitudes-service)
└── tracking_eventos    (tracking-service)
```

### Keycloak (Puerto 8080)
**Servidor de autenticación y autorización:**

**Realm:** `transportista-realm`

**Roles definidos:**
1. **CLIENTE**
   - Crear solicitudes
   - Ver sus solicitudes
   - Consultar tracking

2. **OPERADOR**
   - CRUD completo de todas las entidades
   - Asignar rutas
   - Asignar camiones
   - Gestionar clientes

3. **TRANSPORTISTA**
   - Ver tramos asignados
   - Actualizar estado de tramos
   - Registrar inicio/fin de traslados

**Flujo de autenticación:**
```
1. Cliente solicita token a Keycloak
   POST /realms/transportista-realm/protocol/openid-connect/token
   
2. Keycloak valida credenciales y retorna JWT
   
3. Cliente incluye JWT en header Authorization
   
4. API Gateway valida JWT con Keycloak
   
5. Microservicio valida roles y permisos
   
6. Respuesta al cliente
```

## Modelo de Datos Completo

### Relaciones Entre Entidades

```
Cliente (1) ──────< (N) Solicitud
                         │
                         │ (1)
                         ▼
                    Contenedor (1:1 con Solicitud)
                         │
                         │
Solicitud (1) ──────< (1) Ruta
                         │
                         │ (1)
                         ▼
                    Tramo (N)
                         │
                         │ (N:1)
                         ▼
                    Camión (N:1)
                    
Depósito ────────< Tramo (como origen/destino)

Solicitud (1) ────< (N) TrackingEvento
Contenedor (1) ───< (N) TrackingEvento
```

## Flujos de Negocio Principales

### 1. Crear Nueva Solicitud
```
Cliente → API Gateway → Solicitudes Service
    ↓
Crear Contenedor
    ↓
Crear Solicitud con estado PENDIENTE
    ↓
Registrar evento en Tracking Service
    ↓
Retornar número de solicitud
```

### 2. Asignar Ruta (Operador)
```
Operador → API Gateway → Solicitudes Service
    ↓
Consultar Depósitos disponibles (Logística Service)
    ↓
Consultar Tarifas (Tarifas Service)
    ↓
Calcular distancias (Google Maps - futuro)
    ↓
Crear Ruta con múltiples Tramos
    ↓
Calcular costo total
    ↓
Actualizar Solicitud (estado: RUTA_ASIGNADA)
```

### 3. Asignar Camión a Tramo (Operador)
```
Operador → API Gateway → Solicitudes Service
    ↓
Consultar Camiones disponibles (Logística Service)
    ↓
Validar capacidad del camión
    ↓
Asignar Camión al Tramo
    ↓
Marcar Camión como no disponible
    ↓
Actualizar estado Tramo (ASIGNADO)
```

### 4. Seguimiento de Contenedor (Cliente)
```
Cliente → API Gateway → Tracking Service
    ↓
Consultar eventos por contenedorId
    ↓
Retornar historial completo
    ↓
Mostrar estado actual y ubicación
```

## Seguridad

### Capas de Seguridad

1. **API Gateway Level:**
   - Validación de JWT
   - Rate limiting (futuro)
   - CORS configuration

2. **Service Level:**
   - OAuth2 Resource Server
   - Role-based access control
   - Method-level security (@PreAuthorize)

3. **Data Level:**
   - JPA validations
   - Database constraints
   - Transaction management

### Configuración de Seguridad por Endpoint

```java
// Ejemplo de configuración típica
@PreAuthorize("hasRole('OPERADOR')")  // Solo operadores
@PreAuthorize("hasAnyRole('CLIENTE', 'OPERADOR')")  // Múltiples roles
@PreAuthorize("hasRole('TRANSPORTISTA')")  // Solo transportistas
```

## Patrones de Diseño Aplicados

### 1. API Gateway Pattern
- Punto de entrada único
- Routing transparente
- Autenticación centralizada

### 2. Repository Pattern
- Abstracción de acceso a datos
- Spring Data JPA

### 3. DTO Pattern
- Transferencia de datos entre capas
- Separación de modelo de dominio

### 4. Service Layer Pattern
- Lógica de negocio centralizada
- Transacciones declarativas

### 5. Database per Service (adaptado)
- Una base de datos compartida
- Esquemas lógicos por servicio
- Separación de responsabilidades

## Escalabilidad

### Estrategias de Escalado

**Horizontal Scaling Ready:**
- Cada microservicio es stateless
- Base de datos compartida puede ser replicada
- Load balancer antes del API Gateway (futuro)

**Vertical Scaling:**
- Ajustar recursos de contenedores Docker
- Optimización de queries JPA
- Connection pooling configurado

### Performance Considerations

1. **Database:**
   - Índices en columnas de búsqueda frecuente
   - Lazy loading en relaciones
   - Open-in-view: false

2. **Caching (futuro):**
   - Redis para datos frecuentes
   - Cache de tarifas
   - Cache de depósitos

3. **Async Processing (futuro):**
   - Message queue para tracking events
   - Async notifications

## Monitoreo y Logs

### Actuator Endpoints
Todos los servicios exponen:
```
/actuator/health  - Estado del servicio
/actuator/info    - Información del servicio
/actuator/metrics - Métricas de performance
```

### Logging Configuration
- Console output con formato estructurado
- Nivel DEBUG para desarrollo
- Nivel INFO para producción (recomendado)

## Documentación

### OpenAPI/Swagger
Cada servicio expone documentación interactiva:
- `/swagger-ui.html` - Interfaz visual
- `/v3/api-docs` - JSON specification

### Colección Postman
- Flujos completos de prueba
- Autenticación automática
- Variables de entorno configurables

## Deployment

### Docker Compose
Orquestación completa con:
- Health checks
- Dependency management
- Volume persistence
- Network isolation

### Production Considerations (futuro)
- Kubernetes deployment
- Secrets management (Vault)
- External configuration (Config Server)
- Distributed tracing (Zipkin)
- Centralized logging (ELK Stack)

## Extensibilidad

El sistema está diseñado para ser extendido fácilmente:

1. **Agregar nuevo microservicio:**
   - Crear módulo Maven
   - Configurar Spring Boot
   - Agregar ruta en API Gateway
   - Dockerizar

2. **Agregar nueva entidad:**
   - Crear entity class
   - Crear repository
   - Crear service layer
   - Crear controller

3. **Integración con servicios externos:**
   - WebClient ya configurado
   - Google Maps listo para integrar
   - Notification service (preparado)

## Conclusión

Esta arquitectura proporciona:
- ✅ Separación de responsabilidades
- ✅ Escalabilidad horizontal
- ✅ Seguridad robusta
- ✅ Fácil mantenimiento
- ✅ Documentación completa
- ✅ Testing facilitado
- ✅ Deployment automatizado
