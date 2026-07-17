import { fireEvent, screen, waitFor } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vitest";
import {
  disconnectGoogleConnection,
  getGoogleConnection,
} from "@/lib/google-connection-api";
import { scanGmail } from "@/lib/gmail-api";
import { renderWithQuery } from "@/test/render-with-query";
import { GoogleConnectionPanel } from "./GoogleConnectionPanel";

vi.mock("@/lib/google-connection-api", () => ({
  getGoogleConnection: vi.fn(),
  disconnectGoogleConnection: vi.fn(),
}));

vi.mock("@/lib/gmail-api", () => ({
  scanGmail: vi.fn(),
}));

describe("GoogleConnectionPanel", () => {
  beforeEach(() => {
    vi.mocked(getGoogleConnection).mockReset();
    vi.mocked(disconnectGoogleConnection).mockReset();
    vi.mocked(scanGmail).mockReset();
  });

  it("offers a separate Gmail connection when Gmail is not connected", async () => {
    vi.mocked(getGoogleConnection).mockResolvedValue({
      connected: false,
      gmailAddress: null,
      connectedAt: null,
    });

    renderWithQuery(<GoogleConnectionPanel />);

    const connectLink = await screen.findByRole("link", {
      name: "Connect Gmail",
    });
    expect(connectLink.getAttribute("href")).toBe(
      "/oauth2/authorization/google-gmail",
    );
  });

  it("shows the mailbox and disconnects it", async () => {
    vi.mocked(getGoogleConnection).mockResolvedValue({
      connected: true,
      gmailAddress: "developer@example.com",
      connectedAt: "2026-07-20T10:00:00Z",
    });
    vi.mocked(disconnectGoogleConnection).mockResolvedValue();

    renderWithQuery(<GoogleConnectionPanel />);

    expect(
      await screen.findByText("Connected as developer@example.com"),
    ).toBeDefined();
    fireEvent.click(
      screen.getByRole("button", { name: "Disconnect Gmail" }),
    );

    await waitFor(() => {
      expect(disconnectGoogleConnection).toHaveBeenCalledOnce();
    });
    expect(await screen.findByRole("link", { name: "Connect Gmail" })).toBeDefined();
  });

  it("manually scans Gmail and shows persisted candidate metadata", async () => {
    vi.mocked(getGoogleConnection).mockResolvedValue({
      connected: true,
      gmailAddress: "developer@example.com",
      connectedAt: "2026-07-20T10:00:00Z",
    });
    vi.mocked(scanGmail).mockResolvedValue({
      scannedAt: "2026-07-20T10:05:00Z",
      candidatesFound: 1,
      newCandidatesFound: 1,
      candidates: [
        {
          gmailMessageId: "message-1",
          gmailThreadId: "thread-1",
          sender: "Recruiter <recruiter@example.com>",
          subject: "Interview invitation",
          receivedAt: "2026-07-19T14:30:00Z",
          newlyDiscovered: true,
          classification: "JOB_RELATED",
          eventType: "INTERVIEW",
          confidenceScore: 95,
          classificationReason: "Interview terminology was found",
        },
      ],
    });

    renderWithQuery(<GoogleConnectionPanel />);

    fireEvent.click(
      await screen.findByRole("button", { name: "Scan Gmail" }),
    );

    expect(await screen.findByText("Interview invitation")).toBeDefined();
    expect(
      screen.getByText("Recruiter <recruiter@example.com>"),
    ).toBeDefined();
    expect(
      screen.getByText(
        "Candidate metadata is stored for classification and duplicate prevention. No applications have been changed.",
      ),
    ).toBeDefined();
    expect(screen.getByText("New")).toBeDefined();
    expect(
      screen.getByText("Likely job-related · Interview · Rule score 95/100"),
    ).toBeDefined();
    expect(screen.getByText("Interview terminology was found")).toBeDefined();
  });
});
