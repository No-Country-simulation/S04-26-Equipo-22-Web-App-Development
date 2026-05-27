export const CHANNELS = ["newsletter", "linkedin", "twitter"];

export const CHANNEL_LABELS = {
  newsletter: "Newsletter",
  linkedin: "LinkedIn",
  twitter: "X",
};

export const STATUS_LABELS = {
  GENERATED: "Generado",
  IN_REVIEW: "En revisión",
  APPROVED: "Aprobado",
  PUBLISHED: "Publicado",
  REJECTED: "Rechazado",
};

export function flattenDraftsForList(drafts) {
  if (!Array.isArray(drafts)) return [];
  const rows = [];
  for (const draft of drafts) {
    for (const channel of CHANNELS) {
      const ch = draft.channels[channel];
      if (!ch) continue;
      rows.push({
        draftId: draft.id,
        weekOf: draft.weekOf,
        channel,
        channelLabel: CHANNEL_LABELS[channel],
        title: ch.title || draft.topicTitle,
        channelStatus: ch.status,
        draftStatus: draft.status,
        updatedAt: draft.updatedAt,
      });
    }
  }
  return rows;
}

export function countByChannel(drafts) {
  if (!Array.isArray(drafts)) return { newsletter: 0, linkedin: 0, twitter: 0, all: 0 };
  const counts = { newsletter: 0, linkedin: 0, twitter: 0, all: 0 };
  for (const draft of drafts) {
    for (const channel of CHANNELS) {
      if (draft.channels[channel]) {
        counts[channel] += 1;
        counts.all += 1;
      }
    }
  }
  return counts;
}

export function getChannelView(draft, channel) {
  const ch = draft.channels[channel];
  if (!ch) return null;
  const base = {
    id: `${draft.id}-${channel}`,
    draftId: draft.id,
    channel,
    channelLabel: CHANNEL_LABELS[channel],
    title: ch.title,
    body: ch.body,
    content: ch.body,
    status: ch.status,
    weekOf: draft.weekOf,
    topicTitle: draft.topicTitle,
  };
  switch (channel) {
    case "twitter":
      return {
        ...base,
        user: "TalentCircle",
        username: "@talentcircle_dev",
        avatar: "https://i.pravatar.cc/150?img=15",
        verified: true,
        time: "2m",
      };
    case "linkedin":
      return {
        ...base,
        user: "TalentCircle",
        role: "Comunidad de desarrolladores",
        avatar: "https://i.pravatar.cc/150?img=12",
        time: "2m",
      };
    case "newsletter":
      return {
        ...base,
        subject: ch.title,
        time: "2m",
      };
    default:
      return base;
  }
}
