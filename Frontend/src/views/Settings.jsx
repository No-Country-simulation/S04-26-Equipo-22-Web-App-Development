import { useState } from "react";
import { useNavigate } from "react-router-dom";
import * as communitiesApi from "../api/communities";
import { useFetch } from "../hooks/useFetch";
import "./Settings.css";

function Settings() {
  const navigate = useNavigate();

  const [communities, setCommunities] = useState([]);
  const [newCommunity, setNewCommunity] = useState("");

  const [channels] = useState({
    newsletter: { connected: true, email: "hola@talentcircle.es" },
    linkedin: { connected: true, account: "TalentCircle ES" },
    twitter: { connected: true, account: "@talentcircle_es" },
    feed: { connected: true, api: "api nativa" }
  });

  const [aiModel, setAiModel] = useState("gemini-2.5-flash-lite");
  const [schedule, setSchedule] = useState("Friday 18:00");
  const [team] = useState([
    { name: "Rider", role: "PM" },
    { name: "Anthony", role: "Backend" },
    { name: "Alejandro", role: "Frontend" }
  ]);

  useEffect(() => {
    communitiesApi.listCommunities({ onlyActive: true })
      .then(data => setCommunities(data))
      .catch(() => setCommunities([]));
  }, []);

  const addCommunity = async () => {
    if (!newCommunity.trim()) return;
    try {
      const created = await communitiesApi.createCommunity({ name: newCommunity, platform: "general", active: true });
      setCommunities([...communities, created]);
      setNewCommunity("");
    } catch (e) {
      console.error("Error al crear comunidad", e);
    }
  };

  const removeCommunity = async (id) => {
    try {
      await communitiesApi.deactivateCommunity(id);
      setCommunities(communities.filter(c => c.id !== id));
    } catch (e) {
      console.error("Error al eliminar comunidad", e);
    }
  };

  const handleSave = () => {
    alert("✅ Configuración guardada correctamente");
  };

  return (
    <div className="settings-container">
      <header className="settings-header">
        <button className="back-button" onClick={() => navigate(-1)}>
          ← Volver
        </button>
        <h1 className="settings-title">Configuración</h1>
        <p className="settings-subtitle">Comunidades vigiladas, canales de salida, modelo, equipo y schedule</p>
      </header>

      <div className="settings-sections">
        <section className="settings-section">
          <h2>🌐 Comunidades vigiladas</h2>
          <div className="community-list">
            {communities.map((community) => (
              <div key={community.id} className="community-item">
                <span>{community.name}</span>
                <button className="remove-btn" onClick={() => removeCommunity(community.id)}>✖</button>
              </div>
            ))}
          </div>
          <div className="add-community">
            <input type="text" placeholder="Nueva comunidad..." value={newCommunity} onChange={(e) => setNewCommunity(e.target.value)} />
            <button onClick={addCommunity}>+ Agregar</button>
          </div>
        </section>

        <section className="settings-section">
          <h2>📢 Canales de salida</h2>
          <p className="section-desc">Conecta cuentas para que el editor pueda publicar con un clic.</p>
          <div className="channels-list">
            <div className="channel-item">
              <div className="channel-info">
                <span className="channel-icon">📧</span>
                <span><strong>Newsletter (Mailchimp)</strong><br />{channels.newsletter.email}</span>
              </div>
              <button className="connect-btn connected">✓ Conectado</button>
            </div>
            <div className="channel-item">
              <div className="channel-info">
                <span className="channel-icon">💼</span>
                <span><strong>LinkedIn — página</strong><br />{channels.linkedin.account}</span>
              </div>
              <button className="connect-btn connected">✓ Conectado</button>
            </div>
            <div className="channel-item">
              <div className="channel-info">
                <span className="channel-icon">🐦</span>
                <span><strong>Twitter / X</strong><br />{channels.twitter.account}</span>
              </div>
              <button className="connect-btn connected">✓ Conectado</button>
            </div>
            <div className="channel-item">
              <div className="channel-info">
                <span className="channel-icon">📡</span>
                <span><strong>Feed interno</strong><br />{channels.feed.api}</span>
              </div>
              <button className="connect-btn connected">✓ Conectado</button>
            </div>
            <div className="channel-item">
              <div className="channel-info">
                <span className="channel-icon">💬</span>
                <span><strong>Slack/Discord</strong></span>
              </div>
              <button className="connect-btn">Configurar</button>
            </div>
          </div>
        </section>

        <section className="settings-section">
          <h2>🤖 Modelo de IA</h2>
          <select value={aiModel} onChange={(e) => setAiModel(e.target.value)}>
            <option value="gemini-2.5-flash-lite">Gemini 2.5 Flash-Lite</option>
            <option value="gpt-4">GPT-4</option>
            <option value="claude-3">Claude 3</option>
          </select>
        </section>

        <section className="settings-section">
          <h2>👥 Equipo</h2>
          <div className="team-list">
            {team.map((member, index) => (
              <div key={index} className="team-member">
                <span><strong>{member.name}</strong> - {member.role}</span>
              </div>
            ))}
          </div>
        </section>

        <section className="settings-section">
          <h2>⏰ Schedule</h2>
          <select value={schedule} onChange={(e) => setSchedule(e.target.value)}>
            <option value="Friday 18:00">Viernes 18:00 (recomendado)</option>
            <option value="Friday 20:00">Viernes 20:00</option>
            <option value="Saturday 09:00">Sábado 09:00</option>
          </select>
        </section>
      </div>

      <div className="settings-actions">
        <button className="save-btn" onClick={handleSave}>💾 Guardar configuración</button>
      </div>
    </div>
  );
}

export default Settings;