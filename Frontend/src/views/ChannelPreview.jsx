import { useMemo } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { useFetch } from "../hooks/useFetch";
import * as draftsApi from "../api/drafts";
import { CHANNEL_LABELS, CHANNELS, getChannelView } from "../data/draftSelectors";
import LinkedinPreview from "../components/channelPreviews/LinkedinPreview";
import TwitterPreview from "../components/channelPreviews/TwitterPreview";
import NewsletterPreview from "../components/channelPreviews/NewsletterPreview";
import ChannelIcon from "../components/ChannelIcon";
import "./ChannelPreview.css";

function renderPreview(view) {
  switch (view.channel) {
    case "linkedin":
      return <LinkedinPreview data={{ ...view, channel: "LinkedIn" }} />;
    case "twitter":
      return <TwitterPreview data={{ ...view, channel: "X" }} />;
    case "newsletter":
      return <NewsletterPreview data={{ ...view, channel: "Newsletter" }} />;
    default:
      return null;
  }
}

function ChannelPreview() {
  const navigate = useNavigate();
  const [params, setParams] = useSearchParams();
  const draftId = params.get("draftId");
  const channelParam = params.get("channel");

  const { data: drafts = [], loading, error } = useFetch(() => draftsApi.listDrafts());

  const selectedDraft = useMemo(
    () => drafts.find((d) => d.id === draftId) || drafts[0],
    [drafts, draftId]
  );

  const channels = channelParam ? [channelParam] : CHANNELS;

  if (loading) return <div className="preview-page">Cargando…</div>;
  if (error) return <div className="preview-page">{error}</div>;
  if (!selectedDraft) return <div className="preview-page">No hay borradores disponibles.</div>;

  const views = channels
    .map((c) => getChannelView(selectedDraft, c))
    .filter(Boolean);

  const setChannel = (c) => {
    const next = new URLSearchParams(params);
    next.set("draftId", selectedDraft.id);
    if (c === "all") next.delete("channel");
    else next.set("channel", c);
    setParams(next);
  };

  return (
    <div className="preview-page">
      <header className="preview-header">
        <button className="back-button" onClick={() => navigate(-1)}>← Volver</button>
        <h1 className="preview-title">Vista previa: {selectedDraft.topicTitle}</h1>
      </header>

      {drafts.length > 1 && (
        <div className="preview-draft-selector">
          <label>Borrador:&nbsp;</label>
          <select
            value={selectedDraft.id}
            onChange={(e) => {
              const next = new URLSearchParams(params);
              next.set("draftId", e.target.value);
              setParams(next);
            }}
          >
            {drafts.map((d) => (
              <option key={d.id} value={d.id}>
                {d.topicTitle} — {d.weekOf}
              </option>
            ))}
          </select>
        </div>
      )}

      <nav className="preview-tabs">
        <button className={!channelParam ? "active-tab" : ""} onClick={() => setChannel("all")}>
          Todos
        </button>
        {CHANNELS.map((c) => (
          <button
            key={c}
            className={channelParam === c ? "active-tab" : ""}
            onClick={() => setChannel(c)}
          >
            {CHANNEL_LABELS[c]}
          </button>
        ))}
      </nav>

      <section className="preview-grid">
        {views.map((view) => (
          <div className="preview-card" key={view.id}>
            <h3 className="preview-card-title">
              <ChannelIcon channel={view.channel} size={18} />
              <span>{CHANNEL_LABELS[view.channel]}</span>
            </h3>
            {renderPreview(view)}
          </div>
        ))}
      </section>
    </div>
  );
}

export default ChannelPreview;
