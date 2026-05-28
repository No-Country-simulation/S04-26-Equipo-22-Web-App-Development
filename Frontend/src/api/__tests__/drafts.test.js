import { describe, it, expect, vi, beforeEach } from "vitest";
import * as draftsApi from "../drafts";
import { api } from "../client";

vi.mock("../client", () => ({
  api: {
    get: vi.fn(),
    patch: vi.fn(),
    post: vi.fn(),
    delete: vi.fn(),
  },
}));

const mockDigest = {
  id: 1,
  communityId: 10,
  communityName: "AI Builders",
  weekStart: "2026-05-23",
  weekEnd: "2026-05-29",
  summary: "Resumen semanal sobre RAG",
  status: "PENDING",
  createdAt: "2026-05-23T18:00:00Z",
};

const mockChannelDrafts = [
  {
    id: 101,
    content: "<p>Newsletter body</p>",
    targetPlatform: "NEWSLETTER",
    status: "GENERATED",
    createdAt: "2026-05-23T18:00:00Z",
    approvedAt: null,
    weeklyDigestId: 1,
  },
  {
    id: 102,
    content: "<p>LinkedIn body</p>",
    targetPlatform: "LINKEDIN",
    status: "GENERATED",
    createdAt: "2026-05-23T18:00:00Z",
    approvedAt: null,
    weeklyDigestId: 1,
  },
  {
    id: 103,
    content: "Tweet body",
    targetPlatform: "X",
    status: "GENERATED",
    createdAt: "2026-05-23T18:00:00Z",
    approvedAt: null,
    weeklyDigestId: 1,
  },
];

describe("drafts API", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe("B-001: updateChannelDraft debe enviar el title", () => {
    it("FAIL: title del payload NUNCA se envía al backend", async () => {
      api.get
        .mockResolvedValueOnce({ data: { id: 101 } })
        .mockResolvedValueOnce({ data: mockDigest })
        .mockResolvedValueOnce({ data: mockChannelDrafts });
      api.patch.mockResolvedValueOnce({});

      await draftsApi.updateChannelDraft("1", "newsletter", {
        title: "Mi nuevo título",
        body: "<p>nuevo</p>",
      });

      const patchCall = api.patch.mock.calls[0];
      const sentBody = patchCall[1];

      expect(sentBody).toEqual(
        expect.objectContaining({
          title: "Mi nuevo título",
          content: "<p>nuevo</p>",
        })
      );
    });
  });

  describe("B-002: sourceContributions debe poblarse desde el backend", () => {
    it("FAIL: sourceContributions siempre es array vacío", async () => {
      api.get
        .mockResolvedValueOnce({ data: mockDigest })
        .mockResolvedValueOnce({ data: mockChannelDrafts });

      const draft = await draftsApi.getDraft("1");

      expect(draft.sourceContributions.length).toBeGreaterThan(0);
    });
  });

  describe("B-003: transitionDraft debe hacer rollback en fallo parcial", () => {
    it("FAIL: cuando el segundo PATCH falla, los primeros no se revierten", async () => {
      api.get.mockResolvedValueOnce({ data: mockChannelDrafts });
      api.patch
        .mockResolvedValueOnce({}) // newsletter approve OK
        .mockRejectedValueOnce(new Error("Backend error")); // linkedin approve FAIL

      let rolledBack = false;
      const originalPatch = api.patch.getMockImplementation();
      api.patch.mockImplementation(async (url) => {
        if (url.includes("/reject") || url.includes("/start-review")) {
          rolledBack = true;
        }
        return originalPatch?.(url);
      });

      try {
        await draftsApi.transitionDraft("1", "APPROVED");
      } catch {
        // expected
      }

      expect(rolledBack).toBe(true);
    });

    it("CONFIRMA bug: la transición no es atómica", async () => {
      api.get.mockResolvedValueOnce({ data: mockChannelDrafts });
      api.patch
        .mockResolvedValueOnce({}) // newsletter OK
        .mockRejectedValueOnce(new Error("Backend error")); // linkedin FAIL

      await expect(draftsApi.transitionDraft("1", "APPROVED")).rejects.toThrow();

      const approveCalls = api.patch.mock.calls.filter((c) => c[0].includes("/approve"));
      expect(approveCalls.length).toBe(2);
      const rejectCalls = api.patch.mock.calls.filter((c) => c[0].includes("/reject"));
      expect(rejectCalls.length).toBe(0);
    });
  });

  describe("B-005: status casing consistency", () => {
    it("PASS: status del draft a nivel agregado es UPPERCASE", async () => {
      api.get
        .mockResolvedValueOnce({ data: mockDigest })
        .mockResolvedValueOnce({ data: mockChannelDrafts });

      const draft = await draftsApi.getDraft("1");

      expect(draft.status).toMatch(/^(GENERATED|IN_REVIEW|APPROVED|PUBLISHED|REJECTED)$/);
    });

    it("INCONSISTENCIA: status dentro de channels es lowercase", async () => {
      api.get
        .mockResolvedValueOnce({ data: mockDigest })
        .mockResolvedValueOnce({ data: mockChannelDrafts });

      const draft = await draftsApi.getDraft("1");

      expect(draft.channels.newsletter.status).toBe("pending");
      expect(draft.status).toBe("GENERATED");
    });

    it("computeAggregateStatus mapea correctamente todos los canales APPROVED", async () => {
      const allApproved = mockChannelDrafts.map((d) => ({ ...d, status: "APPROVED" }));
      api.get
        .mockResolvedValueOnce({ data: mockDigest })
        .mockResolvedValueOnce({ data: allApproved });

      const draft = await draftsApi.getDraft("1");

      expect(draft.status).toBe("APPROVED");
    });

    it("computeAggregateStatus devuelve IN_REVIEW si al menos uno está en review", async () => {
      const mixed = [
        { ...mockChannelDrafts[0], status: "IN_REVIEW" },
        { ...mockChannelDrafts[1], status: "GENERATED" },
        { ...mockChannelDrafts[2], status: "GENERATED" },
      ];
      api.get
        .mockResolvedValueOnce({ data: mockDigest })
        .mockResolvedValueOnce({ data: mixed });

      const draft = await draftsApi.getDraft("1");

      expect(draft.status).toBe("IN_REVIEW");
    });
  });

  describe("listDrafts", () => {
    it("PASS: devuelve array vacío si no hay digests", async () => {
      api.get.mockResolvedValueOnce({ data: [] });
      const result = await draftsApi.listDrafts();
      expect(result).toEqual([]);
    });

    it("PASS: filtra por status", async () => {
      api.get
        .mockResolvedValueOnce({ data: [mockDigest] })
        .mockResolvedValueOnce({ data: mockChannelDrafts });

      const result = await draftsApi.listDrafts({ status: "PUBLISHED" });
      expect(result.length).toBe(0);
    });

    it("PASS: respeta el limite de 20 digests del backend (B-015)", async () => {
      api.get.mockResolvedValueOnce({ data: [] });
      await draftsApi.listDrafts();
      expect(api.get).toHaveBeenCalledWith(
        expect.stringContaining("/latest"),
        expect.objectContaining({ params: { size: 20 } })
      );
    });
  });

  describe("getDraft", () => {
    it("PASS: arma channels object con todos los canales aunque falten", async () => {
      api.get
        .mockResolvedValueOnce({ data: mockDigest })
        .mockResolvedValueOnce({ data: [mockChannelDrafts[0]] });

      const draft = await draftsApi.getDraft("1");

      expect(draft.channels.newsletter).toBeDefined();
      expect(draft.channels.linkedin).toBeDefined();
      expect(draft.channels.twitter).toBeDefined();
    });

    it("PASS: convierte digest.id (Long) a string", async () => {
      api.get
        .mockResolvedValueOnce({ data: { ...mockDigest, id: 42 } })
        .mockResolvedValueOnce({ data: mockChannelDrafts });

      const draft = await draftsApi.getDraft("42");
      expect(draft.id).toBe("42");
      expect(typeof draft.id).toBe("string");
    });
  });

  describe("findChannelDraftId / updateChannelDraft", () => {
    it("FAIL: throws con canal desconocido pero error no se maneja gracefully", async () => {
      await expect(
        draftsApi.updateChannelDraft("1", "facebook", { body: "x" })
      ).rejects.toThrow(/Canal desconocido/);
    });
  });

  describe("transitionDraft", () => {
    it("PASS: GENERATED → IN_REVIEW llama start-review", async () => {
      api.get
        .mockResolvedValueOnce({ data: mockChannelDrafts })
        .mockResolvedValueOnce({ data: mockDigest })
        .mockResolvedValueOnce({ data: mockChannelDrafts });
      api.patch.mockResolvedValue({});

      await draftsApi.transitionDraft("1", "IN_REVIEW");

      const startReviewCalls = api.patch.mock.calls.filter((c) =>
        c[0].includes("/start-review")
      );
      expect(startReviewCalls.length).toBe(3);
    });

    it("PASS: APPROVED no se ejecuta si el draft ya está PUBLISHED", async () => {
      const published = mockChannelDrafts.map((d) => ({ ...d, status: "PUBLISHED" }));
      api.get
        .mockResolvedValueOnce({ data: published })
        .mockResolvedValueOnce({ data: mockDigest })
        .mockResolvedValueOnce({ data: published });
      api.patch.mockResolvedValue({});

      await draftsApi.transitionDraft("1", "APPROVED");

      const approveCalls = api.patch.mock.calls.filter((c) => c[0].includes("/approve"));
      expect(approveCalls.length).toBe(0);
    });
  });
});
