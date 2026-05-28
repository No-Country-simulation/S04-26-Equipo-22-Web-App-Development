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
    rawStatus: d.status,
    status: d.status === "GENERATED" ? "pending"
          : d.status === "IN_REVIEW" ? "edited"
          : d.status === "APPROVED" ? "approved"
          : d.status === "REJECTED" ? "rejected"
          : d.status === "PUBLISHED" ? "published"
          : "pending",
    editedAt: d.approvedAt || d.createdAt,
  };
}

async function fetchDigests() {
  const { data } = await api.get(`${DIGEST_BASE}/latest`, { params: { size: 20 } });
  return Array.isArray(data) ? data : [];
}

async function fetchChannelDrafts(digestId) {
  try {
    const { data } = await api.get(`${DRAFT_BASE}/by-digest/${digestId}`);
    return Array.isArray(data) ? data : [];
  } catch {
    return [];
  }
}

function computeAggregateStatus(channelDrafts) {
  if (!channelDrafts || channelDrafts.length === 0) return "GENERATED";
  const statuses = channelDrafts.map((d) => d.status);

  if (statuses.every((s) => s === "PUBLISHED")) return "PUBLISHED";
  if (statuses.every((s) => s === "APPROVED" || s === "PUBLISHED")) return "APPROVED";
  if (statuses.some((s) => s === "IN_REVIEW")) return "IN_REVIEW";
  if (statuses.every((s) => s === "REJECTED")) return "REJECTED";
  return "GENERATED";
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

  const aggregateStatus = computeAggregateStatus(channelDrafts);
  const latestUpdate = channelDrafts.reduce((acc, d) => {
    const ts = d.approvedAt || d.createdAt;
    return ts && ts > acc ? ts : acc;
  }, digest.createdAt || "");

  return {
    id: String(digest.id),
    weekOf: digest.weekStart || "",
    topicTitle: digest.communityName || "Resumen semanal",
    topicSummary: digest.summary || "",
    sourceContributions: [],
    channels,
    status: aggregateStatus,
    digestStatus: digest.status,
    createdAt: digest.createdAt || "",
    updatedAt: latestUpdate || digest.createdAt || "",
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

async function patchIgnoringStateConflict(url) {
  try {
    await api.patch(url);
  } catch (err) {
    if (err?.response?.status !== 400) throw err;
  }
}

export async function transitionDraft(draftId, nextStatus) {
  const platformToChannel = { NEWSLETTER: "newsletter", LINKEDIN: "linkedin", X: "twitter" };
  const { data: drafts } = await api.get(`${DRAFT_BASE}/by-digest/${draftId}`);
  for (const draft of drafts) {
    const ch = platformToChannel[draft.targetPlatform];
    if (!ch) continue;
    switch (nextStatus) {
      case "IN_REVIEW":
        if (draft.status === "GENERATED") {
          await patchIgnoringStateConflict(`${DRAFT_BASE}/${draft.id}/start-review`);
        }
        break;
      case "APPROVED":
        if (draft.status !== "APPROVED" && draft.status !== "PUBLISHED") {
          await patchIgnoringStateConflict(`${DRAFT_BASE}/${draft.id}/approve`);
        }
        break;
      case "REJECTED":
        if (draft.status !== "PUBLISHED") {
          await patchIgnoringStateConflict(`${DRAFT_BASE}/${draft.id}/reject`);
        }
        break;
      case "PUBLISHED":
        if (draft.status === "APPROVED") {
          await patchIgnoringStateConflict(`${DRAFT_BASE}/${draft.id}/publish`);
        }
        break;
    }
  }
  return getDraft(draftId);
}

export async function publishDraft(draftId) {
  return transitionDraft(draftId, "PUBLISHED");
}

export async function approveAllDrafts(weeklyDigestId) {
  await api.patch(`${DRAFT_BASE}/approve-all/${weeklyDigestId}`);
  return getDraft(weeklyDigestId);
}

export async function areAllDraftsApproved(weeklyDigestId) {
  const { data } = await api.get(`${DRAFT_BASE}/are-all-approved/${weeklyDigestId}`);
  return Boolean(data);
}