import { apiRequest } from "./api-client";

export type GmailClassification =
  | "JOB_RELATED"
  | "NOT_JOB_RELATED"
  | "UNCERTAIN";

export type GmailEventType =
  | "APPLICATION"
  | "INTERVIEW"
  | "ASSESSMENT"
  | "OFFER"
  | "REJECTION"
  | "RECRUITER_CONTACT"
  | "UNKNOWN";

export type GmailCandidateMessage = {
  gmailMessageId: string;
  gmailThreadId: string;
  sender: string;
  subject: string;
  receivedAt: string;
  newlyDiscovered: boolean;
  classification: GmailClassification;
  eventType: GmailEventType;
  confidenceScore: number;
  classificationReason: string;
  reviewId: number;
  reviewStatus: "PENDING" | "MATCHED" | "DISMISSED";
  suggestedApplication: GmailApplicationSuggestion | null;
  selectedApplicationId: number | null;
};

export type GmailApplicationSuggestion = {
  applicationId: number;
  companyName: string;
  roleTitle: string;
  confidenceScore: number;
  reason: string;
};

export type GmailScanResult = {
  scannedAt: string;
  candidatesFound: number;
  newCandidatesFound: number;
  candidates: GmailCandidateMessage[];
};

export function scanGmail(): Promise<GmailScanResult> {
  return apiRequest<GmailScanResult>("/api/v1/gmail/scan", {
    method: "POST",
    signal: AbortSignal.timeout(15_000),
  });
}

export function listGmailReviews(): Promise<GmailCandidateMessage[]> {
  return apiRequest<GmailCandidateMessage[]>("/api/v1/gmail/reviews");
}

export function matchGmailReview(
  reviewId: number,
  applicationId: number,
): Promise<GmailCandidateMessage> {
  return apiRequest<GmailCandidateMessage>(
    `/api/v1/gmail/reviews/${reviewId}/match`,
    {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ applicationId }),
    },
  );
}

export function dismissGmailReview(
  reviewId: number,
): Promise<GmailCandidateMessage> {
  return apiRequest<GmailCandidateMessage>(
    `/api/v1/gmail/reviews/${reviewId}/dismiss`,
    { method: "POST" },
  );
}
