import "./DraftCard.css";
import { Pencil, Eye } from "lucide-react";
import { Link } from "react-router-dom";
import ChannelIcon from "../ChannelIcon";
import { formatDateTime } from "../../utils/formatDate";

const channelIcons = {
  newsletter: <ChannelIcon channel="newsletter" className="channel-icon channel-icon--newsletter" />,
  linkedin: <ChannelIcon channel="linkedin" className="channel-icon channel-icon--linkedin" />,
  twitter: <ChannelIcon channel="twitter" className="channel-icon channel-icon--twitter" />,
};

const STATUS_LABEL = {
  pending: "Pendiente",
  edited: "Editado",
  approved: "Aprobado",
  rejected: "Rechazado",
};

export function DraftCard({ row }) {
  const icon = channelIcons[row.channel] ?? null;
  const statusKey = (row.channelStatus || "").toLowerCase();

  return (
    <div className="draft-card">
      <div className="draft-channel" data-channel={row.channel}>
        {icon}
        <span className="draft-channel__label">{row.channelLabel}</span>
      </div>

      <div className="draft-title" title={row.title}>{row.title}</div>

      <div className={`draft-status draft-status--${statusKey}`}>
        <span className="draft-status__dot" aria-hidden="true" />
        {STATUS_LABEL[statusKey] || row.channelStatus}
      </div>

      <div className="draft-date">{formatDateTime(row.updatedAt)}</div>

      <div className="draft-actions">
        <Link to={`/editor/${row.draftId}/${row.channel}`}>
          <button title="Editar" aria-label="Editar borrador">
            <Pencil />
          </button>
        </Link>

        <Link to={`/preview?draftId=${row.draftId}&channel=${row.channel}`}>
          <button title="Vista previa" aria-label="Vista previa del borrador">
            <Eye />
          </button>
        </Link>
      </div>
    </div>
  );
}
