import { describe, it, expect, vi } from "vitest";
import { renderHook, waitFor, act } from "@testing-library/react";
import { useFetch } from "../useFetch";

describe("useFetch", () => {
  it("loading=true inicialmente, después data poblada", async () => {
    const fetchFn = vi.fn().mockResolvedValue([1, 2, 3]);
    const { result } = renderHook(() => useFetch(fetchFn));

    expect(result.current.loading).toBe(true);
    expect(result.current.data).toBeUndefined();

    await waitFor(() => expect(result.current.loading).toBe(false));

    expect(result.current.data).toEqual([1, 2, 3]);
    expect(result.current.error).toBeNull();
  });

  it("setea error cuando fetchFn rechaza", async () => {
    const fetchFn = vi.fn().mockRejectedValue(new Error("Network down"));
    const { result } = renderHook(() => useFetch(fetchFn));

    await waitFor(() => expect(result.current.loading).toBe(false));

    expect(result.current.error).toBe("Network down");
    expect(result.current.data).toBeUndefined();
  });

  it("refetch ejecuta de nuevo fetchFn", async () => {
    const fetchFn = vi.fn().mockResolvedValue("first");
    const { result } = renderHook(() => useFetch(fetchFn));

    await waitFor(() => expect(result.current.loading).toBe(false));
    expect(fetchFn).toHaveBeenCalledTimes(1);

    fetchFn.mockResolvedValue("second");
    await act(async () => {
      await result.current.refetch();
    });

    expect(fetchFn).toHaveBeenCalledTimes(2);
    expect(result.current.data).toBe("second");
  });

  it("B-004 fix: race condition - fetch viejo no overrides nuevo", async () => {
    let resolveFirst;
    let resolveSecond;
    const fetchFn = vi.fn()
      .mockImplementationOnce(() => new Promise((r) => { resolveFirst = r; }))
      .mockImplementationOnce(() => new Promise((r) => { resolveSecond = r; }));

    const { result, rerender } = renderHook(({ deps }) => useFetch(fetchFn, deps), {
      initialProps: { deps: [1] },
    });

    rerender({ deps: [2] });

    await act(async () => {
      resolveSecond("new-data");
      await new Promise((r) => setTimeout(r, 10));
    });

    await act(async () => {
      resolveFirst("old-data");
      await new Promise((r) => setTimeout(r, 10));
    });

    expect(result.current.data).toBe("new-data");
  });

  it("setData permite update manual", async () => {
    const fetchFn = vi.fn().mockResolvedValue("initial");
    const { result } = renderHook(() => useFetch(fetchFn));

    await waitFor(() => expect(result.current.loading).toBe(false));

    act(() => {
      result.current.setData("manual update");
    });

    expect(result.current.data).toBe("manual update");
  });
});
