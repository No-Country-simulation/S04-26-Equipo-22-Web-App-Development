import { useState } from "react";
import {
  Users,
  Plus,
  AlertCircle,
  Trash2,
  Power,
  Filter,
} from "lucide-react";
import * as communitiesApi from "../api/communities";
import { useAuth } from "../context/AuthContext";
import { useFetch } from "../hooks/useFetch";
import { useAsyncAction } from "../hooks/useAsyncAction";
import "./CommunitiesPage.css";

const INITIAL_FORM = { name: "", platform: "", active: true };

export default function CommunitiesPage() {
  const { user } = useAuth();
  const isAdmin = user?.role === "ADMIN";

  const [showOnlyActive, setShowOnlyActive] = useState(false);
  const [form, setForm] = useState(INITIAL_FORM);

  const { data: communities = [], loading, error, setError, setData: setCommunities, refetch } = useFetch(
    () => communitiesApi.listCommunities({ onlyActive: showOnlyActive }),
    [showOnlyActive]
  );

  const { acting: submitting, run } = useAsyncAction();

  const onCreate = async (e) => {
    e.preventDefault();
    if (!form.name.trim() || !form.platform.trim()) return;
    await run(async () => {
      await communitiesApi.createCommunity(form);
      setForm(INITIAL_FORM);
      await refetch();
    });
  };

  const onToggleActive = async (community) => {
    try {
      if (community.active) {
        await communitiesApi.deactivateCommunity(community.id);
      } else {
        await communitiesApi.activateCommunity(community.id);
      }
      await refetch();
    } catch (err) {
      setError(err?.response?.data?.message || "No se pudo actualizar la comunidad");
    }
  };

  const onDelete = async (community) => {
    if (!confirm(`¿Eliminar "${community.name}" definitivamente?`)) return;
    try {
      await communitiesApi.deleteCommunity(community.id);
      await refetch();
    } catch (err) {
      setError(err?.response?.data?.message || "No se pudo eliminar la comunidad");
    }
  };

  return (
    <div className="communities-page">
      <header className="communities-page__header">
        <div className="communities-page__heading">
          <h1>Comunidades</h1>
          <p className="communities-page__subtitle">
            Administra las comunidades que TalentCircle monitorea para generar contenido.
          </p>
        </div>
        <label className="communities-page__filter">
          <Filter aria-hidden="true" />
          <input
            type="checkbox"
            checked={showOnlyActive}
            onChange={(e) => setShowOnlyActive(e.target.checked)}
          />
          <span>Solo activas</span>
        </label>
      </header>

      {error && (
        <div className="communities-page__error">
          <AlertCircle aria-hidden="true" />
          <span>{error}</span>
        </div>
      )}

      {isAdmin && (
        <form className="communities-page__form" onSubmit={onCreate}>
          <div className="communities-page__form-head">
            <span className="communities-page__form-icon" aria-hidden="true">
              <Plus />
            </span>
            <div>
              <strong>Nueva comunidad</strong>
              <span>Añade una fuente para empezar a monitorearla.</span>
            </div>
          </div>

          <div className="communities-page__form-grid">
            <input
              type="text"
              placeholder="Nombre de la comunidad"
              value={form.name}
              onChange={(e) => setForm({ ...form, name: e.target.value })}
              required
            />
            <input
              type="text"
              placeholder="Plataforma (Discord, Slack…)"
              value={form.platform}
              onChange={(e) => setForm({ ...form, platform: e.target.value })}
              required
            />
            <label className="communities-page__checkbox">
              <input
                type="checkbox"
                checked={form.active}
                onChange={(e) => setForm({ ...form, active: e.target.checked })}
              />
              <span>Activa</span>
            </label>
            <button type="submit" disabled={submitting}>
              {submitting ? (
                <>
                  <span className="communities-page__spinner" />
                  Creando…
                </>
              ) : (
                <>
                  <Plus aria-hidden="true" />
                  Crear comunidad
                </>
              )}
            </button>
          </div>
        </form>
      )}

      {loading ? (
        <div className="communities-page__loading">
          <span className="communities-page__spinner communities-page__spinner--dark" />
          Cargando comunidades…
        </div>
      ) : communities.length === 0 ? (
        <div className="communities-page__empty">
          <div className="communities-page__empty-icon" aria-hidden="true">
            <Users />
          </div>
          <h3>Aún no hay comunidades</h3>
          <p>
            {isAdmin
              ? "Crea la primera comunidad usando el formulario de arriba."
              : "No hay comunidades disponibles por el momento."}
          </p>
        </div>
      ) : (
        <ul className="communities-page__list">
          {communities.map((c) => (
            <li key={c.id} className="communities-page__item">
              <div className="communities-page__info">
                <span className="communities-page__avatar" aria-hidden="true">
                  {c.name?.[0]?.toUpperCase() || "?"}
                </span>
                <div className="communities-page__meta">
                  <strong>{c.name}</strong>
                  <span className="communities-page__platform">{c.platform}</span>
                </div>
                <span
                  className={
                    "communities-page__status " +
                    (c.active ? "is-active" : "is-inactive")
                  }
                >
                  <span className="communities-page__status-dot" aria-hidden="true" />
                  {c.active ? "activa" : "inactiva"}
                </span>
              </div>
              {isAdmin && (
                <div className="communities-page__actions">
                  <button type="button" onClick={() => onToggleActive(c)}>
                    <Power aria-hidden="true" />
                    {c.active ? "Desactivar" : "Activar"}
                  </button>
                  <button
                    type="button"
                    className="communities-page__danger"
                    onClick={() => onDelete(c)}
                  >
                    <Trash2 aria-hidden="true" />
                    Eliminar
                  </button>
                </div>
              )}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
