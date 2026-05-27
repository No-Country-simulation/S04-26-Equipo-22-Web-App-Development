# TalentCircle — Backend API

Servicio centralizado (Core API) para la gestión de comunidades, análisis de datos semanales y procesamiento del pipeline de contenido de TalentCircle. Desarrollado con arquitectura REST limpia, Spring Boot 3 y persistencia relacional.

---

##  Prerrequisitos del Sistema

Antes de iniciar la aplicación, asegúrate de contar con:
- **Java SE Development Kit (JDK) 17** o superior instalado localmente.
- **Docker Desktop** (Obligatorio para la orquestación de servicios y contenedores).
- **Maven 3.8+** (Opcional, se incluye el *wrapper* `./mvnw` en el proyecto).

---

##  Clonación y Configuración del Repositorio

Para descargar el proyecto y posicionarte en la rama estable de producción/entrega, ejecuta en tu terminal:

```bash
# 1. Clonar el repositorio unificado de la simulación
git clone [https://github.com/No-Country-simulation/S04-26-Equipo-22-Web-App-Development.git](https://github.com/No-Country-simulation/S04-26-Equipo-22-Web-App-Development.git)

# 2. Ingresar al directorio del Backend
cd S04-26-Equipo-22-Web-App-Development

# 3. Cambiar a la rama de integración final
git checkout develop
 Guía de Despliegue (Cómo Correr el Proyecto)
Método 1: Orquestación Completa (Frontend + Backend + DB) via Docker 🐋
Este es el método oficial y recomendado para la evaluación en el Demo Day. Construye las imágenes desde los Dockerfile correspondientes y levanta la red interna de intercomunicación.

Asegúrate de tener Docker Desktop abierto.

Posiciónate en la raíz principal del proyecto (donde se encuentra el archivo docker-compose.yml).

Ejecuta el comando de inicialización limpia:

Bash
docker compose down
docker compose up --build -d
Mapeo de Servicios Disponibles:

 Backend REST API: http://localhost:8080

 Documentación Interactiva Swagger UI: http://localhost:8080/swagger-ui/index.html

 Frontend UI Web: http://localhost:5175

Método 2: Ejecución Local Nativa (Spring Boot)
Si deseas ejecutar la API de forma aislada sin contenerizar el servicio de Java:

Configurar Variables de Entorno:
Crea una copia del archivo de configuración en src/main/resources/:

Bash
cp src/main/resources/application.properties.example src/main/resources/application.properties
Nota: Asegúrate de configurar las credenciales correctas de tu instancia local de PostgreSQL en dicho archivo.

Compilar y empaquetar la aplicación:

Bash
./mvnw clean package -DskipTests
Iniciar el Servidor de Spring Boot:

Bash
./mvnw spring-boot:run
La API levantará de manera nativa en el puerto estándar: http://localhost:8080

 Arquitectura del Backend (src/main/java/)
El código sigue las convenciones de diseño modular impulsadas por DDD (Domain-Driven Design) y desacoplamiento por capas:

com/nocountry/webapp/
├── TalentCircleApplication.java # Clase principal de arranque de Spring Boot
├── analytics/           # Modelos de procesamiento analítico y KPIs semanales
│   ├── WeeklyDigestData.java
│   └── WeeklyStatistics.java
├── config/              # Configuraciones del framework (Seguridad JWT, CORS, Swagger)
│   ├── ChannelDraftDataSeeder.java
│   ├── CommunityDataSeeder.java
│   ├── CommunityPostDataSeeder.java
│   ├── CorsConfig.java
│   ├── SecurityConfig.java
│   ├── SwaggerConfig.java
│   ├── TimeConfig.java
│   ├── UserDataSeeder.java
│   └── WeeklyDigestDataSeeder.java
├── controller/          # Capa de Entrada REST (Endpoints expuestos al cliente)
│   ├── AuthController.java          # Registro, Login y Refresh Tokens
│   ├── ChannelDraftController.java   # Gestión de borradores por redes sociales
│   ├── CommunityController.java      # CRUD de comunidades monitoreadas
│   ├── CommunityPostController.java  # Extracción de posteos analizados
│   └── WeeklyDigestController.java   # Orquestador del Job semanal
├── dto/                 # Data Transfer Objects (Contratos de Request y Response)
├── entity/              # Modelos de Persistencia (Mapeo de Tablas de Hibernate/JPA)
│   └── User.java, Community.java, CommunityPost.java, RefreshToken.java, WeeklyDigest.java
├── enums/               # Constantes de Estado del pipeline de negocio
│   └── ChannelDraftStatus.java, CommunityPostType.java, Role.java, TargetPlatform.java, WeeklyDigestStatus.java
├── exception/           # Gestión centralizada de errores HTTP semánticos
└── repository/          # Capa de Acceso a Datos (Interfaces Spring Data JPA)
## 🗺️ Matriz de Endpoints Principales (API Rest)

### Módulo de Autenticación (Auth)
```http
POST    /api/auth/register                   --> Registro de nuevos usuarios al sistema (Público)
POST    /api/auth/login                      --> Autenticación. Retorna Access/Refresh Tokens (Público)
POST    /api/auth/refresh                    --> Renueva Access Token 
usando el de refresco (Público)

Módulo de Comunidades (Communities)
GET     /api/communities                     --> Obtiene el listado de comunidades (Autenticado)
POST    /api/communities                     --> Registra una nueva comunidad para monitoreo (Rol EDITOR)

Módulo de Borradores (Channel Drafts)
GET     /api/channel-drafts                  --> Lista los borradores semanales para redes (Autenticado)
PUT     /api/channel-drafts/{id}/content     --> Actualiza el contenido de un borrador (Rol EDITOR)

📊 Automatización de Datos (Seeders de Base de Datos)
Para facilitar las pruebas de navegación de los evaluadores durante el Demo Day, la aplicación cuenta con Data Seeders automáticos independientes:

ChannelDraftDataSeeder

CommunityDataSeeder

CommunityPostDataSeeder

UserDataSeeder

WeeklyDigestDataSeeder

Al levantar el sistema, se inyectarán de manera automática en la base de datos relacional:

Usuarios de prueba con roles diferenciados (USER, EDITOR).

Comunidades activas precargadas en el sistema.

Historial de Community Posts y métricas simuladas de procesamiento.

Borradores de canales (Channel Drafts) listos para ser editados o aprobados desde la interfaz de usuario.

🔑 Credenciales de Acceso Rápido para Pruebas:
👤 Usuario Administrador: admin@talentcircle.com

🔒 Contraseña: Admin123*

🧪 Suite de Pruebas (Testing)
La aplicación cuenta con una suite completa de pruebas unitarias y de integración que garantizan la consistencia de las transiciones de estados del pipeline y la seguridad de los endpoints.

Para ejecutar la suite de test completa de Maven, corre:

Bash
./mvnw test
Tests de Integración: Verifican los controladores reales y respuestas HTTP de seguridad mediante escenarios simulados (CommunityControllerIntegrationTest.java).

Tests Unitarios: Validan las reglas de negocio aisladas de la lógica de servicios (CommunityServiceUnitTest.java).