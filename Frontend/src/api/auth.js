import { api } from "./client";
import { tokenStorage } from "../auth/tokenStorage";

export async function login({ email, password }) {
  const { data } = await api.post("/api/auth/login", { email, password });
  tokenStorage.set(data);
  return data;
}

export async function register({ email, password }) {
  const { data } = await api.post("/api/auth/register", { email, password });
  tokenStorage.set(data);
  return data;
}

export async function logout() {
  try {
    await api.post("/api/auth/logout");
  } finally {
    tokenStorage.clear();
  }
}

export async function getCurrentUser() {
  const { data } = await api.get("/api/users/me");
  return data;
}
