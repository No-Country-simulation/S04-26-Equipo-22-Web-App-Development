export const draftsMock = [
  {
    id: "draft-2026-W19",
    weekOf: "2026-05-08",
    topicTitle: "React performance e IA aplicada al contenido",
    topicSummary:
      "Esta semana la comunidad debatió intensamente sobre optimización en React 19 (memoization, suspense streaming) y casos prácticos de aplicar LLMs a flujos editoriales. También se compartieron recursos clave sobre testing en Java y patrones SQL.",
    sourceContributions: [
      {
        id: 101,
        type: "DISCUSSION",
        authorName: "María González",
        excerpt: "¿Cuándo conviene usar React.memo vs useMemo en componentes pesados?",
        reactionsCount: 42,
        commentsCount: 18,
        sourceUrl: "https://example.com/discussion/101",
        communityName: "Frontend Devs",
      },
      {
        id: 102,
        type: "RESOURCE",
        authorName: "Carlos Pérez",
        excerpt: "Guía completa de Suspense y streaming en React 19",
        reactionsCount: 87,
        commentsCount: 23,
        sourceUrl: "https://example.com/resource/102",
        communityName: "Frontend Devs",
      },
      {
        id: 103,
        type: "QUESTION",
        authorName: "Ana López",
        excerpt: "¿Alguien implementó RAG para resúmenes editoriales con OpenAI?",
        reactionsCount: 35,
        commentsCount: 29,
        sourceUrl: "https://example.com/question/103",
        communityName: "IA & Producto",
      },
      {
        id: 104,
        type: "SESSION",
        authorName: "Diego Ramírez",
        excerpt: "Live coding: testing unitario en Spring Boot con JUnit 5",
        reactionsCount: 56,
        commentsCount: 11,
        sourceUrl: "https://example.com/session/104",
        communityName: "Backend Crew",
      },
    ],
    channels: {
      newsletter: {
        channel: "newsletter",
        title: "Lo mejor de la comunidad — semana del 8 de mayo",
        body: `# Lo mejor de la comunidad

Esta semana la comunidad puso el foco en dos grandes temas: **performance en React 19** y **aplicaciones prácticas de IA al contenido editorial**.

## Temas destacados

- **React.memo vs useMemo** — María González lanzó una discusión que sumó 42 reacciones y 18 comentarios desglosando cuándo cada uno aporta valor real.
- **Suspense y streaming en React 19** — Carlos Pérez compartió una guía completa que se volvió el recurso más reaccionado del periodo.
- **RAG aplicado a resúmenes editoriales** — Ana López abrió una pregunta que ya tiene 29 respuestas con casos reales de uso.

## Aprendizajes de la semana

> "La memoization no es gratis: medí antes de optimizar." — María González

Gracias a quienes participaron. Seguimos.
`,
        status: "pending",
      },
      linkedin: {
        channel: "linkedin",
        title: "Resumen semanal de la comunidad",
        body: `Esta semana en nuestra comunidad surgieron conversaciones muy potentes 🚀

🔹 Performance en React 19: ¿cuándo memoizar realmente importa?
🔹 Aplicaciones prácticas de IA al flujo editorial
🔹 Testing unitario en Spring Boot

¿Cuál de estos temas te impactó más esta semana? 👇

#desarrolloweb #react #ia #comunidad`,
        status: "pending",
      },
      twitter: {
        channel: "twitter",
        body: `🔥 Lo mejor de la semana en la comunidad:

• React 19 + performance
• IA aplicada al contenido
• Testing en Spring Boot
• SQL optimization

#webdev #react #ai`,
        status: "pending",
      },
    },
    status: "GENERATED",
    createdAt: "2026-05-08T20:00:00Z",
    updatedAt: "2026-05-08T20:00:00Z",
  },
  {
    id: "draft-2026-W18",
    weekOf: "2026-05-01",
    topicTitle: "Patrones de diseño y arquitectura en monorepos",
    topicSummary:
      "La semana del 1 de mayo se centró en cómo escalar codebases compartidas. Hubo sesiones en vivo sobre Nx y Turbo, y una discusión muy activa sobre cuándo conviene partir un monorepo.",
    sourceContributions: [
      {
        id: 201,
        type: "SESSION",
        authorName: "Luis Vera",
        excerpt: "Workshop: del monolito a un monorepo Nx en producción",
        reactionsCount: 71,
        commentsCount: 32,
        sourceUrl: "https://example.com/session/201",
        communityName: "Architecture Guild",
      },
      {
        id: 202,
        type: "DISCUSSION",
        authorName: "Sofía Núñez",
        excerpt: "¿Cuándo NO usar un monorepo? Casos donde nos salió caro",
        reactionsCount: 58,
        commentsCount: 41,
        sourceUrl: "https://example.com/discussion/202",
        communityName: "Architecture Guild",
      },
    ],
    channels: {
      newsletter: {
        channel: "newsletter",
        title: "Monorepos en la práctica — semana del 1 de mayo",
        body: `# Monorepos en la práctica

La semana se llenó de aprendizajes sobre arquitectura compartida...

## Sesiones destacadas

- Workshop de Nx en producción con 71 reacciones
- Discusión sobre los costos de mantener un monorepo

Sigue la conversación en la comunidad.
`,
        status: "edited",
        editedAt: "2026-05-04T14:22:00Z",
      },
      linkedin: {
        channel: "linkedin",
        title: "¿Monorepo o multi-repo? La discusión de la semana",
        body: `Esta semana en la comunidad debatimos cuándo un monorepo ayuda y cuándo se vuelve un problema.

Spoiler: la respuesta es "depende", pero hay matices.

#arquitecturasoftware #monorepo`,
        status: "approved",
      },
      twitter: {
        channel: "twitter",
        body: `¿Monorepo o multi-repo? 🤔

La comunidad debatió esta semana. Resumen:

✅ Útil con código compartido
❌ Caro si los equipos son independientes

#dev`,
        status: "approved",
      },
    },
    status: "IN_REVIEW",
    createdAt: "2026-05-01T20:00:00Z",
    updatedAt: "2026-05-04T14:22:00Z",
  },
  {
    id: "draft-2026-W17",
    weekOf: "2026-04-24",
    topicTitle: "Onboarding técnico: la primera semana ideal",
    topicSummary:
      "Conversaciones sobre cómo diseñar las primeras semanas de un dev nuevo en el equipo, con foco en mentoring y documentación viva.",
    sourceContributions: [
      {
        id: 301,
        type: "QUESTION",
        authorName: "Pablo Iriarte",
        excerpt: "¿Cómo hacen el onboarding técnico de un dev junior en su equipo?",
        reactionsCount: 92,
        commentsCount: 54,
        sourceUrl: "https://example.com/question/301",
        communityName: "Tech Leadership",
      },
    ],
    channels: {
      newsletter: {
        channel: "newsletter",
        title: "Onboarding técnico que sí funciona",
        body: `# Onboarding técnico que sí funciona

Resumen de la semana...
`,
        status: "approved",
      },
      linkedin: {
        channel: "linkedin",
        title: "5 ideas de la comunidad para mejorar onboarding técnico",
        body: `5 ideas que la comunidad propuso esta semana para mejorar el onboarding...`,
        status: "approved",
      },
      twitter: {
        channel: "twitter",
        body: `📌 Buen onboarding técnico = menos rotación.

5 ideas que salieron en la comunidad esta semana 🧵`,
        status: "approved",
      },
    },
    status: "PUBLISHED",
    createdAt: "2026-04-24T20:00:00Z",
    updatedAt: "2026-04-27T12:00:00Z",
    approvedBy: { id: 1, email: "editor@talentcircle.dev" },
    approvedAt: "2026-04-27T11:30:00Z",
    publishedAt: "2026-04-27T12:00:00Z",
  },
];
