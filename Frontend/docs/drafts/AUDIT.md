# Auditoría del Flujo de Borradores

Fecha: 2026-05-28
Alcance: `Frontend/src/views/Drafts.jsx`, `DraftEditor.jsx`, `ApprovalPage.jsx`, `ChannelPreview.jsx`, `Frontend/src/api/drafts.js`, `draftExport.js`, `Frontend/src/data/draftSelectors.js`, componentes en `drafts/` y `approval/`.

## Resumen ejecutivo

| Severidad | Cantidad | Acción |
|-----------|----------|--------|
| P0 (Bloqueante) | 5 | Bloquean release |
| P1 (Alto) | 9 | UX/datos rotos, fix antes de demo |
| P2 (Medio) | 6 | Fix en siguiente sprint |
| P3 (Bajo) | 4 | Backlog |

---

## P0 — Bloqueantes

### B-001 — `updateChannelDraft` nunca envía el título al backend
**Archivo:** `Frontend/src/api/drafts.js` (función `updateChannelDraft`)
**Síntoma:** Usuario edita el título de un borrador de Newsletter o LinkedIn, hace click en "Guardar cambios", ve "✓ Cambios guardados". Al recargar, el título original sigue ahí.
**Causa:** La función solo envía `{ content: body }` en el PATCH. El campo `title` del payload se ignora.
**Pasos para reproducir:**
1. Abrir `/editor/{draftId}/newsletter`
2. Cambiar el título a "Nuevo título de prueba"
3. Click "Guardar cambios"
4. Refrescar la página → título vuelve al original
**Fix sugerido:** Incluir `title` en el body del PATCH; el backend debe aceptar el campo (verificar `ChannelDraftUpdateContentRequestDTO.java`).

### B-002 — `sourceContributions` siempre vacío en modo real
**Archivo:** `Frontend/src/api/drafts.js` (función `toFrontendDraft` / `assembleDraft`)
**Síntoma:** La tarjeta "La IA seleccionó este tema porque…" en `/approval/{id}` nunca muestra contribuciones reales. Siempre dice "Sin contribuciones registradas".
**Causa:** El adaptador hardcodea `sourceContributions: []`. El backend no expone este campo en `ChannelDraftResponseDTO`.
**Impacto:** Funcionalidad de "razonamiento de IA" rota end-to-end.
**Fix sugerido:** Opción A — Backend agrega contribuciones al DTO. Opción B — Frontend hace fetch a `/api/community-posts/weekly/digest` y enlaza por `weeklyDigestId`.

### B-003 — `transitionDraft` puede dejar canales en estado inconsistente
**Archivo:** `Frontend/src/api/drafts.js` (función `transitionDraft`)
**Síntoma:** Al aprobar un draft con 3 canales, si el segundo PATCH falla, el primero queda APPROVED y el resto queda en estado anterior. Sin rollback.
**Causa:** Loop secuencial sin transacción. Backend tiene endpoint `approve-all/{weeklyDigestId}` que sí es transaccional, pero el frontend no lo usa.
**Pasos para reproducir:**
1. Simular fallo de red en el segundo PATCH (devtools throttle / breakpoint)
2. Click "Aprobar" en un draft con 3 canales
3. Verificar que algunos canales están APPROVED y otros no
**Fix sugerido:** Usar el endpoint bulk `approve-all` para APPROVED. Para REJECTED/IN_REVIEW, agregar lógica de rollback o reportar error con detalle por canal.

### B-004 — Cache de borradores no se invalida tras editar/transicionar
**Archivos:** `Frontend/src/hooks/useFetch.js`, `Drafts.jsx`, `ApprovalPage.jsx`
**Síntoma:** Usuario edita en `/editor/X/newsletter`, vuelve a `/drafts` (link "← Volver"), pero la lista muestra estado/contenido viejo.
**Causa:** `useFetch` cachea por componente. No hay invalidación cross-componente. Cada lista mantiene su propio snapshot.
**Pasos para reproducir:**
1. `/drafts` → ver draft #1 con estado "Generado"
2. Click "Editar" → editar → "Guardar y enviar a revisión"
3. Click "← Volver a borradores"
4. La lista todavía muestra "Generado"
**Fix sugerido:** Agregar `refetch()` en `onMount`/visibility-change, o implementar un store compartido (Zustand/Context) con invalidación explícita.

### B-005 — Inconsistencia de casing en status (UPPER vs lower)
**Archivos:** `drafts.js` (mapping), `draftSelectors.js`, `draftsMock.js`, `DraftCard.jsx`
**Síntoma:** Mock usa `"pending"/"edited"/"approved"` minúsculas. Real API devuelve `"GENERATED"/"IN_REVIEW"/"APPROVED"` mayúsculas. Filtros y comparaciones fallan silenciosamente al cambiar de mock a real.
**Causa:** Mapping incompleto en `mapChannelDraft`. `STATUS_LABEL` en `DraftCard.jsx` solo tiene claves en minúscula.
**Pasos para reproducir:**
1. Setear `VITE_DRAFTS_USE_MOCK=false`
2. Cargar `/drafts`
3. Verificar que los chips de status no muestran labels traducidos (`Pendiente`, `En revisión`...)
**Fix sugerido:** Normalizar a un único formato (UPPERCASE) en la capa de API. Actualizar mock para coincidir. Updatear `STATUS_LABEL` para aceptar ambos casing como fallback.

---

## P1 — Alta

### B-006 — Sin advertencia de cambios sin guardar al navegar
**Archivo:** `DraftEditor.jsx`
**Síntoma:** Usuario edita 500 caracteres, hace click en "← Volver a borradores", pierde todo sin confirmación.
**Fix:** `useEffect` con `beforeunload` listener + `useBlocker` de React Router v7.

### B-007 — `prompt()` para URL de link acepta `javascript:` (XSS)
**Archivo:** `DraftEditor.jsx` línea ~104 (función `execFormat` cuando `command === "createLink"`)
**Síntoma:** Si un editor malicioso ingresa `javascript:alert(document.cookie)` en el prompt, se crea un link con ese href.
**Fix:** Validar protocolo antes de pasar a `execCommand`: solo `http://`, `https://`, `mailto:`.

### B-008 — Fallos de clipboard silenciosos
**Archivos:** `ApprovalPage.jsx`, `ExportModal.jsx`, `draftExport.js`
**Síntoma:** En contexto sin clipboard API (no-HTTPS, permiso denegado), el botón "Copiar MD" muestra "✓ Copiado" sin haber copiado nada.
**Fix:** Detectar `!navigator.clipboard` antes de llamar, mostrar error claro, fallback a textarea + execCommand("copy").

### B-009 — Canal inexistente muestra error + editor roto a la vez
**Archivo:** `DraftEditor.jsx` líneas ~62-68
**Síntoma:** Usuario va a `/editor/{validId}/facebook`. Se setea `error` pero también se renderiza el editor vacío.
**Fix:** Validar `channel ∈ CHANNELS` antes de renderizar el editor (redireccionar o mostrar pantalla 404).

### B-010 — Estado `REJECTED` se posiciona como `IN_REVIEW` en el timeline
**Archivo:** `ApprovalPage.jsx` constante `STEP_ORDER`
**Síntoma:** `STEP_ORDER.REJECTED === 1` (mismo índice que IN_REVIEW). El timeline visualmente muestra "En revisión" como activo cuando el estado real es Rechazado.
**Fix:** Agregar un step "Rechazado" al timeline o renderizar un branch separado para `isRejected`.

### B-011 — Límite de 280 caracteres en Twitter solo advierte
**Archivo:** `DraftEditor.jsx` línea ~228
**Síntoma:** Usuario pega 350 caracteres para Twitter, ve aviso rojo, pero el botón "Guardar cambios" sigue habilitado. Save procede.
**Fix:** Disabled del botón cuando `overLimit && isTwitter`.

### B-012 — Toolbar funcional en drafts publicados
**Archivo:** `DraftEditor.jsx`
**Síntoma:** Cuando `contentEditable={false}` (draft PUBLISHED), los botones del toolbar siguen clickeables pero `execCommand` falla silenciosamente.
**Fix:** Disabled de cada botón del toolbar cuando `!isEditable`.

### B-013 — Fallback de draft en ChannelPreview no actualiza URL
**Archivo:** `ChannelPreview.jsx` líneas ~33-37
**Síntoma:** `/preview?draftId=invalid` carga `drafts[0]` silenciosamente, pero URL sigue diciendo `invalid`. Compartir el URL lleva a otro draft.
**Fix:** Si hay fallback, hacer `setParams` con el `id` real seleccionado.

### B-014 — Save fallido deja editor sin lock
**Archivo:** `DraftEditor.jsx` función `handleSave`
**Síntoma:** Si `updateChannelDraft` falla, `saving = false` en finally. Usuario puede hacer click otra vez inmediatamente y disparar duplicados.
**Fix:** Usar `useAsyncAction` (ya disponible) en lugar de `setSaving` inline. Su guard `actingRef` previene doble-click.

---

## P2 — Media

### B-015 — Sin paginación, límite hardcodeado de 20 digests
**Archivo:** `drafts.js` función `fetchDigests` (`size: 50` en `listDrafts` actual)
**Síntoma:** Si la organización genera >50 digests, los más viejos no aparecen.
**Fix:** Paginación UI o configuración por env var.

### B-016 — Lista vacía sin acción clara
**Archivo:** `DraftList.jsx`
**Síntoma:** Empty state dice "aparecen cada viernes EOD" pero no indica qué hacer mientras tanto.
**Fix:** Link a `/job-status` o `/communities`.

### B-017 — Mock store no persiste en reload
**Archivo:** `draftsMock.js`
**Síntoma:** En modo mock, edits se pierden al recargar la página. Confunde durante testing.
**Fix:** Persistir mockStore en localStorage, o avisar visualmente "modo mock — los cambios no persisten".

### B-018 — URL params de canal se pierden al cambiar draft
**Archivo:** `ChannelPreview.jsx` `<select onChange>` línea ~70
**Síntoma:** `/preview?draftId=X&channel=twitter`, usuario cambia draft via dropdown, URL queda `/preview?draftId=Y` (channel perdido).
**Fix:** Preservar `channel` al hacer `setParams`.

### B-019 — Error message no auto-desaparece
**Archivo:** `DraftEditor.jsx`
**Síntoma:** Error de save persiste hasta el próximo save exitoso.
**Fix:** `setTimeout` para limpiar error tras 5s, o limpiar al primer cambio en el editor.

### B-020 — `AIReasoningCard` vacío sin mensaje
**Archivo:** `ApprovalPage.jsx`
**Síntoma:** Si `sourceContributions` está vacío (B-002), el componente retorna `null` y el usuario ve un hueco vacío sin contexto.
**Fix:** Mostrar mensaje "Sin información de razonamiento disponible" en lugar de `null`.

---

## P3 — Baja

### B-021 — Avatares/contadores hardcodeados en previews
**Archivo:** `draftSelectors.js` función `getChannelView`
**Síntoma:** Twitter preview muestra avatar de pravatar.cc y "verified" hardcoded. Engañoso.
**Fix:** Agregar banner "Vista previa - elementos de marca no representan publicación real".

### B-022 — `activeFormats` no refleja contenido cargado
**Archivo:** `DraftEditor.jsx` `updateActiveFormats`
**Síntoma:** Si el draft tiene `<strong>texto</strong>`, al hacer click dentro el botón "B" no se highlightea hasta hacer otra acción.
**Fix:** Limitación de `document.queryCommandState`. Considerar migrar a Tiptap/Slate.

### B-023 — Tooltip de título sin truncar
**Archivo:** `DraftCard.jsx` `title={row.title}`
**Síntoma:** Títulos muy largos pueden hacer overflow.
**Fix:** Truncar a 100 chars.

### B-024 — DOMPurify puede strippar HTML válido
**Archivo:** `DraftEditor.jsx` `DOMPurify.sanitize(ch.body)`
**Síntoma:** Si el LLM genera tags no whitelisteados, se pierden al cargar.
**Fix:** Configurar lista de tags permitidos explícita.

---

## Resumen por archivo

| Archivo | Bloqueantes | Total |
|---------|-------------|-------|
| `api/drafts.js` | B-001, B-002, B-003, B-005 | 6 |
| `views/DraftEditor.jsx` | B-006, B-007, B-009, B-011, B-012, B-014 | 9 |
| `views/ApprovalPage.jsx` | B-008, B-010, B-020 | 5 |
| `views/ChannelPreview.jsx` | B-013, B-018 | 2 |
| `hooks/useFetch.js` | B-004 (parcial) | 1 |
| `data/draftSelectors.js` | B-005 (parcial), B-021 | 2 |
| `data/draftsMock.js` | B-005 (parcial), B-017 | 2 |
| `views/Drafts.jsx` + `DraftList.jsx` | B-016 | 2 |

Ver [TEST_PLAN.md](TEST_PLAN.md) para casos de prueba reproducibles.
