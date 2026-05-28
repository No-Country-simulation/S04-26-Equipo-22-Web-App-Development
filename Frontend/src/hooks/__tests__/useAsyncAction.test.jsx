import { describe, it, expect } from "vitest";
import { renderHook, act } from "@testing-library/react";
import { useAsyncAction } from "../useAsyncAction";

describe("useAsyncAction", () => {
  it("inicializa con acting=false y error=null", () => {
    const { result } = renderHook(() => useAsyncAction());
    expect(result.current.acting).toBe(false);
    expect(result.current.error).toBeNull();
  });

  it("ejecuta fn() y retorna su resultado", async () => {
    const { result } = renderHook(() => useAsyncAction());

    let returned;
    await act(async () => {
      returned = await result.current.run(async () => "success");
    });

    expect(returned).toBe("success");
    expect(result.current.acting).toBe(false);
  });

  it("captura errores y los expone via error state", async () => {
    const { result } = renderHook(() => useAsyncAction());

    await act(async () => {
      try {
        await result.current.run(async () => {
          throw new Error("Boom");
        });
      } catch {
        // expected re-throw
      }
    });

    expect(result.current.error).toBe("Boom");
  });

  it("B-014 fix: previene doble-click via actingRef", async () => {
    const { result } = renderHook(() => useAsyncAction());
    let callCount = 0;
    const slowFn = () =>
      new Promise((resolve) => {
        callCount++;
        setTimeout(resolve, 50);
      });

    await act(async () => {
      const p1 = result.current.run(slowFn);
      const p2 = result.current.run(slowFn);
      await Promise.all([p1, p2]);
    });

    expect(callCount).toBe(1);
  });

  it("permite ejecuciones secuenciales", async () => {
    const { result } = renderHook(() => useAsyncAction());
    let callCount = 0;
    const fn = async () => {
      callCount++;
      return callCount;
    };

    await act(async () => {
      await result.current.run(fn);
    });
    await act(async () => {
      await result.current.run(fn);
    });

    expect(callCount).toBe(2);
  });

  it("setError permite limpiar el error manualmente", async () => {
    const { result } = renderHook(() => useAsyncAction());

    await act(async () => {
      try {
        await result.current.run(async () => {
          throw new Error("Test");
        });
      } catch { /* ignore */ }
    });

    expect(result.current.error).toBe("Test");

    act(() => {
      result.current.setError(null);
    });

    expect(result.current.error).toBeNull();
  });
});
