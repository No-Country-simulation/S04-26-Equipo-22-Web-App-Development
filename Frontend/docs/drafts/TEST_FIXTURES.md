# Test Fixtures y Stubs Automatizados

Datos de prueba y código stub para automatizar la suite de tests de borradores.

## 1. Fixtures de datos

### Draft completo (caso feliz)
```json
{
  "id": "draft-2026-W22",
  "weekOf": "2026-05-28",
  "topicTitle": "RAG patterns para sistemas multimodales",
  "topicSummary": "Cómo combinar embeddings de texto e imágenes en una arquitectura RAG.",
  "status": "GENERATED",
  "createdAt": "2026-05-23T18:00:00Z",
  "updatedAt": "2026-05-23T18:00:00Z",
  "approvedAt": null,
  "publishedAt": null,
  "sourceContributions": [
    {
      "id": "c1",
      "type": "QUESTION",
      "communityName": "AI Builders",
      "authorName": "María López",
      "excerpt": "¿Alguien tiene experiencia con CLIP + Pinecone para retrieval multimodal?",
      "reactionsCount": 42,
      "commentsCount": 18,
      "sourceUrl": "https://example.com/post/1"
    }
  ],
  "channels": {
    "newsletter": {
      "id": 101,
      "title": "RAG multimodal: 3 patrones que funcionan",
      "body": "<p>El RAG multimodal combina...</p>",
      "status": "pending"
    },
    "linkedin": {
      "id": 102,
      "title": "Patrones de RAG multimodal",
      "body": "<p>3 patrones para combinar...</p>",
      "status": "pending"
    },
    "twitter": {
      "id": 103,
      "title": null,
      "body": "Hilo: RAG multimodal en 280 caracteres...",
      "status": "pending"
    }
  }
}
```

### Draft sin contribuciones (B-002, B-020)
```json
{
  "id": "draft-empty-contrib",
  "weekOf": "2026-05-28",
  "topicTitle": "Sin fuentes",
  "topicSummary": "",
  "status": "GENERATED",
  "sourceContributions": [],
  "channels": { "newsletter": { "id": 200, "title": "X", "body": "Y", "status": "pending" } }
}
```

### Draft con canales faltantes (B-002)
```json
{
  "id": "draft-partial",
  "weekOf": "2026-05-28",
  "topicTitle": "Solo newsletter",
  "status": "GENERATED",
  "channels": {
    "newsletter": { "id": 300, "title": "T", "body": "B", "status": "pending" }
  }
}
```

### Draft con status REJECTED (B-010)
```json
{
  "id": "draft-rejected",
  "weekOf": "2026-05-21",
  "topicTitle": "Rechazado",
  "status": "REJECTED",
  "channels": { "newsletter": { "id": 400, "title": "T", "body": "B", "status": "rejected" } }
}
```

### Draft published (B-012)
```json
{
  "id": "draft-published",
  "weekOf": "2026-05-14",
  "topicTitle": "Publicado",
  "status": "PUBLISHED",
  "publishedAt": "2026-05-14T20:00:00Z",
  "channels": { "newsletter": { "id": 500, "title": "T", "body": "B", "status": "published" } }
}
```

---

## 2. Setup recomendado

### Instalar dependencias (no instalado actualmente)
```bash
cd Frontend
npm install --save-dev vitest @testing-library/react @testing-library/jest-dom @testing-library/user-event jsdom msw
```

### `vitest.config.js`
```js
import { defineConfig } from "vitest/config";
import react from "@vitejs/plugin-react";

export default defineConfig({
  plugins: [react()],
  test: {
    environment: "jsdom",
    setupFiles: ["./src/test/setup.js"],
    globals: true,
  },
});
```

### `src/test/setup.js`
```js
import "@testing-library/jest-dom";
import { afterEach } from "vitest";
import { cleanup } from "@testing-library/react";

afterEach(() => cleanup());
```

### `package.json` - agregar scripts
```json
{
  "scripts": {
    "test": "vitest",
    "test:run": "vitest run",
    "test:ui": "vitest --ui"
  }
}
```

---

## 3. Stubs de tests críticos

### Test del bug B-001 (título no se guarda)
```js
// src/api/__tests__/drafts.test.js
import { describe, it, expect, vi, beforeEach } from "vitest";
import { updateChannelDraft } from "../drafts";
import { api } from "../client";

vi.mock("../client", () => ({
  api: {
    get: vi.fn(),
    patch: vi.fn(),
  },
}));

describe("updateChannelDraft", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    import.meta.env.VITE_DRAFTS_USE_MOCK = "false";
  });

  it("debe enviar tanto title como content para newsletter", async () => {
    api.get.mockResolvedValueOnce({ data: { id: 101 } });
    api.patch.mockResolvedValueOnce({ data: {} });
    api.get.mockResolvedValueOnce({ data: { id: 1, summary: "x" } });
    api.get.mockResolvedValueOnce({ data: [] });

    await updateChannelDraft("draft-1", "newsletter", {
      title: "Nuevo título",
      body: "<p>Contenido</p>",
    });

    expect(api.patch).toHaveBeenCalledWith(
      expect.stringContaining("/content"),
      expect.objectContaining({
        title: "Nuevo título",
        content: "<p>Contenido</p>",
      })
    );
  });
});
```

### Test del bug B-005 (status casing)
```js
// src/api/__tests__/drafts-status.test.js
import { describe, it, expect } from "vitest";
import { listDrafts } from "../drafts";

describe("listDrafts - status casing", () => {
  it("debe devolver siempre status en UPPERCASE", async () => {
    const drafts = await listDrafts();
    drafts.forEach((d) => {
      expect(d.status).toMatch(/^(GENERATED|IN_REVIEW|APPROVED|PUBLISHED|REJECTED)$/);
    });
  });
});
```

### Test del bug B-003 (transición parcial)
```js
// src/api/__tests__/drafts-transition.test.js
import { describe, it, expect, vi } from "vitest";
import { transitionDraft } from "../drafts";
import { api } from "../client";

vi.mock("../client", () => ({
  api: { get: vi.fn(), patch: vi.fn() },
}));

describe("transitionDraft - rollback", () => {
  it("debe rollback si una transición individual falla", async () => {
    api.get.mockResolvedValueOnce({
      data: [
        { id: 1, targetPlatform: "NEWSLETTER", status: "IN_REVIEW" },
        { id: 2, targetPlatform: "LINKEDIN", status: "IN_REVIEW" },
        { id: 3, targetPlatform: "X", status: "IN_REVIEW" },
      ],
    });
    api.patch
      .mockResolvedValueOnce({}) // newsletter ok
      .mockResolvedValueOnce({}) // linkedin ok
      .mockRejectedValueOnce(new Error("Backend error")); // twitter fail

    await expect(transitionDraft("digest-1", "APPROVED")).rejects.toThrow();

    // Verificar que se intentó rollback (PATCH para volver al estado anterior)
    // O alternativamente, que la función NO siguió procesando después del fallo
    expect(api.patch).toHaveBeenCalledTimes(3); // intentos
    // En una implementación con rollback: esperar PATCHes adicionales de undo
  });
});
```

### Test del bug B-007 (XSS en URL)
```js
// src/views/__tests__/DraftEditor-link.test.jsx
import { describe, it, expect, vi } from "vitest";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { DraftEditor } from "../DraftEditor";
import { MemoryRouter, Route, Routes } from "react-router-dom";

describe("DraftEditor - link insertion security", () => {
  it("no debe permitir URLs javascript:", async () => {
    vi.spyOn(window, "prompt").mockReturnValue("javascript:alert(1)");
    const execSpy = vi.spyOn(document, "execCommand");

    render(
      <MemoryRouter initialEntries={["/editor/draft-1/newsletter"]}>
        <Routes>
          <Route path="/editor/:draftId/:channel" element={<DraftEditor />} />
        </Routes>
      </MemoryRouter>
    );

    // Esperar a que cargue
    await screen.findByText(/Guardar cambios/i);

    const linkBtn = screen.getByTitle(/Insertar enlace/i);
    await userEvent.click(linkBtn);

    expect(execSpy).not.toHaveBeenCalledWith("createLink", false, expect.stringContaining("javascript:"));
  });
});
```

### Test del bug B-011 (Twitter over-limit)
```js
// src/views/__tests__/DraftEditor-twitter.test.jsx
describe("DraftEditor Twitter limit", () => {
  it("debe deshabilitar save cuando supera 280 caracteres", async () => {
    render(/* DraftEditor con channel=twitter, body de 300 chars */);

    const saveBtn = await screen.findByRole("button", { name: /Guardar cambios/i });
    expect(saveBtn).toBeDisabled();
  });
});
```

### Test del bug B-014 (doble-click save)
```js
describe("DraftEditor doble-click", () => {
  it("no debe enviar 2 PATCH si el usuario hace doble-click", async () => {
    const patchSpy = vi.spyOn(api, "patch");
    render(/* DraftEditor */);

    const saveBtn = await screen.findByRole("button", { name: /Guardar cambios/i });
    await userEvent.dblClick(saveBtn);

    expect(patchSpy).toHaveBeenCalledTimes(1);
  });
});
```

### Test del bug B-004 (cache invalidation)
```js
describe("useFetch cache invalidation", () => {
  it("debe refrescar la lista de drafts tras editar uno", async () => {
    const { rerender } = render(<DraftsPage />);
    await screen.findByText("Draft #1 - Generado");

    // Simular edición + transición
    // Esto requiere mock global del API o MSW

    rerender(<DraftsPage />);
    await screen.findByText("Draft #1 - En revisión");
  });
});
```

---

## 4. Helpers compartidos

### `src/test/helpers/renderWithProviders.jsx`
```jsx
import { MemoryRouter } from "react-router-dom";
import { AuthProvider } from "../../context/AuthContext";

export function renderWithProviders(ui, { route = "/" } = {}) {
  return render(
    <MemoryRouter initialEntries={[route]}>
      <AuthProvider>{ui}</AuthProvider>
    </MemoryRouter>
  );
}
```

### `src/test/helpers/mockDrafts.js`
```js
export const FIXTURE_DRAFT_FULL = { /* ver sección 1 */ };
export const FIXTURE_DRAFT_REJECTED = { /* ver sección 1 */ };
export const FIXTURE_DRAFT_PUBLISHED = { /* ver sección 1 */ };

export function makeDraft(overrides = {}) {
  return { ...FIXTURE_DRAFT_FULL, ...overrides };
}
```

### MSW handlers (`src/test/handlers.js`)
```js
import { http, HttpResponse } from "msw";
import { FIXTURE_DRAFT_FULL } from "./helpers/mockDrafts";

export const handlers = [
  http.get("/api/weekly-digests/latest", () => HttpResponse.json([
    { id: 1, communityId: 1, communityName: "AI Builders", weekStart: "2026-05-23", weekEnd: "2026-05-29", summary: "x", status: "PENDING", createdAt: "2026-05-23T18:00:00Z" }
  ])),
  http.get("/api/weekly-digests/:id", () => HttpResponse.json({ /* digest */ })),
  http.get("/api/channel-drafts/by-digest/:id", () => HttpResponse.json([
    { id: 101, content: "<p>x</p>", targetPlatform: "NEWSLETTER", status: "GENERATED", createdAt: "2026-05-23T18:00:00Z", approvedAt: null, editorId: null, editorEmail: null, weeklyDigestId: 1 },
  ])),
  http.patch("/api/channel-drafts/:id/content", () => HttpResponse.json({ id: 101, content: "updated", targetPlatform: "NEWSLETTER", status: "GENERATED" })),
  http.patch("/api/channel-drafts/:id/start-review", () => HttpResponse.json({ status: "IN_REVIEW" })),
  http.patch("/api/channel-drafts/:id/approve", () => HttpResponse.json({ status: "APPROVED" })),
];
```

---

## 5. Prioridad de cobertura

Orden recomendado para implementar tests automatizados:

1. **B-001** (título no se guarda) — el test es simple y previene data loss
2. **B-005** (status casing) — afecta a todos los demás tests
3. **B-007** (XSS) — security-critical
4. **B-003** (transición parcial) — data integrity
5. **B-014** (doble-click) — race condition común
6. **B-011** (Twitter limit) — bug de validación simple
7. **B-004** (cache) — requiere mock global, más complejo
8. **B-002, B-020** (sourceContributions) — depende de decisión de backend
9. Resto en orden de severidad

## 6. CI sugerido

```yaml
# .github/workflows/test.yml
name: Frontend Tests
on: [push, pull_request]
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with:
          node-version: "20"
      - run: cd Frontend && npm ci
      - run: cd Frontend && npm run lint
      - run: cd Frontend && npm run test:run
      - run: cd Frontend && npm run build
```
