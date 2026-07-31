import { fireEvent, screen, waitFor } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vitest";
import {
  disconnectGoogleConnection,
  getGoogleConnection,
} from "@/lib/google-connection-api";
import {
  createApplicationFromGmailReview,
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
  gmailEventTypes: [
    "APPLICATION",
    "INTERVIEW",
    "ASSESSMENT",
    "OFFER",
    "REJECTION",
    "RECRUITER_CONTACT",
    "UNKNOWN",
  ],
  scanGmail: vi.fn(),
  listGmailReviews: vi.fn(),
  matchGmailReview: vi.fn(),
  createApplicationFromGmailReview: vi.fn(),
  dismissGmailReview: vi.fn(),
}));

describe("GoogleConnectionPanel", () => {
  beforeEach(() => {
    vi.mocked(getGoogleConnection).mockReset();
    vi.mocked(disconnectGoogleConnection).mockReset();
    vi.mocked(scanGmail).mockReset();
    vi.mocked(listGmailReviews).mockReset();
    vi.mocked(matchGmailReview).mockReset();
    vi.mocked(createApplicationFromGmailReview).mockReset();
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
    fireEvent.click(screen.getByRole("button", { name: "Disconnect Gmail" }));

    await waitFor(() => {
      expect(disconnectGoogleConnection).toHaveBeenCalledOnce();
    });
    expect(
      await screen.findByRole("link", { name: "Connect Gmail" }),
    ).toBeDefined();
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
          excerpt: "We would like to invite you to interview for the role.",
          receivedAt: "2026-07-19T14:30:00Z",
          newlyDiscovered: true,
          classification: "JOB_RELATED",
          eventType: "INTERVIEW",
          confidenceScore: 95,
          classificationReason: "Interview terminology was found",
          reviewId: 12,
          reviewStatus: "PENDING",
          suggestedApplication: null,
          applicationDraft: { companyName: "", roleTitle: "" },
          selectedApplicationId: null,
        },
      ],
    });

    renderWithQuery(<GoogleConnectionPanel applications={[]} />);

    fireEvent.click(await screen.findByRole("button", { name: "Scan Gmail" }));

    expect(await screen.findByText("Interview invitation")).toBeDefined();
    expect(screen.getByText("Waiting for review")).toBeDefined();
    expect(screen.getByText("Recruiter <recruiter@example.com>")).toBeDefined();
    expect(
      screen.getByText(
        "We would like to invite you to interview for the role.",
      ),
    ).toBeDefined();
    expect(
      screen.getByText(
        "Candidate metadata and a short Gmail excerpt are stored for classification and duplicate prevention. Full email bodies and attachments are not stored. No applications have been changed.",
      ),
    ).toBeDefined();
    expect(screen.getByText("New")).toBeDefined();
    expect(
      screen.getByText("Likely job-related · Interview · Rule score 95/100"),
    ).toBeDefined();
    expect(screen.getByText("Interview terminology was found")).toBeDefined();
  });

  it("marks a review as not job-related and removes it from visible results", async () => {
    const review = {
      gmailMessageId: "message-visa",
      gmailThreadId: "thread-visa",
      sender: "Visa updates <updates@example.com>",
      subject: "Application update",
      excerpt: "Your visa application is being processed.",
      receivedAt: "2026-07-19T14:30:00Z",
      newlyDiscovered: true,
      classification: "UNCERTAIN" as const,
      eventType: "APPLICATION" as const,
      confidenceScore: 53,
      classificationReason:
        "Only broad or conflicting application terminology was found",
      reviewId: 20,
      reviewStatus: "PENDING" as const,
      suggestedApplication: null,
      applicationDraft: { companyName: "Visa updates", roleTitle: "" },
      selectedApplicationId: null,
    };
    vi.mocked(getGoogleConnection).mockResolvedValue({
      connected: true,
      gmailAddress: "developer@example.com",
      connectedAt: "2026-07-20T10:00:00Z",
    });
    vi.mocked(listGmailReviews)
      .mockResolvedValueOnce([review])
      .mockResolvedValueOnce([review])
      .mockResolvedValue([]);
    vi.mocked(scanGmail).mockResolvedValue({
      scannedAt: "2026-07-20T10:05:00Z",
      candidatesFound: 1,
      newCandidatesFound: 1,
      candidates: [review],
    });
    vi.mocked(dismissGmailReview).mockResolvedValue({
      ...review,
      reviewStatus: "DISMISSED",
    });

    renderWithQuery(<GoogleConnectionPanel applications={[]} />);

    fireEvent.click(await screen.findByRole("button", { name: "Scan Gmail" }));
    fireEvent.click(screen.getByRole("button", { name: "Not job-related" }));

    await waitFor(() => {
      expect(dismissGmailReview).toHaveBeenCalledWith(20);
    });
    expect(
      screen.getByText(
        "There are no job-related messages to show from this scan.",
      ),
    ).toBeDefined();
  });

  it("confirms a suggested application from the review queue", async () => {
    const review = {
      gmailMessageId: "message-1",
      gmailThreadId: "thread-1",
      sender: "Acme Careers <careers@acme.example>",
      subject: "Software Engineer interview",
      excerpt:
        "We would like to arrange an interview with the engineering team.",
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
      applicationDraft: {
        companyName: "Acme",
        roleTitle: "Software Engineer",
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
    expect(
      screen.getByText(
        "We would like to arrange an interview with the engineering team.",
      ),
    ).toBeDefined();
    expect(
      screen.getByText(
        "Detected: Interview · Likely job-related · Rule score 95/100",
      ),
    ).toBeDefined();
    expect(
      (screen.getByLabelText("Application") as HTMLSelectElement).value,
    ).toBe("4");
    expect(
      (screen.getByLabelText("Event type") as HTMLSelectElement).value,
    ).toBe("INTERVIEW");
    expect(
      (screen.getByLabelText("Resulting status") as HTMLSelectElement).value,
    ).toBe("INTERVIEWING");

    fireEvent.change(screen.getByLabelText("Event type"), {
      target: { value: "OFFER" },
    });
    expect(
      (screen.getByLabelText("Resulting status") as HTMLSelectElement).value,
    ).toBe("OFFER");

    fireEvent.change(screen.getByLabelText("Resulting status"), {
      target: { value: "APPLIED" },
    });
    expect(
      (screen.getByLabelText("Resulting status") as HTMLSelectElement).value,
    ).toBe("APPLIED");
    fireEvent.click(screen.getByRole("button", { name: "Confirm match" }));

    await waitFor(() => {
      expect(matchGmailReview).toHaveBeenCalledWith(12, 4);
    });
  });

  it("creates an editable application from an unmatched review", async () => {
    const review = {
      gmailMessageId: "message-new",
      gmailThreadId: "thread-new",
      sender: "Acme Careers <careers@acme.example>",
      subject: "Software Engineer application",
      excerpt: "Thank you for applying for the Software Engineer role.",
      receivedAt: "2026-07-19T14:30:00Z",
      newlyDiscovered: false,
      classification: "JOB_RELATED" as const,
      eventType: "APPLICATION" as const,
      confidenceScore: 95,
      classificationReason:
        "Job application confirmation terminology was found",
      reviewId: 30,
      reviewStatus: "PENDING" as const,
      suggestedApplication: null,
      applicationDraft: {
        companyName: "Acme",
        roleTitle: "Software Engineer",
      },
      selectedApplicationId: null,
    };
    const application = {
      id: 9,
      companyName: "Acme Limited",
      roleTitle: "Senior Software Engineer",
      status: "APPLIED" as const,
      applicationDate: "2026-07-19",
      notes: "",
      lastActivityDate: "2026-07-19",
    };
    vi.mocked(getGoogleConnection).mockResolvedValue({
      connected: true,
      gmailAddress: "developer@example.com",
      connectedAt: "2026-07-20T10:00:00Z",
    });
    vi.mocked(listGmailReviews)
      .mockResolvedValueOnce([review])
      .mockResolvedValueOnce([]);
    vi.mocked(createApplicationFromGmailReview).mockResolvedValue({
      review: {
        ...review,
        reviewStatus: "MATCHED",
        selectedApplicationId: application.id,
      },
      application,
    });

    renderWithQuery(<GoogleConnectionPanel applications={[]} />);

    expect(
      (
        (await screen.findByLabelText(
          "Create a new application",
        )) as HTMLInputElement
      ).checked,
    ).toBe(true);
    expect((screen.getByLabelText("Company") as HTMLInputElement).value).toBe(
      "Acme",
    );
    expect((screen.getByLabelText("Role") as HTMLInputElement).value).toBe(
      "Software Engineer",
    );

    fireEvent.change(screen.getByLabelText("Company"), {
      target: { value: "Acme Limited" },
    });
    fireEvent.change(screen.getByLabelText("Role"), {
      target: { value: "Senior Software Engineer" },
    });
    fireEvent.click(screen.getByRole("button", { name: "Create application" }));

    await waitFor(() => {
      expect(createApplicationFromGmailReview).toHaveBeenCalledWith(30, {
        companyName: "Acme Limited",
        roleTitle: "Senior Software Engineer",
        status: "APPLIED",
        applicationDate: "2026-07-19",
        notes: "",
      });
    });
  });
});
