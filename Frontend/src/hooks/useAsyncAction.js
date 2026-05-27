import { useCallback, useState } from "react";

export function useAsyncAction() {
  const [acting, setActing] = useState(false);
  const [error, setError] = useState(null);

  const run = useCallback(async (fn) => {
    setActing(true);
    setError(null);
    try {
      const result = await fn();
      return result;
    } catch (err) {
      const msg = err?.response?.data?.message || err?.message || "Operación fallida";
      setError(msg);
      throw err;
    } finally {
      setActing(false);
    }
  }, []);

  return { acting, error, setError, run };
}
