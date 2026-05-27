import { useCallback, useEffect, useRef, useState } from "react";

export function useFetch(fetchFn, deps = []) {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const cancelledRef = useRef(false);

  const execute = useCallback(async () => {
    cancelledRef.current = false;
    setLoading(true);
    setError(null);
    try {
      const result = await fetchFn();
      if (!cancelledRef.current) setData(result);
    } catch (err) {
      if (!cancelledRef.current) {
        setError(err?.response?.data?.message || err?.message || "Error al cargar datos");
      }
    } finally {
      if (!cancelledRef.current) setLoading(false);
    }
  }, deps); // eslint-disable-line react-hooks/exhaustive-deps

  useEffect(() => {
    execute();
    return () => { cancelledRef.current = true; };
  }, [execute]);

  const refetch = useCallback(() => execute(), [execute]);

  return { data, loading, error, setData, setError, refetch };
}
