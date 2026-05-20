import { api } from "./client";
import { draftsMock } from "../data/draftsMock";

const USE_MOCK = (import.meta.env.VITE_DRAFTS_USE_MOCK ?? "true") !== "false";
const MOCK_LATENCY_MS = 200;

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
  const params = {};
  if (status) params.status = status;
  if (weekOf) params.weekOf = weekOf;
  const { data } = await api.get("/api/drafts", { params });
  return data;
}

export async function getDraft(id) {
  if (USE_MOCK) {
    const draft = mockStore.find((d) => d.id === id);
    if (!draft) throw new Error(`Draft ${id} not found`);
    return delay(cloneDraft(draft));
  }
  const { data } = await api.get(`/api/drafts/${id}`);
  return data;
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
  const { data } = await api.put(`/api/drafts/${draftId}/channels/${channel}`, payload);
  return data;
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
  const { data } = await api.post(`/api/drafts/${draftId}/transition`, {
    status: nextStatus,
  });
  return data;
}

export async function publishDraft(draftId) {
  return transitionDraft(draftId, "PUBLISHED");
}

export function _resetMockStore() {
  mockStore = JSON.parse(JSON.stringify(draftsMock));
}
