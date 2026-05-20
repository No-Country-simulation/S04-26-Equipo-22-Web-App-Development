import axios from "axios";
import { tokenStorage } from "../auth/tokenStorage";

const baseURL = import.meta.env.VITE_API_URL || "http://localhost:8080";

export const api = axios.create({
  baseURL,
  headers: { "Content-Type": "application/json" },
});

api.interceptors.request.use((config) => {
  const token = tokenStorage.getAccess();
  if (token && !config.headers.Authorization) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

let refreshPromise = null;

async function performRefresh() {
  const refreshToken = tokenStorage.getRefresh();
  if (!refreshToken) throw new Error("no refresh token");
  const { data } = await axios.post(
    `${baseURL}/api/auth/refresh`,
    { refreshToken },
    { headers: { "Content-Type": "application/json" } }
  );
  tokenStorage.set(data);
  return data.accessToken;
}

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const { config, response } = error;
    if (!response || response.status !== 401 || config._retry) {
      return Promise.reject(error);
    }
    const url = config.url || "";
    if (url.includes("/api/auth/login") || url.includes("/api/auth/refresh")) {
      return Promise.reject(error);
    }

    config._retry = true;
    try {
      refreshPromise = refreshPromise || performRefresh();
      const newAccess = await refreshPromise;
      refreshPromise = null;
      config.headers.Authorization = `Bearer ${newAccess}`;
      return api(config);
    } catch (refreshError) {
      refreshPromise = null;
      tokenStorage.clear();
      window.dispatchEvent(new CustomEvent("auth:logout"));
      return Promise.reject(refreshError);
    }
  }
);
