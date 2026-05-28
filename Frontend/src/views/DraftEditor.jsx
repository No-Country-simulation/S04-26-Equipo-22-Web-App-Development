import { useParams, Link, useNavigate } from "react-router-dom";
import { useState, useRef, useCallback, useEffect } from "react";
import DOMPurify from "dompurify";
import * as draftsApi from "../api/drafts";
import { CHANNEL_LABELS } from "../data/draftSelectors";
import "./DraftEditor.css";

const TOOLBAR_BUTTONS = [
  { command: "bold", label: <b>B</b>, title: "Negrita (Ctrl+B)" },
  { command: "italic", label: <i>I</i>, title: "Cursiva (Ctrl+I)" },
  { command: "underline", label: <u>U</u>, title: "Subrayado (Ctrl+U)" },
  { type: "separator" },
  { command: "insertOrderedList", label: "1.", title: "Lista numerada" },
  { command: "insertUnorderedList", label: "•≡", title: "Lista con viñetas" },
  { type: "separator" },
  { command: "formatBlock", label: "❝", title: "Cita", value: "blockquote" },
  { command: "createLink", label: "🔗", title: "Insertar enlace" },
];

export function DraftEditor() {
  const { draftId, channel } = useParams();
  const navigate = useNavigate();
  const editorRef = useRef(null);
  const savedTimerRef = useRef(null);
  const mountedRef = useRef(true);

  const [draft, setDraft] = useState(null);
  const [title, setTitle] = useState("");
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [saved, setSaved] = useState(false);
  const [error, setError] = useState(null);
  const [activeFormats, setActiveFormats] = useState(new Set());
  const [charCount, setCharCount] = useState(0);

  const TWITTER_LIMIT = 280;
  const isTwitter = channel === "twitter";
  const overLimit = isTwitter && charCount > TWITTER_LIMIT;

  useEffect(() => {
    return () => clearTimeout(savedTimerRef.current);
  }, []);

  useEffect(() => {
    return () => { mountedRef.current = false; };
  }, []);

  const recountChars = useCallback(() => {
    if (!editorRef.current) return;
    setCharCount((editorRef.current.textContent || "").length);
  }, []);

  useEffect(() => {
    let cancelled = false;
    // eslint-disable-next-line react-hooks/set-state-in-effect
    setLoading(true);
    setError(null);
    draftsApi
      .getDraft(draftId)
      .then((d) => {
        if (cancelled) return;
        setDraft(d);
        const ch = d.channels[channel];
        if (!ch) {
          setError(`El canal "${channel}" no existe en este borrador.`);
          return;
        }
        setTitle(ch.title || "");
        if (editorRef.current) {
          editorRef.current.innerHTML = DOMPurify.sanitize(ch.body || "");
          setCharCount((editorRef.current.textContent || "").length);
        }
      })
      .catch((err) => {
        if (!cancelled) setError(err?.message || "No se pudo cargar el borrador");
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [draftId, channel]);

  const updateActiveFormats = useCallback(() => {
    const formats = new Set();
    if (document.queryCommandState("bold")) formats.add("bold");
    if (document.queryCommandState("italic")) formats.add("italic");
    if (document.queryCommandState("underline")) formats.add("underline");
    if (document.queryCommandState("insertOrderedList")) formats.add("insertOrderedList");
    if (document.queryCommandState("insertUnorderedList")) formats.add("insertUnorderedList");
    setActiveFormats(formats);
  }, []);

  const execFormat = useCallback(
    (command, value) => {
      if (command === "createLink") {
        const url = prompt("Ingresá la URL:");
        if (!url) return;
        document.execCommand("createLink", false, url);
      } else {
        document.execCommand(command, false, value || null);
      }
      editorRef.current?.focus();
      updateActiveFormats();
    },
    [updateActiveFormats]
  );

  const handleSave = async ({ submitForReview = false } = {}) => {
    if (!draft) return;
    const body = editorRef.current?.innerHTML || "";
    setSaving(true);
    setError(null);
    try {
      const payload = channel === "twitter" ? { body } : { title, body };
      const updated = await draftsApi.updateChannelDraft(draftId, channel, payload);
      if (!mountedRef.current) return;
      setDraft(updated);
      setSaved(true);
      clearTimeout(savedTimerRef.current);
      savedTimerRef.current = setTimeout(() => {
        if (mountedRef.current) setSaved(false);
      }, 2500);

      if (submitForReview && updated.status === "GENERATED") {
        await draftsApi.transitionDraft(draftId, "IN_REVIEW");
      }
    } catch (err) {
      if (mountedRef.current) setError(err?.message || "No se pudo guardar");
    } finally {
      if (mountedRef.current) setSaving(false);
    }
  };

  if (loading) return <div className="editor-page">Cargando…</div>;
  if (error && !draft) return <div className="editor-page">{error}</div>;
  if (!draft) return <h1>Borrador no encontrado</h1>;

  const channelLabel = CHANNEL_LABELS[channel] || channel;
  const channelDraft = draft.channels[channel];

  return (
    <div className="editor-page">
      <Link to="/drafts" className="editor-back">← Volver a borradores</Link>

      <div className="editor-panel">
        <div className="editor-meta-row">
          <div>
            <label className="editor-field-label">Tema semanal</label>
            <p className="editor-topic-hint">{draft.topicTitle}</p>
          </div>
          <div>
            <label className="editor-field-label">Canal</label>
            <span className="editor-status-badge">{channelLabel}</span>
          </div>
          <div>
            <label className="editor-field-label">Estado canal</label>
            <span className="editor-status-badge">{channelDraft?.status}</span>
          </div>
        </div>

        {channel !== "twitter" && (
          <>
            <label className="editor-field-label">Título</label>
            <input
              className="editor-title-input"
              type="text"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
            />
          </>
        )}

        <label className="editor-field-label">Contenido</label>

        <div className="editor-toolbar">
          {TOOLBAR_BUTTONS.map((btn, i) =>
            btn.type === "separator" ? (
              <div key={i} className="editor-toolbar-separator" />
            ) : (
              <button
                key={btn.command}
                className={`editor-toolbar-btn${activeFormats.has(btn.command) ? " active" : ""}`}
                title={btn.title}
                onMouseDown={(e) => {
                  e.preventDefault();
                  execFormat(btn.command, btn.value);
                }}
              >
                {btn.label}
              </button>
            )
          )}
        </div>

        <div
          ref={editorRef}
          className="editor-content-area editor-content-rich"
          contentEditable={draft.channels[channel]?.rawStatus !== "PUBLISHED"}
          suppressContentEditableWarning
          onKeyUp={() => { updateActiveFormats(); recountChars(); }}
          onMouseUp={updateActiveFormats}
          onSelect={updateActiveFormats}
          onInput={recountChars}
        />

        {isTwitter && (
          <p className={`editor-char-counter${overLimit ? " over" : ""}`}>
            {charCount} / {TWITTER_LIMIT} caracteres
            {overLimit && " — supera el límite de X"}
          </p>
        )}

        {error && <p className="editor-error">{error}</p>}

        <div className="editor-actions">
          {draft.channels[channel]?.rawStatus === "PUBLISHED" ? (
            <p className="editor-published-msg">Este borrador ya fue publicado y no se puede editar.</p>
          ) : (
            <>
              <button className="editor-btn-save" onClick={() => handleSave()} disabled={saving}>
                {saving ? "Guardando…" : "Guardar cambios"}
              </button>
              {draft.status === "GENERATED" && (
                <button
                  className="editor-btn-publish"
                  onClick={() => handleSave({ submitForReview: true })}
                  disabled={saving}
                >
                  Guardar y enviar a revisión
                </button>
              )}
            </>
          )}
          <button
            type="button"
            onClick={() => navigate(`/approval/${draftId}`)}
            className="editor-btn-approval"
          >
            Ir a aprobación →
          </button>
        </div>

        {saved && <p className="editor-saved-msg">✓ Cambios guardados</p>}
      </div>
    </div>
  );
}
