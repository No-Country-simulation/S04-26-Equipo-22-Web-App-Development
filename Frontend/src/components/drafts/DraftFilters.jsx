import { Inbox } from "lucide-react";
import ChannelIcon from "../ChannelIcon";
import "./DraftFilters.css";

const TABS = [
  { id: "all", label: "Todos" },
  { id: "newsletter", label: "Newsletter" },
  { id: "linkedin", label: "LinkedIn" },
  { id: "twitter", label: "X" },
];

export function DraftFilters({ filter, setFilter, counts }) {
  return (
    <div className="filters" role="tablist" aria-label="Filtrar borradores por canal">
      {TABS.map(({ id, label }) => {
        const isActive = filter === id;
        return (
          <button
            key={id}
            type="button"
            role="tab"
            aria-selected={isActive}
            className={`filters__tab filters__tab--${id} ${isActive ? "active" : ""}`}
            onClick={() => setFilter(id)}
          >
            {id === "all" ? (
              <Inbox className="filters__icon" aria-hidden="true" />
            ) : (
              <ChannelIcon channel={id} className="filters__icon" />
            )}
            <span className="filters__label">{label}</span>
            <span className="filters__count">{counts[id] ?? 0}</span>
          </button>
        );
      })}
    </div>
  );
}
