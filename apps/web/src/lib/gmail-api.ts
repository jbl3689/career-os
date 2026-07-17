import { apiRequest } from "./api-client";

export type GmailCandidateMessage = {
  gmailMessageId: string;
  gmailThreadId: string;
  sender: string;
  subject: string;
  receivedAt: string;
};

export type GmailScanResult = {
  scannedAt: string;
  candidatesFound: number;
  candidates: GmailCandidateMessage[];
};

export function scanGmail(): Promise<GmailScanResult> {
  return apiRequest<GmailScanResult>("/api/v1/gmail/scan", {
    method: "POST",
    signal: AbortSignal.timeout(15_000),
  });
}
