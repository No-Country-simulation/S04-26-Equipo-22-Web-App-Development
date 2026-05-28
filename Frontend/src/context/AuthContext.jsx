/* eslint-disable react-refresh/only-export-components */
import { createContext, useCallback, useEffect, useState } from "react";
import * as authApi from "../api/auth";
import { tokenStorage } from "../auth/tokenStorage";

export const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [status, setStatus] = useState("loading");

  const loadUser = useCallback(async () => {
    if (!tokenStorage.getAccess()) {
      setUser(null);
      setStatus("unauthenticated");
      return;
    }
    try {
      const me = await authApi.getCurrentUser();
      setUser(me);
      setStatus("authenticated");
    } catch {
      tokenStorage.clear();
      setUser(null);
      setStatus("unauthenticated");
    }
  }, []);

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    loadUser();
    const onLogout = () => {
      setUser(null);
      setStatus("unauthenticated");
    };
    window.addEventListener("auth:logout", onLogout);
    return () => window.removeEventListener("auth:logout", onLogout);
  }, [loadUser]);

  const login = useCallback(async (credentials) => {
    await authApi.login(credentials);
    await loadUser();
  }, [loadUser]);

  const register = useCallback(async (credentials) => {
    await authApi.register(credentials);
    await loadUser();
  }, [loadUser]);

  const logout = useCallback(async () => {
    await authApi.logout();
    setUser(null);
    setStatus("unauthenticated");
  }, []);

  const value = {
    user,
    status,
    isAuthenticated: status === "authenticated",
    isLoading: status === "loading",
    login,
    register,
    logout,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
