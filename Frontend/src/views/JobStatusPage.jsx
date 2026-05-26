import "./JobStatusPage.css";

import { useEffect, useState } from "react";

import {
  getWeeklyStatistics,
  getWeeklyDigest,
} from "../api/weeklyDigest";

function JobStatusPage() {
  const [statistics, setStatistics] = useState(null);
  const [digest, setDigest] = useState(null);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [statisticsData, digestData] = await Promise.all([
          getWeeklyStatistics(),
          getWeeklyDigest(),
        ]);

       

        setStatistics(statisticsData);
        setDigest(digestData);

      } catch (err) {
        console.error(err);
        console.error(err.response?.data);

        setError("Error al cargar datos");
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, []);

  if (loading) {
    return <p>Cargando...</p>;
  }

  if (error) {
    return <p>{error}</p>;
  }

  if (!statistics || !digest) {
    return <p>No hay datos disponibles</p>;
  }

  const hasData = statistics.totalPosts > 0;

  // KPIs
  const kpis = [
    {
      title: "Posts totales",
      value: statistics.totalPosts,
      extra: "Publicaciones analizadas",
    },
    {
      title: "Preguntas",
      value: statistics.totalQuestions,
      extra: "Contenido Q&A",
    },
    {
      title: "Recursos",
      value: statistics.totalResources,
      extra: "Material compartido",
    },
    {
      title: "Comentarios",
      value: statistics.totalComments,
      extra: "Interacciones totales",
    },
    {
      title: "Reacciones",
      value: statistics.totalReactions,
      extra: "Engagement total",
    },
    {
      title: "Sesiones",
      value: statistics.totalSessions,
      extra: "Eventos detectados",
    },
  ];

  // STATUS
  const digestStatusLabel =
    statistics.totalPosts > 0
      ? "Procesado"
      : "Sin actividad";

  // FECHA
  const lastRunDate = new Date(
    digest.weekStart
  ).toLocaleString("es-AR", {
    dateStyle: "medium",
    timeStyle: "short",
  });

  // PIPELINE
  const pipeline = [
    {
      name: "Recolección RSS",
      status:
        statistics.totalPosts > 0
          ? "OK"
          : "Pendiente",
    },
    {
      name: "Ranking IA",
      status:
        digest.topReactedPosts.length > 0
          ? "OK"
          : "Pendiente",
    },
    {
      name: "Generación drafts",
      status:
        digest.topResources.length > 0
          ? "OK"
          : "Sin datos",
    },
    {
      name: "Envío inbox",
      status:
        statistics.totalPosts > 0
          ? "OK"
          : "Pendiente",
    },
  ];

  const getBadgeClass = (status) => {
    switch (status) {
      case "OK":
        return "success";

      case "Pendiente":
        return "warning";

      case "Sin datos":
        return "neutral";

      default:
        return "warning";
    }
  };

  return (
    <section className="job-status-page">

      <div className="job-header">
        <div>
          <p className="section-label">
            Digest semanal
          </p>

          <h1>Estado del job semanal</h1>

          <span className="job-status processed">
            {digestStatusLabel}
          </span>
        </div>
      </div>

      {!hasData && (
        <div className="empty-state">
          No hay actividad registrada esta semana.
        </div>
      )}

      <div className="metrics-grid">
        {kpis.map((kpi) => (
          <div
            className="metric-card"
            key={kpi.title}
          >
            <span>{kpi.title}</span>

            <strong>{kpi.value}</strong>

            <small>{kpi.extra}</small>
          </div>
        ))}
      </div>

      <div className="status-grid">

        <div className="status-card">
          <h3>Último procesamiento</h3>

          <p>{lastRunDate}</p>

          <span>
            Semana:
            {" "}
            {new Date(
              statistics.weekStart
            ).toLocaleDateString("es-AR")}
            {" - "}
            {new Date(
              statistics.weekEnd
            ).toLocaleDateString("es-AR")}
          </span>
        </div>

        <div className="status-card">
          <h3>Pipeline</h3>

          {pipeline.map((item) => (
            <div
              key={item.name}
              className="pipeline-row"
            >
              <span>{item.name}</span>

              <span
                className={`badge ${getBadgeClass(item.status)}`}
              >
                {item.status}
              </span>
            </div>
          ))}
        </div>

      </div>

    </section>
  );
}

export default JobStatusPage;