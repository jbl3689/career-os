import type {
  GmailClassification,
  GmailEventType,
} from "@/lib/gmail-api";

export const gmailClassificationLabels: Record<GmailClassification, string> = {
  JOB_RELATED: "Likely job-related",
  NOT_JOB_RELATED: "Not job-related",
  UNCERTAIN: "Needs review",
};

export const gmailEventTypeLabels: Record<GmailEventType, string> = {
  APPLICATION: "Application confirmation",
  INTERVIEW: "Interview",
  ASSESSMENT: "Assessment",
  OFFER: "Offer",
  REJECTION: "Rejection",
  RECRUITER_CONTACT: "Recruiter contact",
  UNKNOWN: "Unknown event",
};

export function formatGmailReceivedAt(value: string): string {
  return new Intl.DateTimeFormat("en-GB", {
    dateStyle: "medium",
    timeStyle: "short",
  }).format(new Date(value));
}
