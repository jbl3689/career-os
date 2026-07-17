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
