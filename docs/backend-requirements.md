# Backend Requirements — TalentCircle (pipeline semanal de contenido)

## 1. Resumen

TalentCircle ejecuta una pipeline semanal asistida por IA: cada **viernes EOD** un cron agrega la actividad de la comunidad (posts con más reacciones, Q&A más respondidas, recursos más compartidos), un LLM selecciona los temas más relevantes y genera **3 drafts diferenciados por canal** (newsletter largo, LinkedIn corto, Twitter muy corto) por cada tema semanal. El **lunes**, un usuario con rol `EDITOR` entra al panel, revisa, edita por canal, aprueba y dispara la publicación (API LinkedIn) o la exportación estructurada (newsletter / Twitter). Este documento enumera los endpoints REST, DTOs, schemas y comportamientos que el backend debe implementar para que el frontend pueda pasar de mock (`VITE_DRAFTS_USE_MOCK=true`) a backend real sin cambios de código.

---

## 2. Endpoints ya existentes que el frontend consume

Estos endpoints ya están implementados y **no requieren cambios** salvo lo indicado:

- `POST /api/auth/register` — registro de usuario.
- `POST /api/auth/login` — login, devuelve JWT.
- `POST /api/auth/refresh` — refresca el JWT.
- `POST /api/auth/logout` — invalida el JWT.
- `GET /api/users/me` → `{ id, email, role }`. **Cambio necesario**: el enum `Role` debe ampliarse con `EDITOR` (ver sección 4).
- `GET /api/communities` — lista.
- `POST /api/communities` — crea (ADMIN).
- `PUT /api/communities/{id}` — actualiza (ADMIN).
- `DELETE /api/communities/{id}` — elimina (ADMIN).
- `GET /api/communities/active` — sólo activas.
- `PATCH /api/communities/{id}/activate` / `PATCH /api/communities/{id}/deactivate` — toggle (ADMIN).

Entidad interna existente (sin controller todavía): `CommunityPost` con campos `content, authorName, type [QUESTION/RESOURCE/SESSION/DISCUSSION], reactionsCount, commentsCount, externalPostId, sourceUrl, collectedAt, community`. Se reutiliza como insumo de la pipeline.

---

## 3. Endpoints nuevos que se necesitan

Base path: `/api/drafts`. Todos requieren JWT (header `Authorization: Bearer <token>`). Acceso restringido a roles `EDITOR` y `ADMIN` salvo indicación contraria.

### 3.1 `GET /api/drafts`

Lista los drafts (filtrables). Llamado desde `Frontend/src/api/drafts.js` → `listDrafts()`.

- **Auth:** JWT, roles `EDITOR` o `ADMIN`.
- **Query params:**
  - `status` (opcional) — uno de `GENERATED | IN_REVIEW | APPROVED | PUBLISHED | REJECTED`.
  - `weekOf` (opcional) — `YYYY-MM-DD` (lunes de la semana ISO correspondiente).
- **Request body:** ninguno.
- **Response 200:** `Draft[]` (ver schema en sección 4).
- **Errores:**
  - `401` sin/expirado JWT.
  - `403` rol no autorizado.
  - `400` `status` o `weekOf` con formato inválido.

### 3.2 `GET /api/drafts/{id}`

Detalle de un draft. Llamado desde `getDraft(id)`.

- **Auth:** JWT, roles `EDITOR` o `ADMIN`.
- **Response 200:** `Draft` completo (con `channels` y `sourceContributions` expandidos).
- **Errores:** `401`, `403`, `404` si no existe.

### 3.3 `PUT /api/drafts/{draftId}/channels/{channel}`

Actualiza el contenido editado de un canal específico. Llamado desde `updateChannelDraft(draftId, channel, payload)`.

- **Auth:** JWT, roles `EDITOR` o `ADMIN`.
- **Path params:**
  - `draftId` — id del draft.
  - `channel` — uno de `newsletter | linkedin | twitter`.
- **Request body:**
  ```json
  {
    "title": "string | null",
    "body": "string"
  }
  ```
  > El frontend envía sólo los campos que cambian. `twitter` no usa `title`. El backend debe poner automáticamente `status = "edited"` y `editedAt = now()` en el canal afectado, y actualizar `draft.updatedAt`.
- **Response 200:** `Draft` completo actualizado.
- **Errores:**
  - `400` `channel` inválido o `body` excede límite (Twitter: 280 chars sugerido, validar en backend).
  - `401`, `403`, `404`.
  - `409` si el draft está en estado terminal (`PUBLISHED` o `REJECTED`).

### 3.4 `POST /api/drafts/{draftId}/transition`

Cambia el estado del draft respetando la state machine. Llamado desde `transitionDraft(draftId, nextStatus)`.

- **Auth:** JWT, roles `EDITOR` o `ADMIN`.
- **Request body:**
  ```json
  { "status": "IN_REVIEW | APPROVED | PUBLISHED | REJECTED | GENERATED" }
  ```
- **Response 200:** `Draft` completo con campos derivados actualizados:
  - Si pasa a `APPROVED` → setear `approvedAt = now()` y `approvedBy = currentUser`.
  - Si pasa a `PUBLISHED` → setear `publishedAt = now()`.
  - Siempre actualizar `updatedAt`.
- **Errores:**
  - `400` transición no permitida (ver tabla sección 5). Mensaje sugerido: `"Transición no permitida: <current> → <next>"`.
  - `401`, `403`, `404`.
  - `422` si `nextStatus = PUBLISHED` pero algún canal no está `approved` (regla de negocio recomendada).

### 3.5 `POST /api/drafts/{draftId}/publish/{channel}` (nuevo, server-side publish)

El frontend hoy exporta a JSON/MD del lado cliente (ver `Frontend/src/api/draftExport.js`). Este endpoint debe ofrecer la publicación real para canales con integración (LinkedIn) y la exportación canónica para los que aún no la tienen.

- **Auth:** JWT, roles `EDITOR` o `ADMIN`.
- **Path params:** `draftId`, `channel` ∈ `newsletter | linkedin | twitter`.
- **Request body:** vacío u opcional `{ "scheduleAt": "ISO-8601" }` (futuro).
- **Response 200:**
  ```json
  {
    "draftId": "string",
    "channel": "newsletter | linkedin | twitter",
    "published": true,
    "externalPostId": "string | null",
    "externalUrl": "string | null",
    "exportedPayload": { /* presente sólo si no hay integración */ },
    "publishedAt": "ISO-8601"
  }
  ```
  - `linkedin` → llama a LinkedIn API, persiste `externalPostId` y `externalUrl`.
  - `newsletter` / `twitter` → devuelve `exportedPayload` (mismo shape que produce `draftExport.js#toJson`) hasta que haya integración.
- **Side effect:** cuando los 3 canales están publicados, el backend transiciona el `Draft.status` a `PUBLISHED` automáticamente.
- **Errores:**
  - `409` el canal no está `approved`.
  - `502` falla la API externa (LinkedIn). Devolver `{ error, providerMessage }`.
  - `401`, `403`, `404`.

---

## 4. Nuevas entidades y enums

### 4.1 Entidad `Draft`

Shape exacto consumido por el frontend (ver `Frontend/src/data/draftsMock.js`):

```json
{
  "id": "string (ej: draft-2026-W19)",
  "weekOf": "YYYY-MM-DD",
  "topicTitle": "string",
  "topicSummary": "string",
  "sourceContributions": [ /* SourceContribution[] */ ],
  "channels": {
    "newsletter": { /* DraftChannel */ },
    "linkedin":   { /* DraftChannel */ },
    "twitter":    { /* DraftChannel */ }
  },
  "status": "GENERATED | IN_REVIEW | APPROVED | PUBLISHED | REJECTED",
  "createdAt": "ISO-8601",
  "updatedAt": "ISO-8601",
  "approvedAt": "ISO-8601 | null",
  "approvedBy": { "id": 1, "email": "editor@talentcircle.dev" } ,
  "publishedAt": "ISO-8601 | null"
}
```

Notas de persistencia:
- `id` puede ser UUID interno; el frontend lo trata como opaque string. El formato `draft-YYYY-Www` queda como sugerencia legible.
- `weekOf` siempre lunes ISO de la semana cubierta.
- Relación: un `Draft` pertenece a una `Community` (o a múltiples; ver pregunta abierta).

### 4.2 Sub-documento `DraftChannel` (embebido en `Draft.channels`)

```json
{
  "channel": "newsletter | linkedin | twitter",
  "title": "string | null (twitter no usa)",
  "body": "string",
  "status": "pending | edited | approved | rejected",
  "editedAt": "ISO-8601 | null",
  "externalPostId": "string | null",
  "externalUrl": "string | null"
}
```

`status` del canal es independiente del `Draft.status` global. Se actualiza por edición (`edited`), por aprobación explícita del canal o por publicación.

### 4.3 `SourceContribution` (proyección de `CommunityPost`)

El frontend renderiza este DTO en `ApprovalPage.jsx` (sección "Contribuciones que originaron este borrador"):

```json
{
  "id": 101,
  "type": "QUESTION | RESOURCE | SESSION | DISCUSSION",
  "authorName": "string",
  "excerpt": "string (primeros N chars de content)",
  "reactionsCount": 0,
  "commentsCount": 0,
  "sourceUrl": "string | null",
  "communityName": "string"
}
```

Relación: `Draft` ↔ `CommunityPost` es N:N. Persistir tabla `draft_source_contributions` con FK a ambas. La respuesta del endpoint devuelve la proyección, no la entidad completa.

### 4.4 Enums

**`DraftStatus`** (alineado con el frontend):
- `GENERATED` — recién creado por el cron, sin revisar.
- `IN_REVIEW` — un editor lo abrió y está trabajando.
- `APPROVED` — listo para publicar.
- `PUBLISHED` — publicado en todos los canales objetivo.
- `REJECTED` — descartado.

**`ChannelStatus`** (lowercase, así lo espera el frontend):
- `pending` — generado, sin tocar.
- `edited` — modificado por un editor.
- `approved` — aprobado por canal.
- `rejected` — descartado por canal.

**`Channel`**: `newsletter | linkedin | twitter`.

**`Role`** (ampliación): agregar `EDITOR` al enum existente. Final: `USER | EDITOR | ADMIN`. El frontend del panel de aprobación debe requerir `EDITOR` o superior.

---

## 5. State machine de `DraftStatus`

Espejo de `VALID_TRANSITIONS` en `Frontend/src/api/drafts.js`. El backend debe rechazar (`400`) cualquier transición no listada.

| Estado actual | Transiciones permitidas         |
|---------------|---------------------------------|
| `GENERATED`   | `IN_REVIEW`, `REJECTED`         |
| `IN_REVIEW`   | `APPROVED`, `GENERATED`, `REJECTED` |
| `APPROVED`    | `PUBLISHED`, `IN_REVIEW`        |
| `PUBLISHED`   | *(ninguna — terminal)*          |
| `REJECTED`    | *(ninguna — terminal)*          |

Reglas adicionales sugeridas:
- `IN_REVIEW → APPROVED` requiere que los 3 canales tengan `status ∈ {edited, approved}` (no `pending`).
- `APPROVED → PUBLISHED` debería pasar por `POST /api/drafts/{id}/publish/{channel}` por cada canal; el backend resuelve la transición global cuando todos los canales están publicados.

---

## 6. Pipeline / cron requirements

**Schedule:** cron semanal **viernes EOD** (timezone a confirmar — sugerido `America/Argentina/Buenos_Aires`, 21:00 local). Ver pregunta abierta.

**Pipeline (idempotente, reintentable):**

1. **Aggregation step** — para cada `Community` activa, sobre la ventana `[lunes 00:00, viernes 20:59]` de la semana ISO actual:
   - Top N posts por `reactionsCount` (sugerido N=5).
   - Top N posts tipo `QUESTION` por `commentsCount`.
   - Top N posts tipo `RESOURCE` por `reactionsCount + commentsCount`.
   - Top N posts tipo `SESSION` por `reactionsCount`.
   - Deduplicar por `id`. Output: `CommunityPost[]` rankeado.

2. **LLM step** — una llamada por tema seleccionado (o batch). Input:
   ```json
   {
     "weekOf": "YYYY-MM-DD",
     "contributions": [ /* SourceContribution[] del paso 1 */ ],
     "communityContext": { "name": "...", "tone": "..." }
   }
   ```
   Output esperado del LLM (function calling / JSON mode):
   ```json
   {
     "topicTitle": "string",
     "topicSummary": "string (~2-3 frases)",
     "selectedContributionIds": [101, 102, 103],
     "channels": {
       "newsletter": { "title": "...", "body": "markdown ~400-700 palabras" },
       "linkedin":   { "title": "...", "body": "texto ~150-300 palabras, con emojis y hashtags" },
       "twitter":    { "body": "≤280 chars, con hashtags" }
     }
   }
   ```

3. **Persistence step** — sólo si el LLM devuelve JSON válido y todos los canales pasan validación (longitud, no vacío):
   - Crear `Draft` con `status = GENERATED`.
   - Inicializar cada `DraftChannel` con `status = "pending"`.
   - Vincular `sourceContributions` (FK a `CommunityPost.id`).
   - Setear `createdAt = updatedAt = now()`.
   - Emitir evento (log estructurado / notificación al editor — pendiente definir).

**Errores y reintentos:** si la llamada LLM falla, reintentar hasta 3 veces con backoff. Si persiste, dejar log y notificar; no crear `Draft` parcial. Si el agregado de la semana ya tiene `Draft`, no duplicar (idempotencia por `(communityId, weekOf)`).

---

## 7. Publishing / exporting

**Estado actual del frontend:** `Frontend/src/api/draftExport.js` hace export **del lado cliente**:
- `copyMarkdown(draft, channel)` — copia al portapapeles.
- `downloadJson(draft, channel)` — descarga `.json` con el shape de `toJson()`.
- `downloadMarkdown(draft, channel)` — descarga `.md`.

Esto seguirá funcionando offline (no requiere backend). Sin embargo, el backend debe ofrecer un equivalente server-side: `POST /api/drafts/{id}/publish/{channel}` (ver 3.5).

**Comportamiento esperado por canal:**

| Canal       | Acción backend                                                                 |
|-------------|--------------------------------------------------------------------------------|
| `linkedin`  | POST a LinkedIn API (UGC posts) con OAuth del editor/cuenta corporativa. Persistir `externalPostId` y `externalUrl` en el `DraftChannel`. |
| `newsletter`| Sin integración aún → devolver `exportedPayload` JSON estructurado y marcar canal como `published` (el editor lo lleva manualmente a Mailchimp/Substack). |
| `twitter`   | Sin integración aún → mismo enfoque (devolver `exportedPayload`).              |

Cuando los 3 canales tienen `externalPostId` o `publishedAt`, transicionar `Draft.status → PUBLISHED`.

---

## 8. CORS & Security

- El backend ya configura `allowedOriginPatterns("*")` — OK para desarrollo. Antes de producción, restringir al dominio del frontend.
- JWT se envía vía header `Authorization: Bearer <token>` (ya lo hace `Frontend/src/api/client.js`). No requiere cookies ni CSRF.
- Endpoints de `/api/drafts/**` deben validar:
  - Token válido y no expirado → `401`.
  - Rol `EDITOR` o `ADMIN` → `403`.
- Loggear `userId` de quien hace cada `transition` y cada `publish` (auditoría).
- Rate limit sugerido en `POST /publish/{channel}`: 10 req/min por usuario.

---

## 9. Open questions

- **Timezone del cron viernes EOD** — ¿`America/Argentina/Buenos_Aires` 21:00? ¿UTC? Confirmar antes de programar el scheduler.
- **Proveedor LLM** — ¿OpenAI (GPT-4 / GPT-4o), Anthropic Claude, Gemini? Definir SDK, modelo, y donde se almacena la API key (Vercel env, AWS Secrets, etc.).
- **OAuth de LinkedIn** — ¿la app publica con una cuenta corporativa fija o con la cuenta del editor que aprueba? Definir flow OAuth y refresh tokens.
- **Multi-community** — ¿un `Draft` agrega contenido de **todas** las comunidades activas o se genera **uno por comunidad** por semana? El mock muestra contribuciones de varias communities en un mismo draft (modelo "global").
- **Retención de historial** — ¿se mantienen drafts `REJECTED` y `PUBLISHED` indefinidamente? ¿Política de archive a partir de X meses?
- **Notificaciones al editor** — cuando termina la pipeline del viernes, ¿enviar email/Slack al rol `EDITOR`? Definir canal.
- **Validación de longitud por canal** — confirmar límites: Twitter 280, LinkedIn 3000, newsletter sin límite duro. Validar en backend además del cliente.
- **Permisos finos** — ¿`USER` puede ver drafts publicados (lectura pública)? Hoy todo está restringido a `EDITOR+`.
- **Versionado de drafts** — ¿se conserva historial de ediciones por canal (cada `PUT` crea una versión) o sólo el último estado? Recomendado: tabla `draft_channel_revisions` para auditoría editorial.
