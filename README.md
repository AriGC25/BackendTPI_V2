# 🚚 Sistema de Gestión de Transporte de Contenedores

Sistema backend basado en microservicios para la gestión integral de transporte de contenedores, desarrollado con Spring Boot y arquitectura de microservicios.

## 📋 Tabla de Contenidos

- [Descripción General](#-descripción-general)
- [Arquitectura](#-arquitectura)
- [Tecnologías Utilizadas](#-tecnologías-utilizadas)
- [Requisitos Previos](#-requisitos-previos)
- [Instalación y Ejecución](#-instalación-y-ejecución)
- [Documentación Swagger](#-documentación-swagger)
- [Servicios](#-servicios)
- [Autenticación y Autorización](#-autenticación-y-autorización)
- [Base de Datos](#-base-de-datos)

---

## 🎯 Descripción General

Este sistema permite gestionar el ciclo completo de transporte de contenedores, desde la solicitud inicial hasta el seguimiento en tiempo real, incluyendo:

- ✅ Gestión de solicitudes de transporte
- ✅ Administración de rutas y tramos
- ✅ Control de flota de camiones
- ✅ Cálculo de tarifas
- ✅ Seguimiento en tiempo real (tracking)
- ✅ Autenticación y autorización con Keycloak
- ✅ Control de acceso basado en roles (RBAC)

---

## 🏗️ Arquitectura

El sistema implementa una **arquitectura de microservicios** con los siguientes componentes:

```
┌─────────────────┐
│   API Gateway   │ ← Punto de entrada único (Puerto 8000)
│   (Spring GW)   │
└────────┬────────┘
         │
    ┌────┴─────────────────────────────┐
    │                                  │
┌───▼────────┐  ┌──────────┐  ┌──────▼──────┐  ┌──────────┐
│Solicitudes │  │Logística │  │  Tarifas    │  │ Tracking │
│  Service   │  │ Service  │  │   Service   │  │ Service  │
│  :8081     │  │  :8082   │  │    :8083    │  │  :8084   │
└────────────┘  └──────────┘  └─────────────┘  └──────────┘
         │            │              │                │
         └────────────┴──────────────┴────────────────┘
                            │
                    ┌───────▼────────┐
                    │   PostgreSQL   │
                    │     :5432      │
                    └────────────────┘
```

**Keycloak (Puerto 8080)** - Gestión de autenticación y autorización

---

## 🛠️ Tecnologías Utilizadas

### Backend
- **Java 17** - Lenguaje de programación
- **Spring Boot 3.2.5** - Framework principal
- **Spring Cloud Gateway** - API Gateway
- **Spring Security** - Seguridad y autenticación
- **Spring Data JPA** - Persistencia de datos
- **Hibernate** - ORM

### Seguridad
- **Keycloak 24.0.3** - Identity and Access Management
- **OAuth 2.0** / **JWT** - Autenticación basada en tokens

### Base de Datos
- **PostgreSQL 15** - Base de datos relacional

### Documentación
- **Springdoc OpenAPI 2.5.0** - Documentación Swagger/OpenAPI

### DevOps
- **Docker** & **Docker Compose** - Containerización
- **Maven** - Gestión de dependencias y build

---

## 📦 Requisitos Previos

- **Java 17** o superior
- **Maven 3.8+**
- **Docker** y **Docker Compose**
- **Git**

---

## 🚀 Instalación y Ejecución

### 1. Clonar el Repositorio

```bash
git clone <repository-url>
cd BackendTPI-main
```

### 2. Compilar el Proyecto

```bash
mvn clean package
```

### 3. Levantar los Servicios con Docker

```bash
docker-compose up -d
```

### 4. Verificar que los Servicios Están Corriendo

```bash
docker-compose ps
```

Deberías ver todos los servicios en estado `Up`:
- ✅ transportista-postgres
- ✅ transportista-keycloak
- ✅ transportista-gateway
- ✅ transportista-solicitudes
- ✅ transportista-logistica
- ✅ transportista-tarifas
- ✅ transportista-tracking

### 5. Acceder al Sistema

- **API Gateway**: http://localhost:8000
- **Keycloak Admin**: http://localhost:8080/admin (admin/admin)

---

## 📚 Documentación Swagger

Cada microservicio expone su propia documentación Swagger/OpenAPI interactiva:

### 🔷 API Gateway
**No tiene Swagger propio** - Actúa como proxy hacia los demás servicios

**URL Base**: `http://localhost:8000`

---

### 🔷 Solicitudes Service

**Swagger UI**: [http://localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html)

**OpenAPI JSON**: [http://localhost:8081/v3/api-docs](http://localhost:8081/v3/api-docs)

**Acceso a través del Gateway**: `http://localhost:8000/api/solicitudes/**`

**Endpoints Principales**:
- `POST /solicitudes` - Crear solicitud
- `GET /solicitudes` - Listar todas
- `GET /solicitudes/{id}` - Obtener por ID
- `GET /solicitudes/{id}/costo` - Calcular costo total
- `PUT /solicitudes/{id}/estado` - Actualizar estado
- `POST /rutas` - Crear ruta
- `POST /tramos` - Crear tramo
- `PUT /tramos/{id}/asignar` - Asignar camión a tramo

---

### 🔷 Logística Service

**Swagger UI**: [http://localhost:8082/swagger-ui.html](http://localhost:8082/swagger-ui.html)

**OpenAPI JSON**: [http://localhost:8082/v3/api-docs](http://localhost:8082/v3/api-docs)

**Acceso a través del Gateway**: `http://localhost:8000/api/logistica/**`

**Endpoints Principales**:
- `GET /camiones` - Listar todos los camiones
- `GET /camiones/{id}` - Obtener camión por ID
- `POST /camiones` - Crear nuevo camión
- `PUT /camiones/{id}` - Actualizar camión
- `DELETE /camiones/{id}` - Eliminar camión
- `GET /camiones/transportista/{id}` - Listar por transportista

---

### 🔷 Tarifas Service

**Swagger UI**: [http://localhost:8083/swagger-ui.html](http://localhost:8083/swagger-ui.html)

**OpenAPI JSON**: [http://localhost:8083/v3/api-docs](http://localhost:8083/v3/api-docs)

**Acceso a través del Gateway**: `http://localhost:8000/api/tarifas/**`

**Endpoints Principales**:
- `GET /tarifas` - Listar todas las tarifas
- `GET /tarifas/{id}` - Obtener tarifa por ID
- `POST /tarifas` - Crear nueva tarifa
- `PUT /tarifas/{id}` - Actualizar tarifa
- `DELETE /tarifas/{id}` - Eliminar tarifa
- `POST /tarifas/calcular` - Calcular tarifa para un trayecto

---

### 🔷 Tracking Service

**Swagger UI**: [http://localhost:8084/swagger-ui.html](http://localhost:8084/swagger-ui.html)

**OpenAPI JSON**: [http://localhost:8084/v3/api-docs](http://localhost:8084/v3/api-docs)

**Acceso a través del Gateway**: `http://localhost:8000/api/tracking/**`

**Endpoints Principales**:
- `GET /ubicaciones/tramo/{tramoId}` - Obtener ubicaciones de un tramo
- `POST /ubicaciones` - Registrar nueva ubicación
- `GET /ubicaciones/{id}` - Obtener ubicación por ID
- `GET /ubicaciones/tramo/{tramoId}/ultima` - Última ubicación del tramo

---

## 🔐 Autenticación y Autorización

El sistema utiliza **Keycloak** como servidor de autenticación y autorización.

### Roles Disponibles

1. **CLIENTE** - Usuario que solicita transportes
2. **OPERADOR** - Administrador del sistema
3. **TRANSPORTISTA** - Conductor/Empresa de transporte

### Obtener Token de Acceso

```bash
curl -X POST 'http://localhost:8080/realms/transportista-realm/protocol/openid-connect/token' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'client_id=transportista-client' \
  -d 'client_secret=YOUR_CLIENT_SECRET' \
  -d 'grant_type=password' \
  -d 'username=YOUR_USERNAME' \
  -d 'password=YOUR_PASSWORD'
```

### Usar el Token en las Peticiones

```bash
curl -X GET 'http://localhost:8000/api/solicitudes' \
  -H 'Authorization: Bearer YOUR_ACCESS_TOKEN'
```

### Acceso a Swagger con Autenticación

1. Obtén un token de acceso
2. En Swagger UI, haz clic en el botón **"Authorize"** 🔓
3. Ingresa: `Bearer YOUR_ACCESS_TOKEN`
4. Haz clic en **"Authorize"** y luego **"Close"**
5. Ahora puedes ejecutar las peticiones autenticadas

---

## 🗄️ Base de Datos

### PostgreSQL

**Host**: `localhost`  
**Puerto**: `5432`  
**Base de datos**: `transportista_db`  
**Usuario**: `transportista_user`  
**Contraseña**: `transportista_pass`

### Conexión

```bash
psql -h localhost -p 5432 -U transportista_user -d transportista_db
```

### Esquema

El esquema se inicializa automáticamente con:
- Scripts de inicialización en `database/init-scripts/`
- JPA auto-DDL (Hibernate)

---

## 🔧 Puertos Utilizados

| Servicio | Puerto | Descripción |
|----------|--------|-------------|
| **API Gateway** | 8000 | Punto de entrada principal |
| **Keycloak** | 8080 | Autenticación y autorización |
| **Solicitudes Service** | 8081 | Gestión de solicitudes |
| **Logística Service** | 8082 | Gestión de camiones |
| **Tarifas Service** | 8083 | Gestión de tarifas |
| **Tracking Service** | 8084 | Seguimiento GPS |
| **PostgreSQL** | 5432 | Base de datos |

---

## 📁 Estructura del Proyecto

```
BackendTPI-main/
├── api-gateway/              # API Gateway (Spring Cloud Gateway)
├── solicitudes-service/      # Servicio de solicitudes
├── logistica-service/        # Servicio de logística
├── tarifas-service/          # Servicio de tarifas
├── tracking-service/         # Servicio de tracking
├── database/                 # Scripts de BD
│   └── init-scripts/
├── docs/                     # Documentación
├── keycloak-config/          # Configuración de Keycloak
├── scripts/                  # Scripts de PowerShell para testing
├── docker-compose.yml        # Orquestación de contenedores
└── pom.xml                   # POM padre del proyecto
```

---

## 🧪 Prueba de endpoints en POSTMAN
- OPERADOR: Crear Camión, Depósito, Tarifa.
- CLIENTE: Crear solicitud de transporte
- OPERADOR:
  - Consultar ruta tentativa.
  - Asignar ruta a solicitud.
  - Asignar camión a tramo.
- TRANSPORTISTA:
  - Iniciar tramo.
  - Finalizar tramo.
- CLIENTE: Consultar estado de solicitud.



## 📝 Comandos Útiles

### Maven

```bash
# Compilar todo el proyecto
mvn clean compile

# Empaquetar (genera JARs)
mvn clean package

# Saltar tests
mvn clean package -DskipTests

# Compilar un módulo específico
mvn clean package -pl solicitudes-service
```

### Docker

```bash
# Levantar todos los servicios
docker-compose up -d

# Ver logs de un servicio
docker-compose logs -f solicitudes-service

# Detener todos los servicios
docker-compose down

# Reconstruir y levantar
docker-compose up -d --build

# Ver estado de los servicios
docker-compose ps
```

