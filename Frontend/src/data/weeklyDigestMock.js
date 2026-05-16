// src/data/weeklyDigestMock.js
export const weeklyDigestMock = {
  id: "digest-001",
  week_start: "2026-05-04",
  week_end: "2026-05-10",
  status: "pending",
  summary: "Esta semana la comunidad compartió contenido increíble sobre React, Java y buenas prácticas.",
  articles: [
    {
      id: 1,
      title: "React 19 - Todo lo que necesitas saber",
      author: "dev_react",
      url: "https://dev.to/...",
      relevance_score: 0.94,
      summary: "React 19 introduce el hook `use` que permite manejar promesas directamente en el renderizado.",
      drafts: {
        newsletter: "React 19 llegó con cambios importantes. El hook `use` permite manejar promesas directamente en el renderizado, algo que antes era imposible sin hooks complejos. Este artículo explica en detalle cómo migrar y aprovechar las nuevas features.",
        linkedin: "🚀 React 19 ya está aquí. El hook `use` cambia las reglas del juego. ¿Lo estás usando? Te cuento cómo funciona en 3 pasos. 👇",
        twitter: "React 19 trae `use`: promesas directas en render. Un antes y después. 🔥"
      }
    },
    {
      id: 2,
      title: "10 trucos de PostgreSQL que todo backend debe conocer",
      author: "pg_expert",
      url: "https://dev.to/...",
      relevance_score: 0.87,
      summary: "Optimizaciones y trucos poco conocidos de PostgreSQL para mejorar el rendimiento.",
      drafts: {
        newsletter: "PostgreSQL es una base de datos poderosa pero con muchos trucos ocultos. Este artículo recopila 10 tips que van desde índices parciales hasta particionamiento avanzado.",
        linkedin: "🐘 PostgreSQL: 10 trucos que uso todos los días. El tip #4 me ahorró horas de consultas lentas. ¿Cuál es tu favorito?",
        twitter: "10 trucos de PostgreSQL que no sabías que necesitabas. El #7 te va a sorprender. 🐘"
      }
    }
  ]
};