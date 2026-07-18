import { fireEvent, screen, waitFor } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vitest";
import {
  disconnectGoogleConnection,
  getGoogleConnection,
} from "@/lib/google-connection-api";
import {
  dismissGmailReview,
  listGmailReviews,
  matchGmailReview,
  scanGmail,
} from "@/lib/gmail-api";
import { renderWithQuery } from "@/test/render-with-query";
import { GoogleConnectionPanel } from "./GoogleConnectionPanel";

vi.mock("@/lib/google-connection-api", () => ({
  getGoogleConnection: vi.fn(),
  disconnectGoogleConnection: vi.fn(),
}));

vi.mock("@/lib/gmail-api", () => ({
  scanGmail: vi.fn(),
  listGmailReviews: vi.fn(),
  matchGmailReview: vi.fn(),
  dismissGmailReview: vi.fn(),
}));

describe("GoogleConnectionPanel", () => {
  beforeEach(() => {
    vi.mocked(getGoogleConnection).mockReset();
    vi.mocked(disconnectGoogleConnection).mockReset();
    vi.mocked(scanGmail).mockReset();
    vi.mocked(listGmailReviews).mockReset();
    vi.mocked(matchGmailReview).mockReset();
    vi.mocked(dismissGmailReview).mockReset();
    vi.mocked(listGmailReviews).mockResolvedValue([]);
  });

  it("offers a separate Gmail connection when Gmail is not connected", async () => {
    vi.mocked(getGoogleConnection).mockResolvedValue({
      connected: false,
      gmailAddress: null,
      connectedAt: null,
    });

    renderWithQuery(<GoogleConnectionPanel applications={[]} />);

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

    renderWithQuery(<GoogleConnectionPanel applications={[]} />);

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
          reviewId: 12,
          reviewStatus: "PENDING",
          suggestedApplication: null,
          selectedApplicationId: null,
        },
      ],
    });

    renderWithQuery(<GoogleConnectionPanel applications={[]} />);

    fireEvent.click(
      await screen.findByRole("button", { name: "Scan Gmail" }),
    );

    expect(await screen.findByText("Interview invitation")).toBeDefined();
    expect(screen.getByText("Waiting for review")).toBeDefined();
    expect(screen.getByText("Latest scan results (1)")).toBeDefined();
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

  it("confirms a suggested application from the review queue", async () => {
    const review = {
      gmailMessageId: "message-1",
      gmailThreadId: "thread-1",
      sender: "Acme Careers <careers@acme.example>",
      subject: "Software Engineer interview",
      receivedAt: "2026-07-19T14:30:00Z",
      newlyDiscovered: false,
      classification: "JOB_RELATED" as const,
      eventType: "INTERVIEW" as const,
      confidenceScore: 95,
      classificationReason: "Interview terminology was found",
      reviewId: 12,
      reviewStatus: "PENDING" as const,
      suggestedApplication: {
        applicationId: 4,
        companyName: "Acme",
        roleTitle: "Software Engineer",
        confidenceScore: 100,
        reason: "Company and role appear in the sender or subject",
      },
      selectedApplicationId: null,
    };
    vi.mocked(getGoogleConnection).mockResolvedValue({
      connected: true,
      gmailAddress: "developer@example.com",
      connectedAt: "2026-07-20T10:00:00Z",
    });
    vi.mocked(listGmailReviews)
      .mockResolvedValueOnce([review])
      .mockResolvedValueOnce([]);
    vi.mocked(matchGmailReview).mockResolvedValue({
      ...review,
      reviewStatus: "MATCHED",
      selectedApplicationId: 4,
    });

    renderWithQuery(
      <GoogleConnectionPanel
        applications={[
          {
            id: 4,
            companyName: "Acme",
            roleTitle: "Software Engineer",
            status: "APPLIED",
            applicationDate: "2026-07-01",
            notes: "",
            lastActivityDate: "2026-07-01",
          },
        ]}
      />,
    );

    expect(
      await screen.findByText(
        "Suggested: Software Engineer at Acme · Match score 100/100",
      ),
    ).toBeDefined();
    fireEvent.click(screen.getByRole("button", { name: "Confirm match" }));

    await waitFor(() => {
      expect(matchGmailReview).toHaveBeenCalledWith(12, 4);
    });
  });
});
