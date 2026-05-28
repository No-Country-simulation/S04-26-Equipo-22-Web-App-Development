import { useEffect, useRef, useState } from "react";
import { X, Copy, Check, FileText, FileJson, FileType } from "lucide-react";
import { CHANNEL_LABELS, CHANNELS } from "../../data/draftSelectors";
import { getChannelView } from "../../data/draftSelectors";
import ChannelIcon from "../ChannelIcon";
import LinkedinPreview from "../channelPreviews/LinkedinPreview";
import TwitterPreview from "../channelPreviews/TwitterPreview";
import NewsletterPreview from "../channelPreviews/NewsletterPreview";
import * as draftExport from "../../api/draftExport";
import "./ExportModal.css";

const PREVIEW_COMPONENT = {
  linkedin: LinkedinPreview,
  twitter: TwitterPreview,
  newsletter: NewsletterPreview,
};

function ExportModal({ draft, channel: initialChannel, onClose, onConfirmPublish }) {
  const [activeChannel, setActiveChannel] = useState(initialChannel);
  const [copied, setCopied] = useState(false);
  const [copyError, setCopyError] = useState(null);
  const [publishing, setPublishing] = useState(false);
  const copiedTimerRef = useRef(null);
  const errorTimerRef = useRef(null);

  useEffect(() => () => {
    clearTimeout(copiedTimerRef.current);
    clearTimeout(errorTimerRef.current);
  }, []);

  const availableChannels = CHANNELS.filter((c) => draft.channels[c]);
  const ch = draft.channels[activeChannel];
  if (!ch) return null;

  const previewData = getChannelView(draft, activeChannel);
  const PreviewComp = PREVIEW_COMPONENT[activeChannel];
  const isAlreadyPublished = draft.status === "PUBLISHED";

  const handleCopy = async () => {
    setCopyError(null);
    try {
      await draftExport.copyPlainText(draft, activeChannel);
      setCopied(true);
      clearTimeout(copiedTimerRef.current);
      copiedTimerRef.current = setTimeout(() => setCopied(false), 2000);
    } catch {
      setCopyError("No se pudo copiar al portapapeles");
      clearTimeout(errorTimerRef.current);
      errorTimerRef.current = setTimeout(() => setCopyError(null), 3000);
    }
  };

  const handlePublish = async () => {
    setPublishing(true);
    try {
      await onConfirmPublish();
    } finally {
      setPublishing(false);
    }
  };

  return (
    <div className="export-overlay" onClick={onClose}>
      <div className="export-modal" onClick={(e) => e.stopPropagation()}>
        <header className="export-modal__header">
          <div className="export-modal__title">
            <ChannelIcon channel={activeChannel} size={20} />
            <h2>{isAlreadyPublished ? "Exportar" : "Publicar y exportar"}</h2>
          </div>
          <button className="export-modal__close" onClick={onClose} aria-label="Cerrar">
            <X size={18} />
          </button>
        </header>

        {availableChannels.length > 1 && (
          <nav className="export-modal__tabs">
            {availableChannels.map((c) => (
              <button
                key={c}
                className={`export-modal__tab${c === activeChannel ? " export-modal__tab--active" : ""}`}
                onClick={() => { setActiveChannel(c); setCopied(false); }}
              >
                <ChannelIcon channel={c} size={14} />
                {CHANNEL_LABELS[c]}
              </button>
            ))}
          </nav>
        )}

        <div className="export-modal__preview">
          <h3>Vista previa</h3>
          <div className="export-modal__preview-frame">
            {PreviewComp && previewData ? (
              <PreviewComp data={previewData} />
            ) : (
              <pre className="export-modal__raw">{ch.body}</pre>
            )}
          </div>
        </div>

        <div className="export-modal__actions">
          <h3>Descargar / Copiar</h3>
          <div className="export-modal__buttons">
            <button className="export-btn" onClick={handleCopy}>
              {copied ? <Check size={16} /> : <Copy size={16} />}
              {copied ? "Copiado" : "Copiar texto"}
            </button>
            <button className="export-btn" onClick={() => draftExport.downloadTxt(draft, activeChannel)}>
              <FileText size={16} />
              .txt
            </button>
            <button className="export-btn" onClick={() => draftExport.downloadMarkdown(draft, activeChannel)}>
              <FileType size={16} />
              .md
            </button>
            <button className="export-btn" onClick={() => draftExport.downloadJson(draft, activeChannel)}>
              <FileJson size={16} />
              .json
            </button>
          </div>
          {copyError && (
            <p className="export-modal__copy-error" role="alert">{copyError}</p>
          )}
        </div>

        <footer className="export-modal__footer">
          <button className="export-modal__cancel" onClick={onClose}>
            {isAlreadyPublished ? "Cerrar" : "Cancelar"}
          </button>
          {!isAlreadyPublished && (
            <button
              className="export-modal__publish"
              onClick={handlePublish}
              disabled={publishing}
            >
              {publishing ? "Publicando…" : "Confirmar publicación"}
            </button>
          )}
        </footer>
      </div>
    </div>
  );
}

export default ExportModal;
