import { Download } from "lucide-react";
import "./ApprovalButtons.css";

function ApprovalButtons({ status, onChangeStatus, onPublish, onExport, disabled = false }) {
  return (
    <div className="approval-buttons">
      {status === "GENERATED" && (
        <>
          <button
            className="review-btn"
            disabled={disabled}
            onClick={() => onChangeStatus("IN_REVIEW")}
          >
            Enviar a revisión
          </button>
          <button
            className="reject-btn"
            disabled={disabled}
            onClick={() => onChangeStatus("REJECTED")}
          >
            Rechazar
          </button>
        </>
      )}

      {status === "IN_REVIEW" && (
        <>
          <button
            className="approve-btn"
            disabled={disabled}
            onClick={() => onChangeStatus("APPROVED")}
          >
            Aprobar
          </button>
          <button
            className="changes-btn"
            disabled={disabled}
            onClick={() => onChangeStatus("GENERATED")}
          >
            Pedir cambios
          </button>
          <button
            className="reject-btn"
            disabled={disabled}
            onClick={() => onChangeStatus("REJECTED")}
          >
            Rechazar
          </button>
        </>
      )}

      {status === "APPROVED" && (
        <button
          className="publish-btn"
          disabled={disabled}
          onClick={onPublish}
        >
          Publicar y exportar
        </button>
      )}

      {status === "PUBLISHED" && (
        <>
          <button className="published-btn" disabled>
            Publicado
          </button>
          <button
            className="export-again-btn"
            onClick={onExport}
          >
            <Download size={15} />
            Exportar de nuevo
          </button>
        </>
      )}

      {status === "REJECTED" && (
        <button className="rejected-btn" disabled>
          Rechazado
        </button>
      )}
    </div>
  );
}

export default ApprovalButtons;
