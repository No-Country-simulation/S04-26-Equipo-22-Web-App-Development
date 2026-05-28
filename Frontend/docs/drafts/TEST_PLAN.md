# Plan de Tests - Flujo de Borradores

Tests ejecutables manualmente. Cada caso indica:
- **ID**: para tracking
- **Bloquea**: ID de bug que validaría
- **Pre-requisitos**: estado inicial
- **Pasos**: acciones del usuario
- **Resultado esperado**: comportamiento correcto
- **Resultado actual**: PASS / FAIL / SKIP

## Setup

### Modo Mock (default)
```
VITE_DRAFTS_USE_MOCK=true  # En Frontend/.env
```
Los borradores vienen de `draftsMock.js`. No requiere backend.

### Modo Real
```
VITE_DRAFTS_USE_MOCK=false
VITE_API_URL=http://localhost:8080
```
Requiere backend corriendo. Login válido necesario.

### Comandos
```bash
cd Frontend
npm run dev    # Inicia en http://localhost:5173
```

---

## Suite 1 — Lista de Borradores (`/drafts`)

### T1-01 — Carga inicial
- **Pre**: Mock activo. Sin sesión iniciada.
- **Pasos**:
  1. Navegar a `/drafts`
- **Esperado**: Redirect a `/login`
- **Modo Real**: Mismo comportamiento

### T1-02 — Lista muestra borradores
- **Pre**: Logueado, mock activo, mockStore tiene 2+ drafts.
- **Pasos**:
  1. `/drafts`
- **Esperado**:
  - Spinner "Cargando borradores…" durante ~200ms (delay mock)
  - Se renderiza la lista con filas: canal, título, estado, fecha, acciones
  - 1 fila por (draft × canal) - si draft tiene 3 canales, hay 3 filas

### T1-03 — Filtro por canal
- **Bloquea**: B-005
- **Pasos**:
  1. `/drafts`
  2. Click tab "Newsletter"
- **Esperado**: solo filas con canal newsletter. Contador en el tab coincide con filas.
- **Verificar también**: Tabs "X", "LinkedIn", "Todos"

### T1-04 — Lista vacía
- **Bloquea**: B-016
- **Pre**: mockStore vacío (`_resetMockStore()` + vaciar manualmente) o backend sin digests.
- **Esperado**:
  - Mensaje "Aún no hay borradores"
  - Sugerencia de qué hacer (actualmente solo dice "aparecen los viernes")
- **FAIL si**: No hay link o sugerencia accionable.

### T1-05 — Click "Editar" navega al editor
- **Pasos**:
  1. `/drafts`
  2. Click icono lápiz en una fila de newsletter
- **Esperado**: `/editor/{draftId}/newsletter` con contenido cargado

### T1-06 — Click "Vista previa"
- **Pasos**:
  1. `/drafts`
  2. Click icono ojo en una fila de Twitter
- **Esperado**: `/preview?draftId=X&channel=twitter`

### T1-07 — Refresh cache tras edición
- **Bloquea**: B-004
- **Pasos**:
  1. `/drafts` → ver draft con estado "Generado"
  2. Click "Editar" → "Guardar y enviar a revisión"
  3. Click "← Volver a borradores"
- **Esperado**: La lista muestra el estado actualizado ("En revisión")
- **FAIL actual**: muestra estado viejo (cache no se invalida)

---

## Suite 2 — Editor (`/editor/:id/:channel`)

### T2-01 — Carga de draft existente
- **Pasos**:
  1. `/editor/{validId}/newsletter`
- **Esperado**:
  - Spinner "Cargando…"
  - Editor contentEditable con HTML sanitizado por DOMPurify
  - Título precargado en el input
  - Badge de estado canal visible

### T2-02 — Draft inexistente
- **Pasos**:
  1. `/editor/nope-no-existe/newsletter`
- **Esperado (Mock)**: Mensaje "Borrador no encontrado"
- **Esperado (Real)**: Mensaje claro de error (no debe mostrar editor)
- **FAIL si**: editor se renderiza vacío

### T2-03 — Canal inexistente
- **Bloquea**: B-009
- **Pasos**:
  1. `/editor/{validId}/facebook`
- **Esperado**: Redirect a `/drafts` o mensaje claro "canal no soportado"
- **FAIL actual**: Se muestra error en banner pero el editor también se renderiza vacío

### T2-04 — Edición de contenido
- **Pasos**:
  1. `/editor/X/newsletter`
  2. Click dentro del editor, escribir "Texto nuevo"
  3. Click "Guardar cambios"
- **Esperado**:
  - Botón muestra "Guardando…" durante save
  - Aparece "✓ Cambios guardados" por 2.5s
  - El contenido persiste tras recargar la página (mock: no persiste hasta reload, ver T2-05)

### T2-05 — Persistencia mock
- **Bloquea**: B-017
- **Pasos**:
  1. Editar y guardar
  2. F5 (recargar pestaña)
- **Esperado (Mock)**: cambios deberían persistir (actualmente no — usar localStorage)
- **Esperado (Real)**: cambios persisten siempre

### T2-06 — Título no se guarda
- **Bloquea**: B-001 (CRÍTICO)
- **Pasos**:
  1. `/editor/X/newsletter`
  2. Cambiar título a "Nuevo título test"
  3. Click "Guardar cambios"
  4. F5
- **Esperado**: Título persiste
- **FAIL actual**: Título vuelve al original

### T2-07 — Contador de Twitter
- **Pasos**:
  1. `/editor/X/twitter`
  2. Pegar 250 caracteres
- **Esperado**: contador muestra "250 / 280 caracteres"
- **Pasos extra**:
  3. Agregar 50 más (total 300)
- **Esperado**: contador en rojo "300 / 280", texto "supera el límite de X"

### T2-08 — Save de Twitter over-limit
- **Bloquea**: B-011
- **Pasos**:
  1. Editor Twitter con 300 chars
  2. Click "Guardar cambios"
- **Esperado**: Botón deshabilitado o warning bloqueante
- **FAIL actual**: Save procede, backend recibe contenido inválido

### T2-09 — Toolbar Bold/Italic/Underline
- **Pasos**:
  1. Editor newsletter
  2. Seleccionar palabra
  3. Click "B"
- **Esperado**: palabra en negrita, botón B con clase `active`
- **Repetir** para italic (I), underline (U)

### T2-10 — Toolbar listas
- **Pasos**:
  1. Posicionarse en línea vacía
  2. Click "1." → escribir "uno" → Enter → "dos"
- **Esperado**: lista numerada
- **Repetir** con viñetas

### T2-11 — Insertar enlace (XSS)
- **Bloquea**: B-007 (SECURITY)
- **Pasos**:
  1. Seleccionar texto
  2. Click 🔗
  3. Pegar `javascript:alert(1)` en el prompt
  4. OK
  5. Click en el link creado
- **Esperado**: Mensaje de error "URL inválida" antes de crear el link
- **FAIL actual**: link malicioso se crea, JavaScript se ejecuta al click

### T2-12 — Insertar enlace válido
- **Pasos**:
  1. Click 🔗 → ingresar `https://example.com`
- **Esperado**: Link normal creado

### T2-13 — Sin advertencia al salir
- **Bloquea**: B-006
- **Pasos**:
  1. Editar 100 caracteres
  2. Click "← Volver a borradores" sin guardar
- **Esperado**: Diálogo de confirmación "¿Descartar cambios?"
- **FAIL actual**: navega sin confirmar, contenido perdido

### T2-14 — Save fallido
- **Bloquea**: B-014
- **Pre**: DevTools → Network → throttle offline
- **Pasos**:
  1. Editar contenido
  2. Click "Guardar cambios"
- **Esperado**:
  - Error visible "No se pudo guardar"
  - Botón deshabilitado o con timeout
  - No se puede triggear duplicate save mientras hay error
- **FAIL actual**: Botón se rehabilita inmediatamente, doble-click envía 2 requests

### T2-15 — "Guardar y enviar a revisión"
- **Pasos**:
  1. Editor con draft en estado GENERATED
  2. Click "Guardar y enviar a revisión"
- **Esperado**:
  - Save sucede
  - Transición a IN_REVIEW automática
  - Badge cambia a "En revisión"

### T2-16 — Botón en draft publicado
- **Bloquea**: B-012
- **Pre**: Draft en estado PUBLISHED.
- **Pasos**:
  1. Abrir editor
- **Esperado**:
  - contentEditable=false (no se puede escribir)
  - Toolbar deshabilitado
  - Botones Guardar deshabilitados
- **FAIL actual**: Toolbar clickeable pero no funcional

---

## Suite 3 — Aprobación (`/approval`)

### T3-01 — Lista de aprobación
- **Pasos**:
  1. `/approval`
- **Esperado**: Lista de drafts con estado, título, fecha. 1 fila por draft (no por canal).

### T3-02 — Click navega a detalle
- **Pasos**:
  1. Click en un draft de la lista
- **Esperado**: `/approval/{id}` con detalle completo

### T3-03 — Detalle muestra todas las secciones
- **Pasos**:
  1. `/approval/{id}`
- **Esperado** (verificar cada sección):
  - Header con título y resumen del tema
  - AIReasoningCard ("La IA seleccionó…")
  - "Contribuciones que originaron este borrador"
  - "Estado del flujo" (ApprovalFlow component)
  - "Ciclo de publicación" (timeline)
  - "Versiones por canal" (3 cards)
  - "Acciones" (botones según estado)

### T3-04 — AIReasoningCard vacío
- **Bloquea**: B-002, B-020
- **Pre**: Draft sin `sourceContributions` (modo real siempre, o mock con array vacío).
- **Esperado**: Mensaje "Sin información de razonamiento disponible"
- **FAIL actual (real)**: Sección desaparece sin explicación; usuario no sabe si hay error

### T3-05 — Transición GENERATED → IN_REVIEW
- **Pre**: Draft en GENERATED.
- **Pasos**:
  1. Detalle → click "Enviar a revisión"
- **Esperado**:
  - Botón muestra loading
  - Estado se actualiza a "En revisión"
  - Timeline avanza al step 2
  - Botones disponibles cambian (ahora "Aprobar", "Rechazar", "Volver a generado")

### T3-06 — Transición IN_REVIEW → APPROVED
- **Pre**: Draft en IN_REVIEW.
- **Pasos**:
  1. Click "Aprobar"
- **Esperado**: estado APPROVED, timeline step 3, botón "Publicar" visible

### T3-07 — Transición APPROVED → PUBLISHED
- **Pre**: Draft en APPROVED.
- **Pasos**:
  1. Click "Publicar"
- **Esperado**: estado PUBLISHED, timeline step 4

### T3-08 — REJECTED en timeline
- **Bloquea**: B-010
- **Pre**: Draft en estado REJECTED.
- **Esperado**:
  - Step "Rechazado" o equivalente visible en rojo
  - NO debe mostrar "En revisión" como activo
- **FAIL actual**: muestra IN_REVIEW como activo (mismo índice)

### T3-09 — Transición parcial fallida
- **Bloquea**: B-003
- **Pre**: Draft con 3 canales, breakpoint en devtools para fallar segundo PATCH.
- **Pasos**:
  1. Click "Aprobar"
- **Esperado**: O todos los canales se aprueban, o ninguno (rollback)
- **FAIL actual**: 1 canal APPROVED, otros sin cambiar; aggregate status inconsistente

### T3-10 — Botón Editar del canal
- **Pasos**:
  1. Detalle → en card de Newsletter → click "Editar"
- **Esperado**: navega a `/editor/X/newsletter`

### T3-11 — Botón Preview del canal
- **Pasos**:
  1. Card LinkedIn → click "Preview"
- **Esperado**: navega a `/preview?draftId=X&channel=linkedin`

### T3-12 — Copiar Markdown
- **Bloquea**: B-008
- **Pasos**:
  1. Card de un canal → click "Copiar MD"
  2. Verificar clipboard (Ctrl+V en otra app)
- **Esperado**:
  - Botón muestra "✓ Copiado" por 1.8s
  - Contenido en clipboard es markdown válido
- **Test adicional**: Repetir en http:// (no https) — debería mostrar error claro
- **FAIL actual en no-https**: muestra "✓ Copiado" sin haber copiado

### T3-13 — Descargar JSON
- **Pasos**:
  1. Card → click "JSON"
- **Esperado**: Descarga archivo `{draftId}-{channel}.json` con metadata + body

### T3-14 — Descargar MD
- **Pasos**:
  1. Card → click "MD"
- **Esperado**: Descarga `{draftId}-{channel}.md`

### T3-15 — Lista no refresca tras transición
- **Bloquea**: B-004
- **Pasos**:
  1. `/approval` → click draft con estado IN_REVIEW
  2. Aprobar
  3. Click "← Volver al listado"
- **Esperado**: Lista muestra estado APPROVED
- **FAIL actual**: muestra IN_REVIEW (cache no invalidada)

---

## Suite 4 — Preview (`/preview`)

### T4-01 — Sin parámetros
- **Pasos**:
  1. `/preview`
- **Esperado**: Carga primer draft (drafts[0]), muestra los 3 canales

### T4-02 — Con draftId válido
- **Pasos**:
  1. `/preview?draftId={validId}`
- **Esperado**: Muestra ese draft, 3 canales

### T4-03 — draftId inválido
- **Bloquea**: B-013
- **Pasos**:
  1. `/preview?draftId=nope`
- **Esperado**:
  - Cargar drafts[0]
  - URL actualizada a `/preview?draftId={realId}`
- **FAIL actual**: URL sigue diciendo `?draftId=nope`

### T4-04 — Filtro por canal
- **Pasos**:
  1. `/preview?draftId=X`
  2. Click tab "Newsletter"
- **Esperado**: URL `?draftId=X&channel=newsletter`, solo 1 card visible

### T4-05 — Cambio de draft via dropdown preserva canal
- **Bloquea**: B-018
- **Pre**: 2+ drafts en lista.
- **Pasos**:
  1. `/preview?draftId=A&channel=twitter`
  2. Dropdown → seleccionar draft B
- **Esperado**: URL `?draftId=B&channel=twitter` (canal preservado)
- **FAIL actual**: URL `?draftId=B` (canal perdido, tabs vuelven a "Todos")

### T4-06 — Componentes de preview
- **Pasos**:
  1. Verificar que cada preview muestre:
     - Twitter: avatar, username, body, contador, "verified"
     - LinkedIn: avatar, nombre, rol, body, "..."
     - Newsletter: subject, body con HTML formateado
- **Nota**: Avatares/contadores son fake (B-021). Verificar que se vean razonables.

---

## Suite 5 — Race conditions & edge cases

### T5-01 — Doble-click en save
- **Bloquea**: B-014
- **Pasos**:
  1. Editor, throttle network a "Slow 3G"
  2. Editar contenido
  3. Click "Guardar cambios" 2 veces rápido (<500ms)
- **Esperado**: Solo 1 PATCH visible en Network
- **FAIL actual**: 2 PATCH requests, último gana

### T5-02 — Doble-click en transición
- **Bloquea**: B-003
- **Pasos**:
  1. `/approval/{id}`, network slow
  2. Click "Aprobar" 2 veces rápido
- **Esperado**: Solo 1 set de transiciones (gracias a `useAsyncAction.actingRef`)
- **PASS esperado** (ya fixed previamente)

### T5-03 — Navegación durante save
- **Pasos**:
  1. Editor, throttle slow
  2. Click Guardar
  3. Inmediatamente click "← Volver"
- **Esperado**: PATCH completa en background, no hay warnings en console
- **Verificar**: No hay "setState on unmounted component" error

### T5-04 — Dos pestañas con el mismo draft
- **Pasos**:
  1. Pestaña A: `/editor/X/newsletter`, editar "Versión A"
  2. Pestaña B: `/editor/X/newsletter`, editar "Versión B"
  3. Pestaña A: Guardar
  4. Pestaña B: Guardar
- **Esperado actual**: Último gana sin warning (Pestaña B sobreescribe A)
- **Esperado ideal**: Detección de conflicto via versioning

### T5-05 — Token expira durante edición
- **Pre**: Setear access token con TTL corto en backend.
- **Pasos**:
  1. Esperar a que expire
  2. Click Guardar
- **Esperado**: refresh automático via interceptor, save reintenta, success
- **Verificar**: `client.js` interceptor logic

---

## Suite 6 — Accesibilidad y UX

### T6-01 — Navegación por teclado
- **Pasos**:
  1. Tab a través de la página `/drafts`
- **Esperado**: focus visible en cada elemento interactivo, orden lógico

### T6-02 — Screen reader
- **Pasos**:
  1. NVDA/VoiceOver activado
  2. Navegar lista
- **Esperado**: roles ARIA correctos, labels en botones de icono

### T6-03 — Estados de loading
- **Verificar**:
  - Spinner en `/drafts` durante carga
  - Spinner en `/editor` durante getDraft
  - "Guardando…" en save
  - "Cargando borradores…" en lista de approval

### T6-04 — Mensajes de error
- **Verificar**:
  - Errores claros (no stack traces)
  - Pintados con color distintivo
  - Acción de retry disponible cuando aplica

### T6-05 — Mobile responsive
- **Pasos**:
  1. DevTools → 375px width (iPhone SE)
  2. Navegar todas las páginas
- **Esperado**:
  - Header no overflow
  - Lista de drafts legible (no horizontal scroll)
  - Editor usable
  - Modales fit en pantalla

---

## Matriz de cobertura

| Suite | Tests | Bloquea bugs |
|-------|-------|--------------|
| 1 Lista | 7 | B-004, B-005, B-016 |
| 2 Editor | 16 | B-001, B-006, B-007, B-009, B-011, B-012, B-014, B-017 |
| 3 Aprobación | 15 | B-002, B-003, B-004, B-008, B-010, B-020 |
| 4 Preview | 6 | B-013, B-018, B-021 |
| 5 Race | 5 | B-003, B-014 |
| 6 A11y | 5 | UX general |
| **Total** | **54** | **20 bugs** |

## Cómo ejecutar

### Manual (recomendado para esta versión)
1. Imprimir o copiar este checklist
2. Levantar el dev server: `npm run dev`
3. Ejecutar cada test, marcar PASS/FAIL en una copia
4. Reportar fallos con: ID del test + screenshot + browser/OS

### Automatizado (siguiente paso)
Ver [TEST_FIXTURES.md](TEST_FIXTURES.md) para datos de prueba y stubs de tests automatizados.
