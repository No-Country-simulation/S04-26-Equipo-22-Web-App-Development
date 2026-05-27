import { Fragment, useState, useRef, useEffect } from "react";
import { useParams, Link, useNavigate } from "react-router-dom";
import { Inbox, ArrowLeft, ArrowRight, AlertCircle, Bot, Sparkles, Eye, CheckCircle2, Send } from "lucide-react";
import * as draftsApi from "../api/drafts";
import * as draftExport from "../api/draftExport";
import { CHANNEL_LABELS, STATUS_LABELS, CHANNELS } from "../data/draftSelectors";
import ApprovalFlow from "../components/approval/ApprovalFlow";
import ApprovalButtons from "../components/approval/ApprovalButtons";
import ExportModal from "../components/approval/ExportModal";
import ChannelIcon from "../components/ChannelIcon";
import { useAuth } from "../context/AuthContext";
import { useFetch } from "../hooks/useFetch";
import { useAsyncAction } from "../hooks/useAsyncAction";
import { formatDateTime } from "../utils/formatDate";
import "./ApprovalPage.css";

function ApprovalList() {
  const { data: drafts = [], loading, error } = useFetch(() => draftsApi.listDrafts());

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
              <span>actualizado {formatDateTime(d.updatedAt)}</span>
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

function AIReasoningCard({ draft }) {
  const contributions = draft.sourceContributions;
  if (!contributions || contributions.length === 0) return null;

  const topSignals = [...contributions]
    .sort((a, b) => (b.reactionsCount + b.commentsCount) - (a.reactionsCount + a.commentsCount))
    .slice(0, 3);

  return (
    <section className="approval-ai-card">
      <header className="approval-ai-card__header">
        <Bot size={15} aria-hidden="true" />
        <span>La IA seleccionó este tema porque…</span>
      </header>
      <ul className="approval-ai-card__signals">
        {topSignals.map((c) => {
          const score = c.reactionsCount + c.commentsCount;
          const excerpt = c.excerpt.length > 88 ? c.excerpt.slice(0, 88) + "…" : c.excerpt;
          return (
            <li key={c.id} className="approval-ai-card__signal">
              <div className="approval-ai-card__signal-text">
                <span className="approval-ai-card__excerpt">"{excerpt}"</span>
                <span className="approval-ai-card__meta">{c.authorName} · {c.communityName}</span>
              </div>
              <span className="approval-ai-card__score">{score} pts</span>
            </li>
          );
        })}
      </ul>
      <p className="approval-ai-card__footer">
        Basado en {contributions.length} contribuciones de la semana
      </p>
    </section>
  );
}

const TIMELINE_STEPS = [
  { key: "GENERATED", label: "Generado",    Icon: Sparkles,     hint: "Viernes 18:00", dateKey: "createdAt"   },
  { key: "IN_REVIEW", label: "En revisión", Icon: Eye,          hint: "Lunes",         dateKey: null          },
  { key: "APPROVED",  label: "Aprobado",    Icon: CheckCircle2, hint: null,            dateKey: "approvedAt"  },
  { key: "PUBLISHED", label: "Publicado",   Icon: Send,         hint: null,            dateKey: "publishedAt" },
];

const STEP_ORDER = { GENERATED: 0, IN_REVIEW: 1, APPROVED: 2, PUBLISHED: 3, REJECTED: 1 };

function DraftTimeline({ draft }) {
  const activeIdx = STEP_ORDER[draft.status] ?? 0;
  const isRejected = draft.status === "REJECTED";

  return (
    <section className="approval-timeline-wrap">
      <h2>Ciclo de publicación</h2>
      <div className="approval-timeline">
        {TIMELINE_STEPS.map((step, idx) => {
          const isDone = idx < activeIdx;
          const isActive = idx === activeIdx && !isRejected;
          const isRej = isRejected && idx === 1;
          const date = step.dateKey ? draft[step.dateKey] : null;

          let mod = "pending";
          if (isRej) mod = "rejected";
          else if (isDone) mod = "done";
          else if (isActive) mod = "active";

          return (
            <Fragment key={step.key}>
              {idx > 0 && (
                <div className={`approval-timeline__bar${isDone || isActive ? " approval-timeline__bar--filled" : ""}`} />
              )}
              <div className="approval-timeline__step">
                <div className={`approval-timeline__dot approval-timeline__dot--${mod}`}>
                  <step.Icon size={14} />
                </div>
                <div className="approval-timeline__text">
                  <span className={`approval-timeline__label${isActive ? " is-active" : ""}`}>
                    {step.label}
                  </span>
                  {date ? (
                    <span className="approval-timeline__date">{formatDateTime(date)}</span>
                  ) : step.hint ? (
                    <span className="approval-timeline__hint">{step.hint}</span>
                  ) : null}
                </div>
              </div>
            </Fragment>
          );
        })}
      </div>
    </section>
  );
}

function ApprovalDetail({ id }) {
  const navigate = useNavigate();
  const { user } = useAuth();
  const { data: draft, loading, error: loadError, setData: setDraft, refetch } = useFetch(() => draftsApi.getDraft(id), [id]);
  const { acting, error: actionError, run } = useAsyncAction();
  const [copied, setCopied] = useState(null);
  const [exportModal, setExportModal] = useState(null);
  const copiedTimerRef = useRef(null);

  useEffect(() => {
    return () => clearTimeout(copiedTimerRef.current);
  }, []);

  const error = actionError || loadError;

  const onCopyMarkdown = async (channel) => {
    try {
      await draftExport.copyMarkdown(draft, channel);
      setCopied(channel);
      clearTimeout(copiedTimerRef.current);
      copiedTimerRef.current = setTimeout(() => setCopied(null), 1800);
    } catch {
      /* clipboard error – silent */
    }
  };

  const onChangeStatus = async (next) => {
    try {
      const updated = await run(() => draftsApi.transitionDraft(id, next, { editorId: user?.id }));
      setDraft(updated);
    } catch {
      /* error is already captured in actionError */
    }
  };

  const onOpenPublishModal = () => {
    const firstChannel = CHANNELS.find((c) => draft.channels[c]);
    if (firstChannel) setExportModal(firstChannel);
  };

  const onOpenExportModal = () => {
    const firstChannel = CHANNELS.find((c) => draft.channels[c]);
    if (firstChannel) setExportModal(firstChannel);
  };

  const onConfirmPublish = async () => {
    await onChangeStatus("PUBLISHED");
    setExportModal(null);
  };

  if (loading) return <p>Cargando…</p>;
  if (error && !draft) return <p className="approval-detail__error">{error}</p>;
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
          Semana del {draft.weekOf} · creado {formatDateTime(draft.createdAt)} · actualizado{" "}
          {formatDateTime(draft.updatedAt)}
        </small>
      </header>

      <AIReasoningCard draft={draft} />

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

      <DraftTimeline draft={draft} />

      <section className="approval-channels">
        <h2>Versiones por canal</h2>
        <div className="approval-channels__grid">
          {CHANNELS.map((c) => {
            const ch = draft.channels[c];
            if (!ch) return null;
            return (
              <article key={c} className="approval-channels__card">
                <header>
                  <span className="approval-channels__icon-wrap">
                    <ChannelIcon channel={c} size={16} />
                    <strong className="approval-channels__label">{CHANNEL_LABELS[c]}</strong>
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
        {error && <p className="approval-detail__error">{error}</p>}
        <ApprovalButtons
          status={draft.status}
          onChangeStatus={onChangeStatus}
          onPublish={onOpenPublishModal}
          onExport={onOpenExportModal}
          disabled={acting}
        />
      </section>

      {exportModal && (
        <ExportModal
          draft={draft}
          channel={exportModal}
          onClose={() => setExportModal(null)}
          onConfirmPublish={onConfirmPublish}
        />
      )}
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
