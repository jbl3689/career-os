import type { GmailCandidateMessage } from "@/lib/gmail-api";
import {
  formatGmailReceivedAt,
  gmailClassificationLabels,
  gmailEventTypeLabels,
} from "../GmailDisplay";

type GmailScanResultsProps = {
  candidates: GmailCandidateMessage[];
};

export function GmailScanResults({ candidates }: GmailScanResultsProps) {
  if (candidates.length === 0) {
    return (
      <p className="mt-4 text-sm text-slate-600">
        There are no job-related messages to show from this scan.
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
              {formatGmailReceivedAt(candidate.receivedAt)}
            </p>
            {candidate.excerpt ? (
              <blockquote className="mt-2 border-l-2 border-slate-200 pl-3 text-sm text-slate-600">
                {candidate.excerpt}
              </blockquote>
            ) : null}
            <p className="mt-2 text-sm text-slate-700">
              {gmailClassificationLabels[candidate.classification]} ·{" "}
              {gmailEventTypeLabels[candidate.eventType]} · Rule score{" "}
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
        Candidate metadata and a short Gmail excerpt are stored for
        classification and duplicate prevention. Full email bodies and
        attachments are not stored. No applications have been changed.
      </p>
    </div>
  );
}
