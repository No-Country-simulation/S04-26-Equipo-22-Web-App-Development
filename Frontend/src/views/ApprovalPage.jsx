import { useEffect, useState } from "react";
import { useParams, Link, useNavigate } from "react-router-dom";
import { Inbox, ArrowLeft, ArrowRight, AlertCircle } from "lucide-react";
import * as draftsApi from "../api/drafts";
import * as draftExport from "../api/draftExport";
import { CHANNEL_LABELS, STATUS_LABELS, CHANNELS } from "../data/draftSelectors";
import ApprovalFlow from "../components/approval/ApprovalFlow";
import ApprovalButtons from "../components/approval/ApprovalButtons";
import ChannelIcon from "../components/ChannelIcon";
import "./ApprovalPage.css";

function formatDate(iso) {
  if (!iso) return "—";
  try {
    return new Date(iso).toLocaleString("es-AR");
  } catch {
    return iso;
  }
}

function ApprovalList() {
  const [drafts, setDrafts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    draftsApi
      .listDrafts()
      .then(setDrafts)
      .catch((err) => setError(err?.message || "No se pudo cargar"))
      .finally(() => setLoading(false));
  }, []);

  if (loading) {
    return (
      <div className="approval-list approval-list--loading">
        <span className="approval-spinner" />
        Cargando borradores…
      </div>
    );
  }
  if (error) {
    return (
      <div className="approval-list">
        <p className="approval-list__error">
          <AlertCircle aria-hidden="true" /> {error}
        </p>
      </div>
    );
  }

  if (drafts.length === 0) {
    return (
      <div className="approval-list">
        <div className="approval-empty">
          <div className="approval-empty__icon" aria-hidden="true">
            <Inbox />
          </div>
          <h3>No hay borradores en revisión</h3>
          <p>
            Cuando la IA genere nuevos borradores semanales, aparecerán aquí
            listos para tu revisión y aprobación.
          </p>
        </div>
      </div>
    );
  }

  return (
    <div className="approval-list">
      {drafts.map((d) => (
        <Link key={d.id} to={`/approval/${d.id}`} className="approval-list__item">
          <div className="approval-list__main">
            <div className="approval-list__title-row">
              <h3>{d.topicTitle}</h3>
              <span className={`approval-list__status status--${d.status.toLowerCase()}`}>
                <span className="approval-list__status-dot" aria-hidden="true" />
                {STATUS_LABELS[d.status] || d.status}
              </span>
            </div>
            <p className="approval-list__summary">{d.topicSummary}</p>
            <small>
              <span className="approval-list__week">Semana del {d.weekOf}</span>
              <span className="approval-list__sep" aria-hidden="true">·</span>
              <span>actualizado {formatDate(d.updatedAt)}</span>
            </small>
          </div>
          <span className="approval-list__chevron" aria-hidden="true">
            <ArrowRight />
          </span>
        </Link>
      ))}
    </div>
  );
}

function ApprovalDetail({ id }) {
  const navigate = useNavigate();
  const [draft, setDraft] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [acting, setActing] = useState(false);
  const [copied, setCopied] = useState(null);

  const onCopyMarkdown = async (channel) => {
    try {
      await draftExport.copyMarkdown(draft, channel);
      setCopied(channel);
      setTimeout(() => setCopied(null), 1800);
    } catch {
      setError("No se pudo copiar al portapapeles");
    }
  };

  const load = () => {
    setLoading(true);
    return draftsApi
      .getDraft(id)
      .then(setDraft)
      .catch((err) => setError(err?.message || "No se pudo cargar"))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    load();
  }, [id]); // eslint-disable-line react-hooks/exhaustive-deps

  const onChangeStatus = async (next) => {
    setActing(true);
    setError(null);
    try {
      const updated = await draftsApi.transitionDraft(id, next);
      setDraft(updated);
    } catch (err) {
      setError(err?.message || "Transición no permitida");
    } finally {
      setActing(false);
    }
  };

  if (loading) return <p>Cargando…</p>;
  if (error && !draft) return <p style={{ color: "crimson" }}>{error}</p>;
  if (!draft) return <p>Borrador no encontrado.</p>;

  return (
    <div>
      <Link to="/approval" className="approval-back">
        <ArrowLeft aria-hidden="true" /> Volver al listado
      </Link>

      <header className="approval-detail__header">
        <h2>{draft.topicTitle}</h2>
        <p className="approval-detail__summary">{draft.topicSummary}</p>
        <small>
          Semana del {draft.weekOf} · creado {formatDate(draft.createdAt)} · actualizado{" "}
          {formatDate(draft.updatedAt)}
        </small>
      </header>

      <section className="approval-sources">
        <h2>Contribuciones que originaron este borrador</h2>
        <p className="approval-sources__hint">
          El LLM seleccionó este tema a partir de las publicaciones, preguntas y recursos más
          relevantes de la semana.
        </p>
        {(!draft.sourceContributions || draft.sourceContributions.length === 0) ? (
          <p className="approval-sources__empty">Sin contribuciones registradas.</p>
        ) : (
          <ul className="approval-sources__list">
            {draft.sourceContributions.map((c) => (
              <li key={c.id} className="approval-sources__item">
                <header>
                  <span className={`approval-sources__type type--${c.type.toLowerCase()}`}>
                    {c.type}
                  </span>
                  <span className="approval-sources__community">{c.communityName}</span>
                </header>
                <p className="approval-sources__excerpt">"{c.excerpt}"</p>
                <footer>
                  <span><strong>{c.authorName}</strong></span>
                  <span title="Reacciones">❤ {c.reactionsCount}</span>
                  <span title="Comentarios">💬 {c.commentsCount}</span>
                  {c.sourceUrl && (
                    <a href={c.sourceUrl} target="_blank" rel="noreferrer">
                      Ver original ↗
                    </a>
                  )}
                </footer>
              </li>
            ))}
          </ul>
        )}
      </section>

      <section className="approval-content">
        <h2>Estado del flujo</h2>
        <ApprovalFlow draft={draft} />
      </section>

      <section className="approval-channels">
        <h2>Versiones por canal</h2>
        <div className="approval-channels__grid">
          {CHANNELS.map((c) => {
            const ch = draft.channels[c];
            if (!ch) return null;
            return (
              <article key={c} className="approval-channels__card">
                <header>
                  <span style={{ display: "inline-flex", alignItems: "center", gap: 6, color: "#2563eb" }}>
                    <ChannelIcon channel={c} size={16} />
                    <strong style={{ color: "#111827" }}>{CHANNEL_LABELS[c]}</strong>
                  </span>
                  <span className="approval-channels__chstatus">{ch.status}</span>
                </header>
                {ch.title && <h4>{ch.title}</h4>}
                <pre className="approval-channels__body">{ch.body}</pre>
                <div className="approval-channels__actions">
                  <button onClick={() => navigate(`/editor/${draft.id}/${c}`)}>Editar</button>
                  <button
                    onClick={() => navigate(`/preview?draftId=${draft.id}&channel=${c}`)}
                  >
                    Preview
                  </button>
                  <button
                    onClick={() => onCopyMarkdown(c)}
                    title="Copiar al portapapeles como Markdown"
                  >
                    {copied === c ? "✓ Copiado" : "Copiar MD"}
                  </button>
                  <button
                    onClick={() => draftExport.downloadJson(draft, c)}
                    title="Descargar como JSON estructurado"
                  >
                    JSON
                  </button>
                  <button
                    onClick={() => draftExport.downloadMarkdown(draft, c)}
                    title="Descargar como Markdown"
                  >
                    MD
                  </button>
                </div>
              </article>
            );
          })}
        </div>
      </section>

      <section className="approval-actions">
        <h2>Acciones</h2>
        {error && <p style={{ color: "crimson" }}>{error}</p>}
        <ApprovalButtons
          status={draft.status}
          onChangeStatus={onChangeStatus}
          disabled={acting}
        />
      </section>
    </div>
  );
}

export default function ApprovalPage() {
  const { id } = useParams();

  return (
    <div className="approval-page">
      <header className="approval-header">
        <h1>Proceso de aprobación</h1>
        <p>Revisa y aprueba el contenido antes de su publicación.</p>
      </header>

      {id ? <ApprovalDetail id={id} /> : <ApprovalList />}
    </div>
  );
}
