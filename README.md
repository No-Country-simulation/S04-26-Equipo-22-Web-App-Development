# TalentCircle — Plataforma Integral (Equipo 22 - S04-26)

 **Objetivo:** Automatizar el monitoreo de comunidades tecnológicas, analizar datos semanales de manera automatizada y generar borradores de contenido (Newsletter, LinkedIn, Twitter) optimizados mediante Inteligencia Artificial.

Este repositorio unificado contiene tanto el ecosistema de la interfaz de usuario (Frontend) como el Core API de servicios (Backend). El proyecto se encuentra **completamente terminado, integrado y listo para producción**.

---

##  Integrantes del Equipo

* **Rider Manrique Cueto** — Backend Developer
* **Anthony Bañon** — Backend Developer
* **Alejandro Camacho** — Fullstack Developer
* **Martin Aguilera** — Frontend Developer
* **Abigail Pintos** — Frontend Developer
* **Luis Alberto vera** — Frontend Developer
* **Jorge Enrique Briches** — QA Tester / Analyst

---

##  Stack Tecnológico

* **Backend:** Java 17 / Spring Boot 3 / Arquitectura REST
* **Base de Datos:** PostgreSQL / Hibernate (JPA)
* **Inteligencia Artificial:** Integración con modelos LLM (OpenAI API / LangChain4j)
* **Frontend:** React / JavaScript (Vite) / Bootstrap & PostCSS
* **Contenedores:** Docker / Docker Desktop (Orquestación unificada)

---

##  Prerrequisitos del Sistema Global

Antes de iniciar la aplicación de manera local, asegúrate de contar con:
- **Docker Desktop** (Obligatorio para la orquestación unificada de servicios y base de datos).
- **Java SE Development Kit (JDK) 17** o superior (Opcional para ejecución nativa).
- **Node.js (v18+)** y **npm** (Opcional para ejecución nativa del Frontend).

---

##  Clonación y Configuración del Repositorio

Para descargar el proyecto y posicionarte en la rama estable de producción/entrega, ejecuta en tu terminal:

```bash
# 1. Clonar el repositorio unificado de la simulación
git clone [https://github.com/No-Country-simulation/S04-26-Equipo-22-Web-App-Development.git](https://github.com/No-Country-simulation/S04-26-Equipo-22-Web-App-Development.git)

# 2. Ingresar al directorio principal del proyecto
cd S04-26-Equipo-22-Web-App-Development

# 3. Cambiar a la rama de integración final
git checkout develop

Guía de Despliegue Rápido (Docker) 
Este es el método oficial y recomendado para la evaluación en el Demo Day. Permite levantar la base de datos PostgreSQL, la API de Java y la interfaz de React interconectadas en una red limpia con un solo comando.

Asegúrate de tener Docker Desktop abierto y corriendo.

Posiciónate en la raíz principal del proyecto (donde se encuentra el archivo docker-compose.yml).

Ejecuta el comando de inicialización limpia:

Bash
docker compose down
docker compose up --build -d
 Mapeo de Servicios Disponibles:
 Frontend UI Web (React + Vite): http://localhost:5175

 Backend REST API (Spring Boot): http://localhost:8080

 Documentación Interactiva Swagger UI: http://localhost:8080/swagger-ui/index.html
