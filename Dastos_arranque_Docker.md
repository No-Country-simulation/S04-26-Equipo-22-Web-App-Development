# Datos de Arranque - Docker

## Comando para levantar todo

```bash
docker-compose up --build
```

---

## Servicios

| Servicio   | Contenedor        | Puerto Host | Puerto Interno |
|------------|-------------------|-------------|----------------|
| PostgreSQL | postgres_talent   | 5433        | 5432           |
| Backend    | talent_backend    | 8080        | 8080           |
| Frontend   | talent_frontend   | 5175        | 5173           |

---

## Base de Datos (PostgreSQL 15)

| Variable         | Valor por defecto |
|------------------|-------------------|
| POSTGRES_DB      | talent            |
| POSTGRES_USER    | postgres          |
| POSTGRES_PASSWORD| postgres          |

Conexion desde el host:

```
Host:     localhost
Puerto:   5433
Database: talent
Usuario:  postgres
Password: postgres
```

---

## Backend (Spring Boot / Java 21)

| Variable               | Valor por defecto                                    |
|------------------------|------------------------------------------------------|
| SPRING_PROFILES_ACTIVE | dev                                                  |
| DB_HOST                | db (nombre del servicio en Docker)                   |
| DB_PORT                | 5432                                                 |
| DB_NAME                | talent                                               |
| DB_USER                | postgres                                             |
| DB_PASSWORD            | postgres                                             |
| JWT_SECRET             | temporary_secret_key_at_least_32_characters_long     |
| PORT                   | 8080                                                 |

### JWT

| Parametro              | Valor        | Equivalente   |
|------------------------|--------------|---------------|
| Token de acceso        | 86400000 ms  | 24 horas      |
| Refresh token          | 604800000 ms | 7 dias        |

### Usuarios Seed (se crean automaticamente)

| Rol   | Email                      | Password   |
|-------|----------------------------|------------|
| ADMIN | admin@talentcircle.com     | Admin123*  |
| USER  | user@talentcircle.com      | User1234*  |

---

## Frontend (React 19 / Vite 8)

| Variable           | Valor por defecto          |
|--------------------|----------------------------|
| VITE_API_URL       | http://localhost:8080       |
| VITE_DRAFTS_USE_MOCK | true                     |

### URLs de acceso

- Frontend: http://localhost:5175
- Backend API: http://localhost:8080
- Swagger/OpenAPI: http://localhost:8080/swagger-ui.html

---

## Red Docker

- Nombre: `talent_network`
- Driver: bridge
- Los servicios se comunican entre si usando los nombres de servicio (`db`, `backend`, `frontend`)

---

## Volumen persistente

- `pgdata` -> /var/lib/postgresql/data (datos de PostgreSQL)

---

## Comandos Utiles

### Arranque

```bash
# Levantar todos los servicios (construye imagenes si es necesario)
docker-compose up --build

# Levantar en segundo plano (detached)
docker-compose up --build -d

# Levantar solo un servicio especifico
docker-compose up backend
docker-compose up frontend
docker-compose up db
```

### Detener

```bash
# Detener todos los servicios
docker-compose down

# Detener y eliminar volumenes (BORRA datos de la DB)
docker-compose down -v

# Detener un servicio especifico
docker-compose stop backend
```

### Reset completo

```bash
# Reset total: contenedores + imagenes + volumenes + cache
docker-compose down -v --rmi all

# Reconstruir todo desde cero
docker-compose up --build --force-recreate

# Limpiar cache de Docker (liberar espacio)
docker system prune -a
```

### Logs

```bash
# Ver logs de todos los servicios
docker-compose logs

# Logs en tiempo real
docker-compose logs -f

# Logs de un servicio especifico
docker-compose logs -f backend
docker-compose logs -f frontend
docker-compose logs -f db
```

### Estado y monitoreo

```bash
# Ver servicios corriendo
docker-compose ps

# Ver consumo de recursos (CPU, RAM)
docker stats

# Inspeccionar un contenedor
docker inspect talent_backend
```

### Acceso a contenedores

```bash
# Entrar al contenedor del backend
docker exec -it talent_backend sh

# Entrar al contenedor del frontend
docker exec -it talent_frontend sh

# Entrar a la base de datos con psql
docker exec -it postgres_talent psql -U postgres -d talent
```

### Base de datos

```bash
# Backup de la base de datos
docker exec postgres_talent pg_dump -U postgres talent > backup.sql

# Restaurar backup
docker exec -i postgres_talent psql -U postgres talent < backup.sql

# Reset solo la base de datos (borra y recrea)
docker-compose down -v
docker-compose up db -d
```

### Reconstruir un servicio individual

```bash
# Reconstruir solo el backend
docker-compose build --no-cache backend
docker-compose up -d backend

# Reconstruir solo el frontend
docker-compose build --no-cache frontend
docker-compose up -d frontend
```

---

## Personalizar variables

Crear un archivo `.env` en la raiz del proyecto:

```env
DB_NAME=talent
DB_USER=postgres
DB_PASSWORD=mi_password_seguro
JWT_SECRET=mi_secret_jwt_de_produccion_muy_largo
```
