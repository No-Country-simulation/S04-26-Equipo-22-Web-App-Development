import { useCallback, useEffect, useRef, useState } from "react";

export function useFetch(fetchFn, deps = []) {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const idRef = useRef(0);

  const execute = useCallback(async () => {
    const id = ++idRef.current;
    setLoading(true);
    setError(null);
    try {
      const result = await fetchFn();
      if (id === idRef.current) setData(result);
    } catch (err) {
      if (id === idRef.current) {
        setError(err?.response?.data?.message || err?.message || "Error al cargar datos");
      }
    } finally {
      if (id === idRef.current) setLoading(false);
    }
  }, deps); // eslint-disable-line react-hooks/exhaustive-deps

  useEffect(() => {
    execute();
    return () => { idRef.current++; };
  }, [execute]);

  const refetch = useCallback(() => execute(), [execute]);

  return { data, loading, error, setData, setError, refetch };
}
