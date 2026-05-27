import { api } from "./client";
import { draftsMock } from "../data/draftsMock";

const USE_MOCK = (import.meta.env.VITE_DRAFTS_USE_MOCK ?? "true") !== "false";
const MOCK_LATENCY_MS = 200;

/* ── Platform mapping helpers ─────────────────────────────── */
const PLATFORM_TO_KEY = { NEWSLETTER: "newsletter", LINKEDIN: "linkedin", X: "twitter" };
const KEY_TO_PLATFORM = { newsletter: "NEWSLETTER", linkedin: "LINKEDIN", twitter: "X" };

/* ── Backend → Frontend transformation ────────────────────── */
function deriveStatus(channelDrafts) {
  const statuses = channelDrafts.map((cd) => cd.status);
  if (statuses.every((s) => s === "PUBLISHED")) return "PUBLISHED";
  if (statuses.some((s) => s === "REJECTED")) return "REJECTED";
  if (statuses.some((s) => s === "APPROVED")) return "APPROVED";
  if (statuses.some((s) => s === "IN_REVIEW")) return "IN_REVIEW";
  return "GENERATED";
}

function toFrontendDraft(digest, channelDrafts) {
  const channels = {};
  let approvedAt = null;
  let publishedAt = null;

  for (const cd of channelDrafts) {
    const key = PLATFORM_TO_KEY[cd.targetPlatform];
    if (!key) continue;
    channels[key] = {
      id: cd.id,
      title: "",
      body: cd.content || "",
      status: cd.status?.toLowerCase() || "pending",
    };
    if (cd.approvedAt && !approvedAt) approvedAt = cd.approvedAt;
  }

  return {
    id: String(digest.id),
    weekOf: digest.weekStart,
    topicTitle: digest.summary || digest.communityName || "Sin título",
    topicSummary: digest.summary || "",
    status: deriveStatus(channelDrafts),
    createdAt: digest.createdAt,
    updatedAt: digest.createdAt,
    approvedAt,
    publishedAt,
    sourceContributions: [],
    channels,
  };
}

async function fetchFrontendDraft(digestId) {
  const [{ data: digest }, { data: channelDrafts }] = await Promise.all([
    api.get(`/api/weekly-digests/${digestId}`),
    api.get(`/api/channel-drafts/by-digest/${digestId}`),
  ]);
  return toFrontendDraft(digest, channelDrafts);
}

let mockStore = JSON.parse(JSON.stringify(draftsMock));

function delay(value) {
  return new Promise((resolve) => setTimeout(() => resolve(value), MOCK_LATENCY_MS));
}

function cloneDraft(d) {
  return JSON.parse(JSON.stringify(d));
}

function findDraftIndex(id) {
  const idx = mockStore.findIndex((d) => d.id === id);
  if (idx === -1) throw new Error(`Draft ${id} not found`);
  return idx;
}

function nowIso() {
  return new Date().toISOString();
}

export async function listDrafts({ status, weekOf } = {}) {
  if (USE_MOCK) {
    let result = mockStore;
    if (status) result = result.filter((d) => d.status === status);
    if (weekOf) result = result.filter((d) => d.weekOf === weekOf);
    return delay(result.map(cloneDraft));
  }
  const { data: digests } = await api.get("/api/weekly-digests/latest", {
    params: { page: 0, size: 50 },
  });
  const results = await Promise.all(
    digests.map(async (digest) => {
      const { data: channelDrafts } = await api.get(`/api/channel-drafts/by-digest/${digest.id}`);
      return toFrontendDraft(digest, channelDrafts);
    })
  );
  let filtered = results;
  if (status) filtered = filtered.filter((d) => d.status === status);
  if (weekOf) filtered = filtered.filter((d) => d.weekOf === weekOf);
  return filtered;
}

export async function getDraft(id) {
  if (USE_MOCK) {
    const draft = mockStore.find((d) => d.id === id);
    if (!draft) throw new Error(`Draft ${id} not found`);
    return delay(cloneDraft(draft));
  }
  return fetchFrontendDraft(id);
}

export async function updateChannelDraft(draftId, channel, payload) {
  if (USE_MOCK) {
    const idx = findDraftIndex(draftId);
    const next = cloneDraft(mockStore[idx]);
    next.channels[channel] = {
      ...next.channels[channel],
      ...payload,
      status: "edited",
      editedAt: nowIso(),
    };
    next.updatedAt = nowIso();
    mockStore[idx] = next;
    return delay(cloneDraft(next));
  }
  const platform = KEY_TO_PLATFORM[channel];
  const { data: channelDraft } = await api.get("/api/channel-drafts/by-digest-platform", {
    params: { weeklyDigestId: draftId, platform },
  });
  await api.patch(`/api/channel-drafts/${channelDraft.id}/content`, {
    content: payload.body || payload.content || "",
  });
  return fetchFrontendDraft(draftId);
}

const VALID_TRANSITIONS = {
  GENERATED: ["IN_REVIEW", "REJECTED"],
  IN_REVIEW: ["APPROVED", "GENERATED", "REJECTED"],
  APPROVED: ["PUBLISHED", "IN_REVIEW"],
  PUBLISHED: [],
  REJECTED: [],
};

export async function transitionDraft(draftId, nextStatus) {
  if (USE_MOCK) {
    const idx = findDraftIndex(draftId);
    const current = mockStore[idx].status;
    const allowed = VALID_TRANSITIONS[current] || [];
    if (!allowed.includes(nextStatus)) {
      throw new Error(`Transición no permitida: ${current} → ${nextStatus}`);
    }
    const next = cloneDraft(mockStore[idx]);
    next.status = nextStatus;
    next.updatedAt = nowIso();
    if (nextStatus === "APPROVED") next.approvedAt = nowIso();
    if (nextStatus === "PUBLISHED") next.publishedAt = nowIso();
    mockStore[idx] = next;
    return delay(cloneDraft(next));
  }
  const TRANSITION_ENDPOINTS = {
    IN_REVIEW: (id) => ({ method: "patch", url: `/api/channel-drafts/${id}/start-review` }),
    APPROVED: (id) => ({ method: "patch", url: `/api/channel-drafts/${id}/approve`, data: {} }),
    REJECTED: (id) => ({ method: "patch", url: `/api/channel-drafts/${id}/reject` }),
    PUBLISHED: (id) => ({ method: "patch", url: `/api/channel-drafts/${id}/publish` }),
  };

  const endpoint = TRANSITION_ENDPOINTS[nextStatus];
  if (!endpoint) throw new Error(`Transición no soportada: ${nextStatus}`);

  const { data: channelDrafts } = await api.get(`/api/channel-drafts/by-digest/${draftId}`);
  await Promise.all(
    channelDrafts.map((cd) => {
      const { method, url, data } = endpoint(cd.id);
      return api[method](url, data);
    })
  );
  return fetchFrontendDraft(draftId);
}

export async function publishDraft(draftId) {
  return transitionDraft(draftId, "PUBLISHED");
}

export function _resetMockStore() {
  mockStore = JSON.parse(JSON.stringify(draftsMock));
}
