import { Fragment, useState, useRef, useEffect } from "react";
import { useParams, Link, useNavigate } from "react-router-dom";
import {
  Inbox,
  ArrowLeft,
  ArrowUpRight,
  AlertCircle,
  Bot,
  Sparkles,
  Eye,
  CheckCircle2,
  Send,
  RefreshCw,
  CheckCheck,
  X,
  Hash,
  Calendar,
  Pencil,
  Copy,
  FileJson,
  FileText,
  ExternalLink,
} from "lucide-react";
import * as draftsApi from "../api/drafts";
import * as draftExport from "../api/draftExport";
import { regenerateDrafts as regenerateDraftsApi } from "../api/weeklyDigest";
import { CHANNEL_LABELS, STATUS_LABELS, CHANNELS } from "../data/draftSelectors";
import ApprovalButtons from "../components/approval/ApprovalButtons";
import ExportModal from "../components/approval/ExportModal";
import ChannelIcon from "../components/ChannelIcon";
import { useFetch } from "../hooks/useFetch";
import { useAsyncAction } from "../hooks/useAsyncAction";
import { formatDateTime } from "../utils/formatDate";
import "./ApprovalPage.css";

/* ------------------------------------------------------------------ */
/* helpers                                                            */
/* ------------------------------------------------------------------ */

const fmtId = (id) => String(id ?? "").padStart(3, "0");

const CHANNEL_KICKERS = {
  newsletter: "NEWSLETTER",
  linkedin: "LINKEDIN",
  twitter: "X / TWITTER",
};

/* ------------------------------------------------------------------ */
/* List view — tabular wire feed                                      */
/* ------------------------------------------------------------------ */

function ApprovalList() {
  const { data: drafts = [], loading, error } = useFetch(() => draftsApi.listDrafts());

  if (loading) {
    return (
      <div className="approval-list approval-list--loading">
        <span className="approval-spinner" aria-hidden="true" />
        <span>Cargando borradores…</span>
      </div>
    );
  }

  if (error) {
    return (
      <div className="approval-list">
        <p className="approval-list__error">
          <AlertCircle size={15} aria-hidden="true" /> {error}
        </p>
      </div>
    );
  }

  if (drafts.length === 0) {
    return (
      <div className="approval-list">
        <div className="approval-empty">
          <div className="approval-empty__icon" aria-hidden="true">
            <Inbox size={22} />
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
      <div className="approval-list__head" aria-hidden="true">
        <span>ID</span>
        <span>Tema</span>
        <span>Semana</span>
        <span>Estado</span>
      </div>

      {drafts.map((d) => (
        <Link key={d.id} to={`/approval/${d.id}`} className="approval-list__item">
          <span className="approval-list__id">
            <span className="approval-list__id-rule" aria-hidden="true" />
            <span className="approval-list__id-num">№{fmtId(d.id)}</span>
          </span>

          <div className="approval-list__main">
            <h3 className="approval-list__title">{d.topicTitle}</h3>
            <p className="approval-list__summary">{d.topicSummary}</p>
          </div>

          <span className="approval-list__week">
            <Calendar size={11} aria-hidden="true" />
            {d.weekOf}
          </span>

          <span className={`approval-status status--${d.status.toLowerCase()}`}>
            <span className="approval-status__dot" aria-hidden="true" />
            {STATUS_LABELS[d.status] || d.status}
          </span>

          <span className="approval-list__chevron" aria-hidden="true">
            <ArrowUpRight size={14} />
          </span>
        </Link>
      ))}
    </div>
  );
}

/* ------------------------------------------------------------------ */
/* AI reasoning — compact margin block                                */
/* ------------------------------------------------------------------ */

function AIReasoningBlock({ draft }) {
  const contributions = draft.sourceContributions;
  if (!contributions || contributions.length === 0) return null;

  const topSignals = [...contributions]
    .sort((a, b) => (b.reactionsCount + b.commentsCount) - (a.reactionsCount + a.commentsCount))
    .slice(0, 3);

  return (
    <section className="approval-reasoning" aria-label="Razonamiento de la IA">
      <header className="approval-reasoning__head">
        <Bot size={13} aria-hidden="true" />
        <span>Por qué la IA eligió este tema</span>
      </header>
      <ol className="approval-reasoning__list">
        {topSignals.map((c, i) => {
          const score = c.reactionsCount + c.commentsCount;
          const excerpt = c.excerpt.length > 96 ? c.excerpt.slice(0, 96) + "…" : c.excerpt;
          return (
            <li key={c.id} className="approval-reasoning__item">
              <span className="approval-reasoning__rank">{String(i + 1).padStart(2, "0")}</span>
              <div className="approval-reasoning__text">
                <p className="approval-reasoning__excerpt">“{excerpt}”</p>
                <span className="approval-reasoning__meta">
                  {c.authorName} · {c.communityName}
                </span>
              </div>
              <span className="approval-reasoning__score" title="Reacciones + comentarios">
                {score}
              </span>
            </li>
          );
        })}
      </ol>
      <p className="approval-reasoning__foot">
        Basado en {contributions.length}&nbsp;contribuciones de la semana
      </p>
    </section>
  );
}

/* ------------------------------------------------------------------ */
/* Vertical timeline                                                  */
/* ------------------------------------------------------------------ */

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
    <section className="approval-timeline" aria-label="Ciclo de publicación">
      <header className="approval-eyebrow">
        <span className="approval-eyebrow__rule" aria-hidden="true" />
        <span>Ciclo de publicación</span>
      </header>

      <ol className="approval-timeline__list">
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
            <li key={step.key} className={`approval-timeline__item is-${mod}`}>
              <span className="approval-timeline__rail" aria-hidden="true" />
              <span className="approval-timeline__dot">
                <step.Icon size={12} />
              </span>
              <div className="approval-timeline__text">
                <span className="approval-timeline__label">{step.label}</span>
                {date ? (
                  <span className="approval-timeline__date">{formatDateTime(date)}</span>
                ) : step.hint ? (
                  <span className="approval-timeline__hint">— {step.hint}</span>
                ) : (
                  <span className="approval-timeline__hint">— pendiente</span>
                )}
              </div>
            </li>
          );
        })}
      </ol>
    </section>
  );
}

/* ------------------------------------------------------------------ */
/* Detail view                                                        */
/* ------------------------------------------------------------------ */

function ApprovalDetail({ id }) {
  const navigate = useNavigate();
  const { data: draft, loading, error: loadError, setData: setDraft } = useFetch(() => draftsApi.getDraft(id), [id]);
  const { acting, error: actionError, run } = useAsyncAction();
  const [copied, setCopied] = useState(null);
  const [exportModal, setExportModal] = useState(null);
  const [regenStage, setRegenStage] = useState(null); // null | 'confirm' | 'loading'
  const [toast, setToast] = useState(null); // { type: 'success'|'error', message }
  const copiedTimerRef = useRef(null);
  const toastTimerRef = useRef(null);

  useEffect(() => {
    return () => {
      clearTimeout(copiedTimerRef.current);
      clearTimeout(toastTimerRef.current);
    };
  }, []);

  const showToast = (type, message) => {
    setToast({ type, message });
    clearTimeout(toastTimerRef.current);
    toastTimerRef.current = setTimeout(() => setToast(null), 4000);
  };

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
      const updated = await run(() => draftsApi.transitionDraft(id, next));
      setDraft(updated);
    } catch {
      /* error is already captured in actionError */
    }
  };

  const onOpenPublishModal = () => {
    if (!draft?.channels) return;
    const firstChannel = CHANNELS.find((c) => draft.channels[c]);
    if (firstChannel) setExportModal(firstChannel);
  };

  const onOpenExportModal = () => {
    if (!draft?.channels) return;
    const firstChannel = CHANNELS.find((c) => draft.channels[c]);
    if (firstChannel) setExportModal(firstChannel);
  };

  const onConfirmPublish = async () => {
    await onChangeStatus("PUBLISHED");
    setExportModal(null);
  };

  const onRegenerateWithAI = () => setRegenStage("confirm");

  const onConfirmRegenerate = async () => {
    setRegenStage("loading");
    try {
      await run(async () => {
        await regenerateDraftsApi(id);
        const refreshed = await draftsApi.getDraft(id);
        setDraft(refreshed);
        return refreshed;
      });
      setRegenStage(null);
      showToast("success", "Borradores regenerados con IA correctamente.");
    } catch {
      setRegenStage(null);
      showToast("error", "No se pudieron regenerar los borradores. Intentá nuevamente.");
    }
  };

  const onApproveAll = async () => {
    try {
      const updated = await run(() => draftsApi.approveAllDrafts(id));
      setDraft(updated);
    } catch {
      /* error captured */
    }
  };

  if (loading) {
    return (
      <div className="approval-list approval-list--loading">
        <span className="approval-spinner" aria-hidden="true" />
        <span>Cargando borrador…</span>
      </div>
    );
  }
  if (error && !draft) return <p className="approval-detail__error">{error}</p>;
  if (!draft) return <p className="approval-detail__error">Borrador no encontrado.</p>;

  const channelCount = CHANNELS.filter((c) => draft.channels?.[c]).length;
  const sources = draft.sourceContributions || [];

  return (
    <div className="approval-shell">
      <Link to="/approval" className="approval-back">
        <ArrowLeft size={14} aria-hidden="true" />
        <span>Volver al listado</span>
      </Link>

      <div className="approval-grid">
        {/* ============================ RAIL ============================ */}
        <aside className="approval-rail">
          <section className="approval-masthead">
            <div className="approval-masthead__kicker">
              <Hash size={11} aria-hidden="true" />
              <span>DIGEST №{fmtId(draft.id)}</span>
              <span className="approval-masthead__dot" aria-hidden="true" />
              <span>SEMANA {draft.weekOf}</span>
            </div>

            <h1 className="approval-masthead__title">{draft.topicTitle}</h1>

            <p className="approval-masthead__dek">{draft.topicSummary}</p>

            <div className="approval-masthead__dateline">
              <span className="approval-masthead__date">
                Creado&nbsp;
                <time>{formatDateTime(draft.createdAt)}</time>
              </span>
              <span className="approval-masthead__date">
                Actualizado&nbsp;
                <time>{formatDateTime(draft.updatedAt)}</time>
              </span>
              <span className={`approval-status status--${draft.status.toLowerCase()}`}>
                <span className="approval-status__dot" aria-hidden="true" />
                {STATUS_LABELS[draft.status] || draft.status}
              </span>
            </div>
          </section>

          <section className="approval-rail__actions" aria-label="Acciones masivas">
            <button
              type="button"
              className="approval-bulk-btn approval-bulk-btn--regenerate"
              onClick={onRegenerateWithAI}
              disabled={acting}
            >
              <RefreshCw size={13} aria-hidden="true" />
              {acting ? "Regenerando…" : "Regenerar con IA"}
            </button>
            <button
              type="button"
              className="approval-bulk-btn approval-bulk-btn--approve-all"
              onClick={onApproveAll}
              disabled={acting}
            >
              <CheckCheck size={13} aria-hidden="true" />
              Aprobar todos
            </button>
          </section>

          <DraftTimeline draft={draft} />

          <AIReasoningBlock draft={draft} />
        </aside>

        {/* ========================== WORK ========================== */}
        <main className="approval-work">
          <section className="approval-channels">
            <header className="approval-section-head">
              <h2 className="approval-section-title">
                <span className="approval-section-title__rule" aria-hidden="true" />
                <span>Versiones por canal</span>
                <span className="approval-section-title__count">{channelCount}</span>
              </h2>
              <p className="approval-section-sub">
                Cada canal se publica con su voz y formato propios. Editá libremente antes de aprobar.
              </p>
            </header>

            <div className="approval-channels__grid">
              {CHANNELS.map((c) => {
                const ch = draft.channels[c];
                if (!ch) return null;
                const isCopied = copied === c;
                return (
                  <article key={c} className={`approval-channel approval-channel--${c}`}>
                    <span className="approval-channel__tint" aria-hidden="true" />

                    <header className="approval-channel__head">
                      <span className="approval-channel__id">
                        <ChannelIcon channel={c} size={14} />
                        <span className="approval-channel__kicker">
                          {CHANNEL_KICKERS[c] || (CHANNEL_LABELS[c] || c).toUpperCase()}
                        </span>
                      </span>
                      <span className={`approval-status approval-status--xs status--${(ch.status || "").toLowerCase()}`}>
                        <span className="approval-status__dot" aria-hidden="true" />
                        {ch.status}
                      </span>
                    </header>

                    {ch.title && <h3 className="approval-channel__title">{ch.title}</h3>}

                    <pre className="approval-channel__body">{ch.body}</pre>

                    <footer className="approval-channel__actions">
                      <button
                        type="button"
                        className="ch-btn ch-btn--primary"
                        onClick={() => navigate(`/editor/${draft.id}/${c}`)}
                      >
                        <Pencil size={12} aria-hidden="true" />
                        <span>Editar</span>
                      </button>
                      <button
                        type="button"
                        className="ch-btn"
                        onClick={() => navigate(`/preview?draftId=${draft.id}&channel=${c}`)}
                      >
                        <Eye size={12} aria-hidden="true" />
                        <span>Preview</span>
                      </button>
                      <button
                        type="button"
                        className={`ch-btn${isCopied ? " is-success" : ""}`}
                        onClick={() => onCopyMarkdown(c)}
                        title="Copiar al portapapeles como Markdown"
                      >
                        {isCopied ? <CheckCircle2 size={12} aria-hidden="true" /> : <Copy size={12} aria-hidden="true" />}
                        <span>{isCopied ? "Copiado" : "Copiar MD"}</span>
                      </button>
                      <button
                        type="button"
                        className="ch-btn ch-btn--icon"
                        onClick={() => draftExport.downloadJson(draft, c)}
                        title="Descargar como JSON estructurado"
                        aria-label="Descargar JSON"
                      >
                        <FileJson size={12} aria-hidden="true" />
                      </button>
                      <button
                        type="button"
                        className="ch-btn ch-btn--icon"
                        onClick={() => draftExport.downloadMarkdown(draft, c)}
                        title="Descargar como Markdown"
                        aria-label="Descargar Markdown"
                      >
                        <FileText size={12} aria-hidden="true" />
                      </button>
                    </footer>
                  </article>
                );
              })}
            </div>
          </section>

          <section className="approval-transitions" aria-label="Transiciones de estado">
            <header className="approval-section-head">
              <h2 className="approval-section-title">
                <span className="approval-section-title__rule" aria-hidden="true" />
                <span>Próximo movimiento</span>
              </h2>
              <p className="approval-section-sub">
                Definí el siguiente paso del borrador en el flujo editorial.
              </p>
            </header>

            {error && (
              <p className="approval-detail__error">
                <AlertCircle size={14} aria-hidden="true" /> {error}
              </p>
            )}

            <div className="approval-transitions__row">
              <ApprovalButtons
                status={draft.status}
                onChangeStatus={onChangeStatus}
                onPublish={onOpenPublishModal}
                onExport={onOpenExportModal}
                disabled={acting}
              />
            </div>
          </section>

          <section className="approval-sources" aria-label="Fuentes">
            <header className="approval-section-head">
              <h2 className="approval-section-title">
                <span className="approval-section-title__rule" aria-hidden="true" />
                <span>Fuentes</span>
                <span className="approval-section-title__count">{sources.length}</span>
              </h2>
              <p className="approval-section-sub">
                Las contribuciones de la semana que dieron origen a este tema.
              </p>
            </header>

            {sources.length === 0 ? (
              <p className="approval-sources__empty">Sin contribuciones registradas.</p>
            ) : (
              <ul className="approval-sources__list">
                {sources.map((c) => (
                  <li key={c.id} className="approval-source">
                    <header className="approval-source__head">
                      <span className={`approval-source__type type--${c.type.toLowerCase()}`}>
                        {c.type}
                      </span>
                      <span className="approval-source__community">{c.communityName}</span>
                    </header>
                    <p className="approval-source__excerpt">“{c.excerpt}”</p>
                    <footer className="approval-source__foot">
                      <span className="approval-source__author">{c.authorName}</span>
                      <span className="approval-source__stat" title="Reacciones">❤ {c.reactionsCount}</span>
                      <span className="approval-source__stat" title="Comentarios">💬 {c.commentsCount}</span>
                      {c.sourceUrl && (
                        <a href={c.sourceUrl} target="_blank" rel="noreferrer" className="approval-source__link">
                          Original <ExternalLink size={11} aria-hidden="true" />
                        </a>
                      )}
                    </footer>
                  </li>
                ))}
              </ul>
            )}
          </section>
        </main>
      </div>

      {exportModal && (
        <ExportModal
          draft={draft}
          channel={exportModal}
          onClose={() => setExportModal(null)}
          onConfirmPublish={onConfirmPublish}
        />
      )}

      {regenStage && (
        <div className="regen-modal__overlay" role="dialog" aria-modal="true">
          <div className="regen-modal">
            {regenStage === "confirm" ? (
              <Fragment>
                <header className="regen-modal__header">
                  <span className="regen-modal__icon" aria-hidden="true">
                    <Sparkles size={16} />
                  </span>
                  <div className="regen-modal__heading">
                    <span className="regen-modal__kicker">ACCIÓN GENERATIVA</span>
                    <h3>Regenerar borradores con IA</h3>
                  </div>
                  <button
                    type="button"
                    className="regen-modal__close"
                    onClick={() => setRegenStage(null)}
                    aria-label="Cerrar"
                  >
                    <X size={14} />
                  </button>
                </header>
                <p className="regen-modal__body">
                  Se reemplazará el contenido actual de los tres canales (Newsletter, LinkedIn y Twitter)
                  con nuevas versiones generadas por la IA. Esta acción no se puede deshacer.
                </p>
                <footer className="regen-modal__actions">
                  <button
                    type="button"
                    className="regen-modal__btn regen-modal__btn--ghost"
                    onClick={() => setRegenStage(null)}
                  >
                    Cancelar
                  </button>
                  <button
                    type="button"
                    className="regen-modal__btn regen-modal__btn--primary"
                    onClick={onConfirmRegenerate}
                  >
                    <RefreshCw size={13} aria-hidden="true" /> Regenerar
                  </button>
                </footer>
              </Fragment>
            ) : (
              <div className="regen-modal__loading">
                <span className="regen-modal__pulse" aria-hidden="true">
                  <span className="regen-modal__pulse-dot" />
                </span>
                <span className="regen-modal__kicker regen-modal__kicker--center">EN&nbsp;CURSO</span>
                <h3>Regenerando borradores con IA</h3>
                <p>Esto puede tardar unos segundos. No cierres esta ventana.</p>
              </div>
            )}
          </div>
        </div>
      )}

      {toast && (
        <div
          className={`approval-toast approval-toast--${toast.type}`}
          role="status"
          aria-live="polite"
        >
          <span className="approval-toast__icon" aria-hidden="true">
            {toast.type === "success" ? (
              <CheckCircle2 size={16} />
            ) : (
              <AlertCircle size={16} />
            )}
          </span>
          <span className="approval-toast__msg">{toast.message}</span>
        </div>
      )}
    </div>
  );
}

/* ------------------------------------------------------------------ */
/* Page shell                                                         */
/* ------------------------------------------------------------------ */

export default function ApprovalPage() {
  const { id } = useParams();

  return (
    <div className="approval-page">
      <header className="approval-header">
        <div className="approval-header__kicker">
          <span className="approval-header__rule" aria-hidden="true" />
          <span>Panel de aprobación</span>
        </div>
        <div className="approval-header__row">
          <h1 className="approval-header__title">Proceso de aprobación</h1>
          <p className="approval-header__sub">
            {id
              ? "Revisá, regenerá con IA y aprobá el contenido antes de su publicación."
              : "Borradores generados por la IA, listos para tu revisión y aprobación."}
          </p>
        </div>
      </header>

      {id ? <ApprovalDetail id={id} /> : <ApprovalList />}
    </div>
  );
}
