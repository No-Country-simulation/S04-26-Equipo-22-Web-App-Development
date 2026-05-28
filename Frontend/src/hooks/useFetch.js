import { useCallback, useEffect, useRef, useState } from "react";

export function useFetch(fetchFn, deps = []) {
  const [data, setData] = useState(undefined);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const idRef = useRef(0);
  const fetchFnRef = useRef(fetchFn);

  useEffect(() => {
    fetchFnRef.current = fetchFn;
  });

  // eslint-disable-next-line react-hooks/exhaustive-deps, react-hooks/use-memo
  const execute = useCallback(async () => {
    const id = ++idRef.current;
    setLoading(true);
    setError(null);
    try {
      const result = await fetchFnRef.current();
      if (id === idRef.current) setData(result);
    } catch (err) {
      if (id === idRef.current) {
        setError(err?.response?.data?.message || err?.message || "Error al cargar datos");
      }
    } finally {
      if (id === idRef.current) setLoading(false);
    }
  }, deps);

  useEffect(() => {
    execute();
    const ref = idRef;
    return () => { ref.current++; };
  }, [execute]);

  const refetch = useCallback(() => execute(), [execute]);

  return { data, loading, error, setData, setError, refetch };
}
