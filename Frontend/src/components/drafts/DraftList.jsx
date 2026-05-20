import { Inbox } from "lucide-react";
import { DraftCard } from "./DraftCard";
import "./DraftList.css";

export function DraftList({ rows }) {
  if (!rows || rows.length === 0) {
    return (
      <div className="draft-empty">
        <div className="draft-empty__icon" aria-hidden="true">
          <Inbox />
        </div>
        <h3 className="draft-empty__title">Aún no hay borradores</h3>
        <p className="draft-empty__text">
          Los borradores semanales aparecen aquí cada viernes EOD,
          generados por IA a partir de las contribuciones de tus comunidades.
        </p>
      </div>
    );
  }

  return (
    <div className="draft-list">
      <div className="draft-header" role="row">
        <p>Canal</p>
        <p>Título</p>
        <p>Estado canal</p>
        <p>Última actualización</p>
        <p className="draft-header__actions">Acciones</p>
      </div>

      <div className="draft-list__rows">
        {rows.map((row) => (
          <DraftCard key={`${row.draftId}-${row.channel}`} row={row} />
        ))}
      </div>
    </div>
  );
}
