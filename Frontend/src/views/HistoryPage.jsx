import { useState, useMemo } from "react";
import { Download } from "lucide-react";
import { CHANNEL_LABELS } from "../data/draftSelectors";
import { formatDateShort } from "../utils/formatDate";
import "./HistoryPage.css";

const MOCK_DATA = [
  {
    id: 1,
    date: "2025-05-23",
    channel: "newsletter",
    title: "Edición #41 · el truco TQ",
    status: "publicado",
    metric: "opens 38% · clicks 6%",
  },
  {
    id: 2,
    date: "2025-05-23",
    channel: "linkedin",
    title: "El truco de TanStack Query…",
    status: "publicado",
    metric: "2.1k vistas · 47 reacc.",
  },
  {
    id: 3,
    date: "2025-05-23",
    channel: "twitter",
    title: "Hilo: 5 lecciones del AMA",
    status: "publicado",
    metric: "1.4k impr. · 28 RT",
  },
  {
    id: 4,
    date: "2025-05-16",
    channel: "newsletter",
    title: "Edición #40 · semana RAG",
    status: "publicado",
    metric: "opens 41% · clicks 8%",
  },
  {
    id: 5,
    date: "2025-05-16",
    channel: "linkedin",
    title: "3 patrones de RAG…",
    status: "publicado",
    metric: "3.1k vistas · 62 reacc.",
  },
  {
    id: 6,
    date: "2025-05-16",
    channel: "twitter",
    title: "Hilo: lecciones del live",
    status: "rechazado",
    metric: "editor descartó",
  },
  {
    id: 7,
    date: "2025-05-09",
    channel: "newsletter",
    title: "Edición #39",
    status: "publicado",
    metric: "opens 36%",
  },
];

const PERIOD_OPTIONS = [
  { value: "30", label: "últimos 30 días" },
  { value: "90", label: "últimos 90 días" },
  { value: "all", label: "todo" },
];

function formatDate(iso) {
  if (!iso) return "—";
  try {
    const d = new Date(iso + "T00:00:00");
    const day = d.getDate().toString().padStart(2, "0");
    const month = d.toLocaleString("es", { month: "short" }).replace(".", "");
    return `${day} ${month}`;
  } catch {
    return iso;
  }
}

function exportCSV(rows) {
  const header = "Fecha,Canal,Título,Estado,Métrica";
  const lines = rows.map(
    (r) =>
      `${r.date},"${CHANNEL_LABELS[r.channel]}","${r.title}",${r.status},"${r.metric}"`
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
  const [channelFilter, setChannelFilter] = useState("all");
  const [periodFilter, setPeriodFilter] = useState("30");

  const filtered = useMemo(() => {
    let rows = MOCK_DATA;

    if (channelFilter !== "all") {
      rows = rows.filter((r) => r.channel === channelFilter);
    }

    if (periodFilter !== "all") {
      const days = parseInt(periodFilter, 10);
      const cutoff = new Date();
      cutoff.setDate(cutoff.getDate() - days);
      rows = rows.filter((r) => new Date(r.date + "T00:00:00") >= cutoff);
    }

    return rows;
  }, [channelFilter, periodFilter]);

  return (
    <div className="history-page">
      <div className="history-container">
        <header className="history-header">
          <h1 className="history-title">Historial · tabla</h1>
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
            >
              <Download size={14} />
              exportar CSV
            </button>
          </div>
        </header>

        <div className="history-table">
          <div className="history-table__head">
            <span>FECHA</span>
            <span>CANAL</span>
            <span>TÍTULO</span>
            <span>ESTADO</span>
            <span>MÉTRICA</span>
            <span>ACCIÓN</span>
          </div>

          <div className="history-table__body">
            {filtered.length === 0 && (
              <div className="history-table__empty">
                No hay publicaciones en este período.
              </div>
            )}
            {filtered.map((row) => (
              <div className="history-row" key={row.id}>
                <span className="history-row__date">
                  {formatDate(row.date)}
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
                <span className="history-row__metric">{row.metric}</span>
                <span className="history-row__action">
                  <button className="history-open-btn">abrir</button>
                </span>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}
