import { api } from "./client";

export async function listCommunities({ onlyActive = false } = {}) {
  const path = onlyActive ? "/api/communities/active" : "/api/communities";
  const { data } = await api.get(path);
  return data;
}

export async function getCommunity(id) {
  const { data } = await api.get(`/api/communities/${id}`);
  return data;
}

export async function createCommunity(payload) {
  const { data } = await api.post("/api/communities", payload);
  return data;
}

export async function updateCommunity(id, payload) {
  const { data } = await api.put(`/api/communities/${id}`, payload);
  return data;
}

export async function activateCommunity(id) {
  await api.patch(`/api/communities/${id}/activate`);
}

export async function deactivateCommunity(id) {
  await api.patch(`/api/communities/${id}/deactivate`);
}

export async function deleteCommunity(id) {
  await api.delete(`/api/communities/${id}`);
}
