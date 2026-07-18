import type { GmailCandidateMessage } from "@/lib/gmail-api";

type GmailScanResultsProps = {
  candidates: GmailCandidateMessage[];
};

function formatReceivedAt(value: string): string {
  return new Intl.DateTimeFormat("en-GB", {
    dateStyle: "medium",
    timeStyle: "short",
  }).format(new Date(value));
}

const classificationLabels = {
  JOB_RELATED: "Likely job-related",
  NOT_JOB_RELATED: "Not job-related",
  UNCERTAIN: "Needs review",
} as const;

const eventTypeLabels = {
  APPLICATION: "Application",
  INTERVIEW: "Interview",
  ASSESSMENT: "Assessment",
  OFFER: "Offer",
  REJECTION: "Rejection",
  RECRUITER_CONTACT: "Recruiter contact",
  UNKNOWN: "Unknown event",
} as const;

export function GmailScanResults({ candidates }: GmailScanResultsProps) {
  if (candidates.length === 0) {
    return (
      <p className="mt-4 text-sm text-slate-600">
        No candidate job messages were found in the current search window.
      </p>
    );
  }

  return (
    <div className="mt-5">
      <h3 className="text-sm font-semibold text-slate-950">
        Latest scan results ({candidates.length})
      </h3>
      <ul className="mt-3 divide-y divide-slate-200 border-y border-slate-200">
        {candidates.map((candidate) => (
          <li key={candidate.gmailMessageId} className="py-3">
            <div className="flex items-start justify-between gap-3">
              <p className="font-medium text-slate-950">
                {candidate.subject || "No subject"}
              </p>
              <span className="text-xs text-slate-500">
                {candidate.newlyDiscovered ? "New" : "Previously found"}
              </span>
            </div>
            <p className="mt-1 text-sm text-slate-600">
              {candidate.sender || "Unknown sender"}
            </p>
            <p className="mt-1 text-xs text-slate-500">
              {formatReceivedAt(candidate.receivedAt)}
            </p>
            <p className="mt-2 text-sm text-slate-700">
              {classificationLabels[candidate.classification]} ·{" "}
              {eventTypeLabels[candidate.eventType]} · Rule score{" "}
              {candidate.confidenceScore}/100
            </p>
            <p className="mt-1 text-xs text-slate-500">
              {candidate.classificationReason}
            </p>
            {candidate.suggestedApplication ? (
              <p className="mt-2 text-sm text-sky-800">
                Suggested match: {candidate.suggestedApplication.roleTitle} at{" "}
                {candidate.suggestedApplication.companyName}
              </p>
            ) : null}
          </li>
        ))}
      </ul>
      <p className="mt-3 text-xs text-slate-500">
        Candidate metadata is stored for classification and duplicate
        prevention. No applications have been changed.
      </p>
    </div>
  );
}
