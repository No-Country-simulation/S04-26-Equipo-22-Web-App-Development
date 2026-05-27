import { api } from "./client";

const DIGEST_BASE = "/api/weekly-digests";
const DRAFT_BASE = "/api/channel-drafts";
const PLATFORM_MAP = { newsletter: "NEWSLETTER", linkedin: "LINKEDIN", twitter: "X" };
const CHANNEL_MAP = { NEWSLETTER: "newsletter", LINKEDIN: "linkedin", X: "twitter" };

function mapChannelDraft(d) {
  return {
    channel: CHANNEL_MAP[d.targetPlatform] || d.targetPlatform,
    title: d.title || `Borrador ${CHANNEL_MAP[d.targetPlatform] || d.targetPlatform}`,
    body: d.content || "",
    status: d.status === "GENERATED" ? "pending"
          : d.status === "IN_REVIEW" ? "edited"
          : d.status === "APPROVED" ? "approved"
          : d.status === "REJECTED" ? "rejected"
          : d.status === "PUBLISHED" ? "approved"
          : "pending",
    editedAt: d.approvedAt || d.createdAt,
  };
}

async function fetchDigests() {
  const { data } = await api.get(`${DIGEST_BASE}/latest`, { params: { size: 20 } });
  return data;
}

async function fetchChannelDrafts(digestId) {
  try {
    const { data } = await api.get(`${DRAFT_BASE}/by-digest/${digestId}`);
    return Array.isArray(data) ? data : [];
  } catch {
    return [];
  }
}

async function assembleDraft(digest) {
  const channelDrafts = await fetchChannelDrafts(digest.id);
  const channels = {};
  for (const cd of channelDrafts) {
    const ch = mapChannelDraft(cd);
    channels[ch.channel] = ch;
  }
  if (!channels.newsletter) channels.newsletter = { channel: "newsletter", title: "Borrador newsletter", body: "", status: "pending" };
  if (!channels.linkedin) channels.linkedin = { channel: "linkedin", title: "Borrador linkedin", body: "", status: "pending" };
  if (!channels.twitter) channels.twitter = { channel: "twitter", title: "Borrador twitter", body: "", status: "pending" };

  return {
    id: String(digest.id),
    weekOf: digest.weekStart || "",
    topicTitle: digest.communityName || "Resumen semanal",
    topicSummary: digest.summary || "",
    sourceContributions: [],
    channels,
    // FIX COPILOT: Mapea el estado real del backend para desongelar los botones de aprobación
    status: digest.status || "GENERATED",
    createdAt: digest.createdAt || "",
    updatedAt: digest.createdAt || "",
  };
}

export async function listDrafts({ status, weekOf } = {}) {
  const digests = await fetchDigests();
  const results = await Promise.all(digests.map(assembleDraft));
  let filtered = results;
  if (status) filtered = filtered.filter((d) => d.status === status);
  if (weekOf) filtered = filtered.filter((d) => d.weekOf.startsWith(weekOf));
  return filtered;
}

export async function getDraft(id) {
  const { data } = await api.get(`${DIGEST_BASE}/${id}`);
  return assembleDraft(data);
}

async function findChannelDraftId(digestId, channel) {
  const platform = PLATFORM_MAP[channel];
  if (!platform) throw new Error(`Canal desconocido: ${channel}`);
  const { data } = await api.get(`${DRAFT_BASE}/by-digest-platform`, {
    params: { weeklyDigestId: digestId, platform },
  });
  return data.id;
}

export async function updateChannelDraft(draftId, channel, payload) {
  const realId = await findChannelDraftId(draftId, channel);
  const body = payload.body || payload.content || "";
  await api.patch(`${DRAFT_BASE}/${realId}/content`, { content: body });
  return getDraft(draftId);
}

export async function transitionDraft(draftId, nextStatus) {
  const platformToChannel = { NEWSLETTER: "newsletter", LINKEDIN: "linkedin", X: "twitter" };
  const { data: drafts } = await api.get(`${DRAFT_BASE}/by-digest/${draftId}`);
  for (const draft of drafts) {
    const ch = platformToChannel[draft.targetPlatform];
    if (!ch) continue;
    switch (nextStatus) {
      case "IN_REVIEW":
        await api.patch(`${DRAFT_BASE}/${draft.id}/start-review`);
        break;
      case "APPROVED":
        // NOTA: editorId: 1 asume el ID del administrador generado por tus seeders del backend
        await api.patch(`${DRAFT_BASE}/${draft.id}/approve`, { editorId: 1 });
        break;
      case "REJECTED":
        await api.patch(`${DRAFT_BASE}/${draft.id}/reject`);
        break;
      case "PUBLISHED":
        await api.patch(`${DRAFT_BASE}/${draft.id}/publish`);
        break;
    }
  }
  return getDraft(draftId);
}

export async function publishDraft(draftId) {
  return transitionDraft(draftId, "PUBLISHED");
}