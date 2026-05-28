# Paquete de Auditoría y Tests - Borradores

Documentación enfocada en el flujo de **borradores** (drafts), el componente con más bloqueos del proyecto.

## Contenido

| Archivo | Propósito |
|---------|-----------|
| [AUDIT.md](AUDIT.md) | 24 bugs identificados con severidad P0-P3, archivo:línea afectados, causa y fix sugerido |
| [TEST_PLAN.md](TEST_PLAN.md) | 54 casos de prueba manuales agrupados en 6 suites con pasos reproducibles |
| [TEST_FIXTURES.md](TEST_FIXTURES.md) | Datos de prueba JSON + stubs de tests automatizados con Vitest |

## Top 5 bloqueantes a resolver primero

1. **B-001** — `updateChannelDraft` no envía el title al backend (data loss)
2. **B-002** — `sourceContributions` siempre vacío en modo real (feature rota)
3. **B-003** — `transitionDraft` deja canales inconsistentes en fallo parcial
4. **B-004** — Cache no se invalida tras editar/transicionar (datos stale)
5. **B-005** — Casing inconsistente de status entre mock y real

## Cómo usar este paquete

### Para desarrollo
1. Lee `AUDIT.md` y prioriza por severidad
2. Asigna IDs (B-XXX) a issues en tu tracker
3. Cada fix referencia el ID del bug en su PR

### Para QA
1. Imprime `TEST_PLAN.md` (o duplícalo en un Google Sheet)
2. Ejecuta cada suite secuencialmente
3. Reporta fallos como `FAIL T2-06 con browser X` referenciando el ID del test

### Para automatización
1. Sigue el setup en `TEST_FIXTURES.md`
2. Implementa stubs en orden de prioridad indicado
3. Integra a CI vía el workflow sugerido

## Métricas

- 4 archivos de código con bloqueos críticos
- 9 archivos de código auditados completamente
- 54 casos de test (manuales)
- 7 tests automatizados sugeridos (stubs incluidos)
- 0 frameworks de test configurados (recomendación: Vitest + RTL + MSW)
