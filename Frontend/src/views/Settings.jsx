import { useState } from "react";
import { useNavigate } from "react-router-dom";
import * as communitiesApi from "../api/communities";
import * as authApi from "../api/auth";
import { useAuth } from "../context/AuthContext";
import { useFetch } from "../hooks/useFetch";
import "./Settings.css";

function Settings() {
  const navigate = useNavigate();
  const { logout } = useAuth();

  const { data: communities = [], setData: setCommunities } = useFetch(
    () => communitiesApi.listCommunities({ onlyActive: true })
  );

  const [newCommunity, setNewCommunity] = useState("");
  const [communityMsg, setCommunityMsg] = useState(null);
  const [pwd, setPwd] = useState({ currentPassword: "", newPassword: "", confirm: "" });
  const [pwdMsg, setPwdMsg] = useState(null);
  const [pwdLoading, setPwdLoading] = useState(false);
  const [deleting, setDeleting] = useState(false);

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

  const addCommunity = async () => {
    if (!newCommunity.trim()) return;
    setCommunityMsg(null);
    try {
      const created = await communitiesApi.createCommunity({ name: newCommunity, platform: "general", active: true });
      setCommunities([...communities, created]);
      setNewCommunity("");
      setCommunityMsg({ type: "success", text: "Comunidad agregada" });
    } catch (err) {
      const msg = err?.response?.data?.message || "No se pudo agregar la comunidad";
      setCommunityMsg({ type: "error", text: msg });
    }
  };

  const removeCommunity = async (id) => {
    setCommunityMsg(null);
    try {
      await communitiesApi.deactivateCommunity(id);
      setCommunities(communities.filter(c => c.id !== id));
      setCommunityMsg({ type: "success", text: "Comunidad eliminada" });
    } catch (err) {
      const msg = err?.response?.data?.message || "No se pudo eliminar la comunidad";
      setCommunityMsg({ type: "error", text: msg });
    }
  };

  const handleSave = () => {
    alert("✅ Configuración guardada correctamente");
  };

  const handleChangePassword = async (e) => {
    e.preventDefault();
    setPwdMsg(null);
    if (pwd.newPassword !== pwd.confirm) {
      setPwdMsg({ type: "error", text: "Las contraseñas nuevas no coinciden" });
      return;
    }
    setPwdLoading(true);
    try {
      await authApi.updatePassword({
        currentPassword: pwd.currentPassword,
        newPassword: pwd.newPassword,
      });
      setPwdMsg({ type: "success", text: "Contraseña actualizada. Volvé a iniciar sesión." });
      setPwd({ currentPassword: "", newPassword: "", confirm: "" });
      setTimeout(async () => {
        await logout();
        navigate("/login");
      }, 1500);
    } catch (err) {
      const msg = err?.response?.data?.message || "No se pudo cambiar la contraseña";
      setPwdMsg({ type: "error", text: msg });
    } finally {
      setPwdLoading(false);
    }
  };

  const handleDeleteAccount = async () => {
    if (!window.confirm("¿Eliminar tu cuenta permanentemente? Esta acción es irreversible.")) return;
    setDeleting(true);
    try {
      await authApi.deleteAccount();
      navigate("/");
    } catch (err) {
      alert(err?.response?.data?.message || "No se pudo eliminar la cuenta");
      setDeleting(false);
    }
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
          {communityMsg && (
            <p className={`pwd-msg pwd-msg--${communityMsg.type}`}>{communityMsg.text}</p>
          )}
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

      <section className="settings-section">
        <h2>🔒 Cambiar contraseña</h2>
        <form className="password-form" onSubmit={handleChangePassword}>
          <input
            type="password"
            placeholder="Contraseña actual"
            value={pwd.currentPassword}
            onChange={(e) => setPwd({ ...pwd, currentPassword: e.target.value })}
            required
          />
          <input
            type="password"
            placeholder="Nueva contraseña (min 8, mayúscula, minúscula, número)"
            value={pwd.newPassword}
            onChange={(e) => setPwd({ ...pwd, newPassword: e.target.value })}
            minLength={8}
            required
          />
          <input
            type="password"
            placeholder="Confirmar nueva contraseña"
            value={pwd.confirm}
            onChange={(e) => setPwd({ ...pwd, confirm: e.target.value })}
            required
          />
          {pwdMsg && (
            <p className={`pwd-msg pwd-msg--${pwdMsg.type}`}>{pwdMsg.text}</p>
          )}
          <button type="submit" disabled={pwdLoading} className="save-btn">
            {pwdLoading ? "Cambiando…" : "Cambiar contraseña"}
          </button>
        </form>
      </section>

      <section className="settings-section settings-danger">
        <h2>⚠️ Zona peligrosa</h2>
        <p className="section-desc">Eliminar tu cuenta es permanente. Perderás acceso a todos tus borradores.</p>
        <button
          type="button"
          className="danger-btn"
          onClick={handleDeleteAccount}
          disabled={deleting}
        >
          {deleting ? "Eliminando…" : "Eliminar mi cuenta"}
        </button>
      </section>

      <div className="settings-actions">
        <button className="save-btn" onClick={handleSave}>💾 Guardar configuración</button>
      </div>
    </div>
  );
}

export default Settings;