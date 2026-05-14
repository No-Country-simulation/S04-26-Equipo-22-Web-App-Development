import { useState } from "react";
import "./ChannelPreview.css";
import { useNavigate } from "react-router-dom";
import { channelDraftsMock } from "../data/channelDraftsMock";
import { channelDraftAdapter } from "../adapters/channelDraftAdapter";

function ChannelPreview() {
  const navigate = useNavigate();
  const adaptedData = channelDraftsMock.map(channelDraftAdapter);
  const [activeTab, setActiveTab] = useState("All");
  const [drafts, setDrafts] = useState(adaptedData);

  const handleApprove = (id) => {
    alert(`✅ Borrador ${id} aprobado`);
  };

  const handleReject = (id) => {
    alert(`❌ Borrador ${id} rechazado`);
  };

  const handleEdit = (id) => {
    alert(`✏️ Editar borrador ${id}`);
  };

  const filteredDrafts = drafts.filter(
    (draft) => activeTab === "All" || draft.channel === activeTab
  );

  return (
    <div className="channel-preview">
      <header className="preview-header">
        <button className="back-button" onClick={() => navigate(-1)}>
          ← Volver
        </button>
        <h1 className="preview-title">Borradores generados por IA</h1>
      </header>

      <nav className="preview-tabs">
        <button
          className={activeTab === "All" ? "active-tab" : ""}
          onClick={() => setActiveTab("All")}
        >
          Todos
        </button>
        <button
          className={activeTab === "Newsletter" ? "active-tab" : ""}
          onClick={() => setActiveTab("Newsletter")}
        >
          Newsletter
        </button>
        <button
          className={activeTab === "LinkedIn" ? "active-tab" : ""}
          onClick={() => setActiveTab("LinkedIn")}
        >
          LinkedIn
        </button>
        <button
          className={activeTab === "Twitter" ? "active-tab" : ""}
          onClick={() => setActiveTab("Twitter")}
        >
          Twitter
        </button>
      </nav>

      <section className="drafts-table-container">
        <table className="drafts-table">
          <thead>
            <tr>
              <th>Canal</th>
              <th>Contenido</th>
              <th>Estado</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            {filteredDrafts.map((draft) => (
              <tr key={draft.id}>
                <td>
                  <span className={`channel-badge ${draft.channel.toLowerCase()}`}>
                    {draft.channel}
                  </span>
                </td>
                <td className="content-cell">
                  {draft.content.substring(0, 120)}...
                </td>
                <td>
                  <span className={`status-badge ${draft.status.toLowerCase()}`}>
                    {draft.status}
                  </span>
                </td>
                <td className="actions-cell">
                  <button className="btn-edit" onClick={() => handleEdit(draft.id)}>
                    ✏️ Editar
                  </button>
                  <button className="btn-approve" onClick={() => handleApprove(draft.id)}>
                    ✅ Aprobar
                  </button>
                  <button className="btn-reject" onClick={() => handleReject(draft.id)}>
                    ❌ Rechazar
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>
    </div>
  );
}

export default ChannelPreview;