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
        Candidate messages ({candidates.length})
      </h3>
      <ul className="mt-3 divide-y divide-slate-200 border-y border-slate-200">
        {candidates.map((candidate) => (
          <li key={candidate.gmailMessageId} className="py-3">
            <p className="font-medium text-slate-950">
              {candidate.subject || "No subject"}
            </p>
            <p className="mt-1 text-sm text-slate-600">
              {candidate.sender || "Unknown sender"}
            </p>
            <p className="mt-1 text-xs text-slate-500">
              {formatReceivedAt(candidate.receivedAt)}
            </p>
          </li>
        ))}
      </ul>
      <p className="mt-3 text-xs text-slate-500">
        These results are not stored and have not changed any applications.
      </p>
    </div>
  );
}
