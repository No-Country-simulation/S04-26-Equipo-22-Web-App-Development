import { useCallback, useRef, useState } from "react";

export function useAsyncAction() {
  const [acting, setActing] = useState(false);
  const [error, setError] = useState(null);
  const actingRef = useRef(false);

  const run = useCallback(async (fn) => {
    if (actingRef.current) return;
    actingRef.current = true;
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
      actingRef.current = false;
      setActing(false);
    }
  }, []);

  return { acting, error, setError, run };
}
