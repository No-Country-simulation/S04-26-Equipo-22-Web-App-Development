import { useState, useMemo } from "react";
import { useNavigate } from "react-router-dom";
import { Download, AlertCircle, Inbox } from "lucide-react";
import { CHANNEL_LABELS, CHANNELS } from "../data/draftSelectors";
import * as draftsApi from "../api/drafts";
import { useFetch } from "../hooks/useFetch";
import { formatDateShort } from "../utils/formatDate";
import "./HistoryPage.css";

const STATUS_MAP = {
  PUBLISHED: "publicado",
  APPROVED: "aprobado",
  REJECTED: "rechazado",
  IN_REVIEW: "en revisión",
  GENERATED: "generado",
};

const PERIOD_OPTIONS = [
  { value: "30", label: "últimos 30 días" },
  { value: "90", label: "últimos 90 días" },
  { value: "all", label: "todo" },
];

function flattenDraftsToHistory(drafts) {
  const rows = [];
  for (const draft of drafts) {
    for (const channel of CHANNELS) {
      const ch = draft.channels[channel];
      if (!ch) continue;
      rows.push({
        id: draft.id,
        date: draft.weekOf || draft.createdAt || "",
        channel,
        title: ch.title || draft.topicTitle || "Sin título",
        status: STATUS_MAP[draft.status] || draft.status,
        rawStatus: draft.status,
      });
    }
  }
  return rows;
}

function exportCSV(rows) {
  const header = "Fecha,Canal,Título,Estado";
  const lines = rows.map(
    (r) =>
      `${r.date},"${CHANNEL_LABELS[r.channel]}","${r.title}",${r.status}`
  );
  const csv = [header, ...lines].join("\n");
  const blob = new Blob([csv], { type: "text/csv;charset=utf-8;" });
  const url = URL.createObjectURL(blob);
  const a = document.createElement("a");
  a.href = url;
  a.download = "historial.csv";
  a.click();
  URL.revokeObjectURL(url);
}

export default function HistoryPage() {
  const navigate = useNavigate();
  const [channelFilter, setChannelFilter] = useState("all");
  const [periodFilter, setPeriodFilter] = useState("all");

  const { data: drafts = [], loading, error } = useFetch(() => draftsApi.listDrafts());

  const allRows = useMemo(() => flattenDraftsToHistory(drafts), [drafts]);

  const filtered = useMemo(() => {
    let rows = allRows;

    if (channelFilter !== "all") {
      rows = rows.filter((r) => r.channel === channelFilter);
    }

    if (periodFilter !== "all") {
      const days = parseInt(periodFilter, 10);
      const cutoff = new Date();
      cutoff.setDate(cutoff.getDate() - days);
      rows = rows.filter((r) => {
        if (!r.date) return true;
        return new Date(r.date + "T00:00:00") >= cutoff;
      });
    }

    return rows;
  }, [allRows, channelFilter, periodFilter]);

  return (
    <div className="history-page">
      <div className="history-container">
        <header className="history-header">
          <h1 className="history-title">Historial de publicaciones</h1>
          <div className="history-filters">
            <div className="history-filter-group">
              <button
                className={`history-filter-btn ${channelFilter === "all" ? "history-filter-btn--active" : ""}`}
                onClick={() => setChannelFilter("all")}
              >
                todos los canales
              </button>
              {Object.entries(CHANNEL_LABELS).map(([key, label]) => (
                <button
                  key={key}
                  className={`history-filter-btn ${channelFilter === key ? "history-filter-btn--active" : ""}`}
                  onClick={() => setChannelFilter(key)}
                >
                  {label}
                </button>
              ))}
            </div>
            <select
              className="history-period-select"
              value={periodFilter}
              onChange={(e) => setPeriodFilter(e.target.value)}
            >
              {PERIOD_OPTIONS.map((opt) => (
                <option key={opt.value} value={opt.value}>
                  {opt.label}
                </option>
              ))}
            </select>
            <button
              className="history-export-btn"
              onClick={() => exportCSV(filtered)}
              disabled={filtered.length === 0}
            >
              <Download size={14} />
              exportar CSV
            </button>
          </div>
        </header>

        {loading && (
          <div className="history-table__empty">
            Cargando historial…
          </div>
        )}

        {error && (
          <div className="history-table__empty" style={{ color: "#991b1b" }}>
            <AlertCircle size={16} style={{ display: "inline", verticalAlign: "middle" }} /> {error}
          </div>
        )}

        {!loading && !error && (
          <div className="history-table">
            <div className="history-table__head">
              <span>FECHA</span>
              <span>CANAL</span>
              <span>TÍTULO</span>
              <span>ESTADO</span>
              <span>ACCIÓN</span>
            </div>

            <div className="history-table__body">
              {filtered.length === 0 && (
                <div className="history-table__empty">
                  <Inbox size={20} style={{ display: "inline", verticalAlign: "middle", marginRight: 6 }} />
                  No hay publicaciones en este período.
                </div>
              )}
              {filtered.map((row, idx) => (
                <div className="history-row" key={`${row.id}-${row.channel}-${idx}`}>
                  <span className="history-row__date">
                    {row.date ? formatDateShort(row.date) : "—"}
                  </span>
                  <span className="history-row__channel">
                    <span
                      className={`history-channel-badge history-channel-badge--${row.channel}`}
                    >
                      {CHANNEL_LABELS[row.channel]}
                    </span>
                  </span>
                  <span className="history-row__title" title={row.title}>
                    {row.title}
                  </span>
                  <span className="history-row__status">
                    <span
                      className={`history-status-chip history-status-chip--${row.status}`}
                    >
                      <span
                        className="history-status-chip__dot"
                        aria-hidden="true"
                      />
                      {row.status}
                    </span>
                  </span>
                  <span className="history-row__action">
                    <button
                      className="history-open-btn"
                      onClick={() => navigate(`/approval/${row.id}`)}
                    >
                      ver
                    </button>
                  </span>
                </div>
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
