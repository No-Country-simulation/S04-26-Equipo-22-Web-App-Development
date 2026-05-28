import { describe, it, expect } from "vitest";
import {
  CHANNELS,
  CHANNEL_LABELS,
  STATUS_LABELS,
  flattenDraftsForList,
  countByChannel,
  getChannelView,
} from "../draftSelectors";

const sampleDraft = {
  id: "draft-1",
  weekOf: "2026-05-23",
  topicTitle: "Test topic",
  topicSummary: "Summary",
  status: "GENERATED",
  updatedAt: "2026-05-23T18:00:00Z",
  channels: {
    newsletter: { title: "NL Title", body: "<p>NL body</p>", status: "pending" },
    linkedin: { title: "LI Title", body: "LI body", status: "edited" },
    twitter: { title: null, body: "Tweet body", status: "approved" },
  },
};

describe("draftSelectors", () => {
  describe("constants", () => {
    it("CHANNELS contiene los 3 canales esperados", () => {
      expect(CHANNELS).toEqual(["newsletter", "linkedin", "twitter"]);
    });

    it("CHANNEL_LABELS mapea twitter a 'X'", () => {
      expect(CHANNEL_LABELS.twitter).toBe("X");
    });

    it("STATUS_LABELS tiene los 5 estados principales", () => {
      ["GENERATED", "IN_REVIEW", "APPROVED", "PUBLISHED", "REJECTED"].forEach((s) => {
        expect(STATUS_LABELS[s]).toBeDefined();
      });
    });
  });

  describe("flattenDraftsForList", () => {
    it("genera 1 row por canal", () => {
      const rows = flattenDraftsForList([sampleDraft]);
      expect(rows.length).toBe(3);
    });

    it("incluye channelLabel", () => {
      const rows = flattenDraftsForList([sampleDraft]);
      const labels = rows.map((r) => r.channelLabel);
      expect(labels).toContain("Newsletter");
      expect(labels).toContain("LinkedIn");
      expect(labels).toContain("X");
    });

    it("usa topicTitle si el canal no tiene título", () => {
      const draft = {
        ...sampleDraft,
        channels: {
          newsletter: { title: null, body: "x", status: "pending" },
        },
      };
      const rows = flattenDraftsForList([draft]);
      expect(rows[0].title).toBe("Test topic");
    });

    it("array vacío si no hay drafts", () => {
      expect(flattenDraftsForList([])).toEqual([]);
    });

    it("skip canales que no existen en el draft", () => {
      const draft = {
        ...sampleDraft,
        channels: { newsletter: { body: "x", status: "pending" } },
      };
      const rows = flattenDraftsForList([draft]);
      expect(rows.length).toBe(1);
      expect(rows[0].channel).toBe("newsletter");
    });
  });

  describe("countByChannel", () => {
    it("cuenta correctamente cuando todos los canales presentes", () => {
      const counts = countByChannel([sampleDraft]);
      expect(counts.newsletter).toBe(1);
      expect(counts.linkedin).toBe(1);
      expect(counts.twitter).toBe(1);
      expect(counts.all).toBe(3);
    });

    it("inicializa en 0 si no hay drafts", () => {
      const counts = countByChannel([]);
      expect(counts.all).toBe(0);
      expect(counts.newsletter).toBe(0);
    });
  });

  describe("getChannelView (B-021: avatares hardcoded)", () => {
    it("twitter view incluye avatar hardcoded", () => {
      const view = getChannelView(sampleDraft, "twitter");
      expect(view.avatar).toContain("pravatar.cc");
      expect(view.verified).toBe(true);
    });

    it("retorna null si el canal no existe", () => {
      const view = getChannelView(sampleDraft, "facebook");
      expect(view).toBeNull();
    });

    it("incluye topicTitle en el view", () => {
      const view = getChannelView(sampleDraft, "newsletter");
      expect(view.topicTitle).toBe("Test topic");
    });
  });
});
