import { useEffect, useMemo, useState } from "react";
import { AlertCircle } from "lucide-react";
import { DraftFilters } from "../components/drafts/DraftFilters";
import { DraftList } from "../components/drafts/DraftList";
import { flattenDraftsForList, countByChannel } from "../data/draftSelectors";
import * as draftsApi from "../api/drafts";
import "./Drafts.css";

export function Drafts() {
  const [filter, setFilter] = useState("all");
  const [drafts, setDrafts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    draftsApi
      .listDrafts()
      .then((data) => {
        if (!cancelled) setDrafts(data);
      })
      .catch((err) => {
        if (!cancelled) setError(err?.message || "No se pudieron cargar los borradores");
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, []);

  const rows = useMemo(() => flattenDraftsForList(drafts), [drafts]);
  const counts = useMemo(() => countByChannel(drafts), [drafts]);

  const filtered = filter === "all" ? rows : rows.filter((r) => r.channel === filter);

  return (
    <div className="drafts-view">
      <header className="drafts-view__header">
        <h1 className="borrador">Borradores</h1>
        <p className="caption">
          Revisa y edita los borradores generados por IA cada viernes.
        </p>
      </header>

      <DraftFilters filter={filter} setFilter={setFilter} counts={counts} />

      {error && (
        <p className="drafts-view__error">
          <AlertCircle aria-hidden="true" />
          {error}
        </p>
      )}

      {loading ? (
        <div className="drafts-view__loading">
          <span className="drafts-view__spinner" />
          Cargando borradores…
        </div>
      ) : (
        <DraftList rows={filtered} />
      )}
    </div>
  );
}
