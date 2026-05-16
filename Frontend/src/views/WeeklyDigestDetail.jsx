// src/views/WeeklyDigestDetail.jsx
import { useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { weeklyDigestMock } from "../data/weeklyDigestMock";
import "./WeeklyDigestDetail.css";

function WeeklyDigestDetail() {
  const navigate = useNavigate();
  const { id } = useParams();
  const [digest, setDigest] = useState(weeklyDigestMock);
  const [expandedArticle, setExpandedArticle] = useState(null);

  const handleApproveAll = () => {
    alert("✅ Todos los borradores aprobados");
  };

  const handleApproveArticle = (articleId) => {
    alert(`✅ Artículo ${articleId} aprobado`);
  };

  const toggleExpand = (articleId) => {
    setExpandedArticle(expandedArticle === articleId ? null : articleId);
  };

  return (
    <div className="digest-detail">
      <header className="digest-header">
        <button className="back-button" onClick={() => navigate(-1)}>
          ← Volver
        </button>
        <h1 className="digest-title">Digest Semanal</h1>
        <p className="digest-date">
          Semana del {digest.week_start} al {digest.week_end}
        </p>
      </header>

      <section className="digest-summary">
        <p>{digest.summary}</p>
      </section>

      <div className="digest-actions">
        <button className="btn-approve-all" onClick={handleApproveAll}>
          ✅ Aprobar todo y publicar
        </button>
      </div>

      <section className="articles-list">
        <h2>Artículos destacados</h2>
        {digest.articles.map((article) => (
          <div key={article.id} className="article-card">
            <div className="article-header">
              <div>
                <h3>{article.title}</h3>
                <p className="article-meta">
                  Por {article.author} · Puntaje de relevancia: {article.relevance_score}
                </p>
              </div>
              <button className="btn-approve-article" onClick={() => handleApproveArticle(article.id)}>
                ✅ Aprobar
              </button>
            </div>

            <p className="article-summary">{article.summary}</p>

            <button className="btn-toggle" onClick={() => toggleExpand(article.id)}>
              {expandedArticle === article.id ? "▲ Ocultar borradores" : "▼ Ver borradores generados"}
            </button>

            {expandedArticle === article.id && (
              <div className="article-drafts">
                <div className="draft-card">
                  <h4>📧 Newsletter</h4>
                  <p>{article.drafts.newsletter}</p>
                </div>
                <div className="draft-card">
                  <h4>💼 LinkedIn</h4>
                  <p>{article.drafts.linkedin}</p>
                </div>
                <div className="draft-card">
                  <h4>🐦 Twitter</h4>
                  <p>{article.drafts.twitter}</p>
                </div>
              </div>
            )}
          </div>
        ))}
      </section>
    </div>
  );
}

export default WeeklyDigestDetail;