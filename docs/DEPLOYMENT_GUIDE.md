# Guía Rápida de Despliegue - Backend TPI 2025

## Inicio Rápido

### Requisitos Previos
- Docker Desktop instalado y ejecutándose
- 8GB de RAM libre recomendado
- Puertos disponibles: 5432, 8000, 8080, 8081, 8082, 8083, 8084

### Pasos para Ejecutar el Sistema

#### 1. Clonar el Repositorio
```bash
git clone https://github.com/AriGC25/BackendTPI.git
cd BackendTPI
```

#### 2. Iniciar Todos los Servicios
```bash
docker-compose up --build
```

**Tiempo de inicio:** Aproximadamente 3-5 minutos

#### 3. Verificar que los Servicios Están Activos

Esperar a ver estos mensajes en los logs:
- ✅ PostgreSQL: "database system is ready to accept connections"
- ✅ Keycloak: "Admin console listening"
- ✅ Todos los microservicios: "Started [ServiceName]Application"

#### 4. Acceder a las Interfaces

| Servicio | URL | Descripción |
|----------|-----|-------------|
| API Gateway | http://localhost:8000 | Punto de entrada único |
| Keycloak Admin | http://localhost:8080/admin | Consola de administración |
| Swagger - Solicitudes | http://localhost:8081/swagger-ui.html | Documentación API |
| Swagger - Logística | http://localhost:8082/swagger-ui.html | Documentación API |
| Swagger - Tarifas | http://localhost:8083/swagger-ui.html | Documentación API |
| Swagger - Tracking | http://localhost:8084/swagger-ui.html | Documentación API |

### Credenciales de Acceso

#### Keycloak Admin
- Usuario: `admin`
- Contraseña: `admin`

#### Usuarios del Sistema

| Usuario | Contraseña | Rol | Permisos |
|---------|-----------|-----|----------|
| cliente1 | cliente123 | CLIENTE | Crear solicitudes, ver tracking |
| operador1 | operador123 | OPERADOR | Acceso completo CRUD |
| transportista1 | transportista123 | TRANSPORTISTA | Ver y actualizar tramos |

### Flujo de Prueba Básico

#### Paso 1: Obtener Token JWT

```bash
curl -X POST "http://localhost:8080/realms/transportista-realm/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=backend-client" \
  -d "client_secret=backend-client-secret" \
  -d "grant_type=password" \
  -d "username=operador1" \
  -d "password=operador123"
```

Copiar el `access_token` de la respuesta.

#### Paso 2: Consultar Depósitos Disponibles

```bash
curl -X GET "http://localhost:8000/api/logistica/depositos" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

#### Paso 3: Consultar Camiones Disponibles

```bash
curl -X GET "http://localhost:8000/api/logistica/camiones/disponibles" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

#### Paso 4: Crear una Solicitud (como Cliente)

Primero, obtener token de cliente:
```bash
curl -X POST "http://localhost:8080/realms/transportista-realm/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=backend-client" \
  -d "client_secret=backend-client-secret" \
  -d "grant_type=password" \
  -d "username=cliente1" \
  -d "password=cliente123"
```

Luego crear solicitud:
```bash
curl -X POST "http://localhost:8000/api/solicitudes/solicitudes" \
  -H "Authorization: Bearer CLIENTE_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{
    "clienteId": 1,
    "pesoContenedor": 10.5,
    "volumenContenedor": 25.0,
    "descripcionContenedor": "Mercadería general",
    "direccionOrigen": "Av. Corrientes 1234, CABA",
    "latitudOrigen": -34.6037,
    "longitudOrigen": -58.3816,
    "direccionDestino": "San Martín 890, Córdoba",
    "latitudDestino": -31.4201,
    "longitudDestino": -64.1888
  }'
```

#### Paso 5: Verificar Tracking

```bash
curl -X GET "http://localhost:8000/api/tracking/tracking/solicitud/1" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

### Usar la Colección de Postman

1. Importar `docs/postman-collection.json` en Postman
2. Ejecutar "Login - Operador" para obtener token automáticamente
3. Los demás requests usarán el token automáticamente
4. Explorar todos los endpoints disponibles

### Datos Precargados

El sistema incluye datos de prueba:

#### 3 Clientes
- Juan Pérez (DNI: 12345678)
- María González (DNI: 23456789)
- Carlos Rodríguez (DNI: 34567890)

#### 4 Depósitos
- Depósito Norte (Buenos Aires)
- Depósito Sur (La Plata)
- Depósito Centro (Rosario)
- Depósito Córdoba

#### 5 Camiones
- AB123CD - Transporte López
- EF456GH - Transportes del Norte
- IJ789KL - Logística Sur
- MN012OP - Cargo Express
- QR345ST - Trans Continental

#### Tarifas Configuradas
- ORIGEN_DEPOSITO: $5.50/km
- DEPOSITO_DEPOSITO: $4.80/km
- DEPOSITO_DESTINO: $5.50/km
- ORIGEN_DESTINO: $6.00/km

### Detener el Sistema

```bash
# Detener servicios
docker-compose down

# Detener y eliminar datos
docker-compose down -v
```

### Resolución de Problemas

#### Los servicios no inician
```bash
# Verificar Docker está corriendo
docker ps

# Ver logs de un servicio específico
docker-compose logs -f [nombre-servicio]

# Reiniciar todo
docker-compose down
docker-compose up --build
```

#### Puerto en uso
Si algún puerto está ocupado, detener el servicio que lo usa o modificar el puerto en `docker-compose.yml`

#### Keycloak no responde
Esperar 1-2 minutos adicionales. Keycloak puede tardar en inicializar.

#### Error de base de datos
```bash
# Recrear volúmenes
docker-compose down -v
docker-compose up --build
```

### Comandos Útiles

```bash
# Ver todos los contenedores
docker-compose ps

# Ver logs en tiempo real
docker-compose logs -f

# Reiniciar un servicio específico
docker-compose restart solicitudes-service

# Acceder al contenedor de PostgreSQL
docker exec -it transportista-postgres psql -U transportista_user -d transportista_db
```

### Variables de Entorno Opcionales

Para habilitar Google Maps API (opcional):
```bash
export GOOGLE_MAPS_API_KEY="tu-api-key-aqui"
docker-compose up --build
```

### Arquitectura del Sistema

```
┌─────────────────┐
│   API Gateway   │ :8000
│  (Entry Point)  │
└────────┬────────┘
         │
    ┌────┴─────────────────────────────┐
    │                                  │
┌───┴──────────┐              ┌───────┴──────┐
│  Keycloak    │              │  PostgreSQL  │
│    :8080     │              │    :5432     │
└──────────────┘              └──────────────┘
                                      │
         ┌────────────────────────────┼─────────────────────┐
         │                            │                     │
    ┌────┴────────┐          ┌────────┴──────┐    ┌────────┴──────┐
    │ Solicitudes │          │   Logística   │    │    Tarifas    │
    │   :8081     │          │     :8082     │    │     :8083     │
    └─────────────┘          └───────────────┘    └───────────────┘
                                      │
                              ┌───────┴────────┐
                              │   Tracking     │
                              │     :8084      │
                              └────────────────┘
```

### Recursos Adicionales

- README completo: `README.md`
- Colección Postman: `docs/postman-collection.json`
- Documentación OpenAPI: Disponible en cada servicio en `/swagger-ui.html`

### Soporte

Para problemas o preguntas, revisar:
1. Los logs con `docker-compose logs`
2. La documentación Swagger de cada servicio
3. El README.md principal

¡El sistema está listo para usar! 🚀
