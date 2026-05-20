# TalentCircle — Frontend

Panel editorial del pipeline semanal de contenido de TalentCircle.

## Stack

- **React 19** + **Vite 8**
- **React Router 7** (`react-router-dom`)
- **axios** para HTTP (con interceptores para JWT + refresh)
- **lucide-react** para iconografía UI (uniforme y con buen tree-shaking)
- Componente compartido `ChannelIcon` para los logos de canal (Newsletter, LinkedIn, Twitter/X) — SVGs inline propios para mantener consistencia visual
- **CSS vanilla** por componente (sin Tailwind, sin CSS-in-JS, sin preprocesadores)

## Quick start

```bash
npm install
cp .env.example .env
npm run dev
```

El dev server arranca en `http://localhost:5173` (o el siguiente puerto libre si está ocupado).

Scripts disponibles:

| Script           | Acción                              |
| ---------------- | ----------------------------------- |
| `npm run dev`    | Dev server con HMR                  |
| `npm run build`  | Build de producción a `dist/`       |
| `npm run preview`| Sirve el build localmente           |
| `npm run lint`   | ESLint sobre el repo                |

## Variables de entorno

Todas las variables expuestas al cliente deben empezar con `VITE_`.

| Variable                 | Default                  | Descripción |
| ------------------------ | ------------------------ | ----------- |
| `VITE_API_URL`           | `http://localhost:8080`  | Base URL del backend. Usado por `axios` en `src/api/client.js`. |
| `VITE_DRAFTS_USE_MOCK`   | `true`                   | Cuando es `"false"`, el frontend consume `/api/drafts/*` real. Cuando es `"true"` (default) usa los mocks en memoria de `src/data/draftsMock.js` con ~200ms de latencia simulada. |

> Nota: `VITE_DRAFTS_USE_MOCK` se evalúa una sola vez al cargar el módulo `api/drafts.js`. Cambiar el valor requiere reiniciar el dev server.

## Arquitectura

```
src/
├── api/               # Capa de servicios (toda llamada axios vive aquí)
│   ├── client.js      # Instancia axios + interceptores JWT/refresh
│   ├── auth.js        # login, register, logout, getCurrentUser
│   ├── communities.js # CRUD de comunidades
│   ├── drafts.js      # list/get/update/transition + switch mock↔real
│   └── draftExport.js # Export a Markdown/JSON (clipboard y descarga)
├── auth/
│   └── tokenStorage.js  # Wrapper localStorage para access/refresh tokens
├── context/
│   └── AuthContext.jsx  # Estado global de auth, carga /me al boot, useAuth()
├── data/
│   ├── draftsMock.js     # Seed de drafts (la "fuente" en modo mock)
│   └── draftSelectors.js # Funciones puras: flatten, conteos, vista por canal
├── routes/
│   ├── AppRouter.jsx     # Mapa de rutas (públicas vs. protegidas)
│   └── ProtectedRoute.jsx# Gate de auth con soporte de roles
├── components/
│   ├── layout/           # Chrome compartido (Header, AppLayout)
│   ├── drafts/           # Lista, filtros, card, sidebar de drafts
│   ├── approval/         # Botones y flujo de aprobación
│   └── channelPreviews/  # Previews visuales por canal (LinkedIn, Twitter, Newsletter)
└── views/             # Componentes de ruta (uno por path)
```

Reglas implícitas:

- Toda llamada HTTP vive en `src/api/`. Las views/componentes nunca importan `axios` directo.
- Los selectores de `src/data/draftSelectors.js` son funciones puras y no tocan red.
- Los estilos viven junto al componente (`Foo.jsx` + `Foo.css`).

## El contrato `Draft`

Cada draft representa **un tema semanal con tres versiones de canal**. Este es el modelo canónico que el backend debe servir tal cual:

```json
{
  "id": "draft-2026-W19",
  "weekOf": "2026-05-08",
  "topicTitle": "React performance e IA aplicada al contenido",
  "topicSummary": "Resumen del tema semanal…",
  "sourceContributions": [
    {
      "id": 101,
      "type": "DISCUSSION",
      "authorName": "María González",
      "excerpt": "…",
      "reactionsCount": 42,
      "commentsCount": 18,
      "sourceUrl": "https://example.com/discussion/101",
      "communityName": "Frontend Devs"
    }
  ],
  "channels": {
    "newsletter": { "channel": "newsletter", "title": "…", "body": "…", "status": "pending" },
    "linkedin":   { "channel": "linkedin",   "title": "…", "body": "…", "status": "pending" },
    "twitter":    { "channel": "twitter",                "body": "…", "status": "pending" }
  },
  "status": "GENERATED",
  "createdAt": "2026-05-08T20:00:00Z",
  "updatedAt": "2026-05-08T20:00:00Z",
  "approvedBy": { "id": 1, "email": "editor@talentcircle.dev" },
  "approvedAt": "2026-04-27T11:30:00Z",
  "publishedAt": "2026-04-27T12:00:00Z"
}
```

**Estados de `draft.status` (state machine):**

| Desde        | Transiciones permitidas        |
| ------------ | ------------------------------ |
| `GENERATED`  | `IN_REVIEW`, `REJECTED`        |
| `IN_REVIEW`  | `APPROVED`, `GENERATED`, `REJECTED` |
| `APPROVED`   | `PUBLISHED`, `IN_REVIEW`       |
| `PUBLISHED`  | (terminal)                     |
| `REJECTED`   | (terminal)                     |

**Estados de `channels[x].status`:** `pending` → `edited` → `approved` (a nivel canal, no afecta el `status` global del draft).

Los tipos de contribución soportados son: `DISCUSSION`, `RESOURCE`, `QUESTION`, `SESSION`.

## Mecánicas clave

### Autenticación

- JWT en `localStorage` bajo las claves `talent_access_token` y `talent_refresh_token` (ver `auth/tokenStorage.js`).
- El **request interceptor** de `api/client.js` agrega `Authorization: Bearer <accessToken>` a cada petición si hay token.
- En respuesta **401**, el cliente intenta `POST /api/auth/refresh` con el refresh token y reintenta la petición original con el nuevo access token. Las llamadas a `/login` y `/refresh` están excluidas para evitar bucles.
- Si el refresh falla: se limpian los tokens y se dispara un `CustomEvent("auth:logout")` en `window`, que `AuthContext` escucha para cerrar la sesión globalmente.
- `AuthContext` carga el usuario actual desde `GET /api/users/me` al boot y expone `useAuth()` con `{ user, status, isAuthenticated, isLoading, login, register, logout }`.

### Rutas protegidas

`<ProtectedRoute>` envuelve `<AppLayout />` y:

- Muestra un placeholder mientras `isLoading`.
- Redirige a `/login` si el usuario no está autenticado (preserva `from` en `location.state` para volver tras login).
- Acepta `roles={["EDITOR"]}` para gating por rol; si el usuario no tiene el rol, redirige a `/`.

### Drafts: mock ↔ real

`src/api/drafts.js` lee `VITE_DRAFTS_USE_MOCK` una vez al cargar el módulo y decide en cada función si:

- **mock**: muta una copia en memoria de `draftsMock.js`, simula 200ms de latencia, y valida las transiciones de estado localmente.
- **real**: hace `GET/PUT/POST` contra `/api/drafts/*` usando el cliente axios (auth automática vía interceptor).

Esto permite trabajar el frontend sin backend levantado y cambiar al endpoint real con una sola variable.

## Mapa de rutas

| Path                              | Componente         | Descripción                                           | Auth |
| --------------------------------- | ------------------ | ----------------------------------------------------- | ---- |
| `/login`                          | `LoginPage`        | Login con email + password                            | no   |
| `/register`                       | `RegisterPage`     | Registro de cuenta                                    | no   |
| `/`                               | `HomePage`         | Dashboard de entrada                                  | sí   |
| `/communities`                    | `CommunitiesPage`  | Listado y administración de comunidades               | sí   |
| `/drafts`                         | `Drafts`           | Lista de drafts semanales por canal                   | sí   |
| `/editor/:draftId/:channel`       | `DraftEditor`      | Edición del contenido de un canal específico          | sí   |
| `/preview`                        | `ChannelPreview`   | Previsualización visual por canal                     | sí   |
| `/approval` · `/approval/:id`     | `ApprovalPage`     | Flujo de aprobación/publicación de un draft           | sí   |

Las rutas autenticadas están envueltas en `<AppLayout>` (header + `<Outlet />`).

## Próximos pasos

- Apuntar a backend real: setear `VITE_DRAFTS_USE_MOCK=false` y verificar que `/api/drafts/*` cumpla el contrato de arriba.
- Implementar endpoint de publicación efectiva (hoy `publishDraft` solo transiciona el estado a `PUBLISHED`; falta el envío real al canal correspondiente).
- Activar gating por rol `EDITOR` en las rutas de aprobación (`<ProtectedRoute roles={["EDITOR"]}>`).
- Tests unitarios para `draftSelectors.js` y para las transiciones de `transitionDraft`.

## Referencia cruzada

- Contrato esperado del backend: [`../docs/backend-requirements.md`](../docs/backend-requirements.md).
