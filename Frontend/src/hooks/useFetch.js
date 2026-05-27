import { useCallback, useEffect, useState } from "react";

export function useFetch(fetchFn, deps = []) {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const execute = useCallback(async () => {
    let cancelled = false;
    setLoading(true);
    setError(null);
    try {
      const result = await fetchFn();
      if (!cancelled) setData(result);
    } catch (err) {
      if (!cancelled) setError(err?.response?.data?.message || err?.message || "Error al cargar datos");
    } finally {
      if (!cancelled) setLoading(false);
    }
    return () => { cancelled = true; };
  }, deps); // eslint-disable-line react-hooks/exhaustive-deps

  useEffect(() => {
    let cleanup;
    execute().then((c) => { cleanup = c; });
    return () => { cleanup?.(); };
  }, [execute]);

  const refetch = useCallback(() => execute(), [execute]);

  return { data, loading, error, setData, setError, refetch };
}
