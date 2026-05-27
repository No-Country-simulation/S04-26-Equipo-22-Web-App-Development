Markdown
# TalentCircle — Frontend Web Application

Esta sección del repositorio contiene la aplicación cliente de la plataforma TalentCircle. Una interfaz SPA (Single Page Application) de alto rendimiento, modular y con diseño totalmente responsivo orientada a tableros analíticos y administración de contenidos.

---

## 🛠️ Stack Tecnológico Frontend

* **Core:** React 18+ / JavaScript (ES6+)
* **Herramienta de Construcción:** Vite (Entorno de desarrollo ultra rápido)
* **Gestión de Estado:** Context API (Global State para sesión, autenticación y carrito de datos)
* **Estilos y Maquetación:** Bootstrap 5 / PostCSS

---

## 📌 Prerrequisitos de Entorno

Antes de inicializar los servicios del Frontend de manera aislada o nativa, asegúrate de contar con:
- **Node.js** (Versión 18 o superior recomendada).
- **npm** (Administrador de paquetes de Node, instalado automáticamente junto con Node.js).

---

## 🚀 Ejecución en Entorno de Desarrollo Local

Si deseas ejecutar la interfaz web de manera local y nativa (sin usar los contenedores unificados de Docker):

1. **Ingresar al directorio correspondiente:**
   ```bash
   cd Frontend
Instalar todas las dependencias del proyecto:

Bash
npm install
Iniciar el servidor local de desarrollo con Vite:

Bash
npm run dev
El servidor local se inicializará típicamente en el puerto configurado: http://localhost:5175

🏗️ Arquitectura de Carpetas y Componentes (src/)
La estructura del código está organizada de forma modular siguiendo las buenas prácticas de React:

src/
├── assets/          # Recursos estáticos (Imágenes, logotipos, vectores SVG)
├── components/      # Componentes de UI reutilizables (Navbar, Sidebar, Footer, Modales)
├── context/         # Configuración del Estado Global (AuthContext, DataContext)
│   ├── AuthContext.jsx # Manejo global de tokens de sesión JWT y estado de login
│   └── AppProvider.jsx # Proveedor que envuelve la aplicación evitando el "Prop Drilling"
├── hooks/           # Custom Hooks personalizados para modularizar lógica de negocio
├── layouts/          # Plantillas estructurales de vistas compartidas
├── pages/           # Páginas o pantallas principales de la SPA
│   ├── Dashboard/   # Tablero analítico principal (KPIs y métricas semanales)
│   ├── Communities/ # Panel de gestión y CRUD de comunidades monitoreadas
│   ├── Drafts/      # Editor dinámico de borradores generados por IA (Twitter, LinkedIn)
│   └── Login/       # Formulario de autenticación segura
├── services/        # Configuración de clientes HTTP (Instancia de Axios o Fetch a la API)
├── styles/          # Hojas de estilos generales (Configuración de PostCSS y Bootstrap)
├── App.jsx          # Enrutador principal y estructura central del Front
└── main.jsx         # Punto de entrada oficial de la aplicación a la jerarquía del DOM
📋 Características Implementadas Destacadas
Consumo de API Segura: Interconexión mediante Axios con el backend en Spring Boot, interceptando cabeceras para enviar los tokens JWT de manera transparente.

Flujo de Estado Limpio: Implementación de Context API para centralizar la información que múltiples componentes lejanos necesitan compartir (roles de usuario, sesión activa, borradores en edición), eliminando el prop drilling.

Control de Roles: Control de vistas en el cliente dependiendo del rol inyectado desde la base de datos (USER o EDITOR), bloqueando o permitiendo la edición de componentes.