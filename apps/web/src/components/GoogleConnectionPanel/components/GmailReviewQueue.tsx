"use client";

import { useState } from "react";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import type { JobApplication } from "@/lib/applications-api";
import {
  dismissGmailReview,
  matchGmailReview,
} from "@/lib/gmail-api";
import type { GmailCandidateMessage } from "@/lib/gmail-api";
import { gmailQueryKeys } from "@/lib/gmail-query-keys";

type GmailReviewQueueProps = {
  reviews: GmailCandidateMessage[];
  applications: JobApplication[];
  isPending: boolean;
  isError: boolean;
};

export function GmailReviewQueue({
  reviews,
  applications,
  isPending,
  isError,
}: GmailReviewQueueProps) {
  return (
    <div className="mt-6">
      <h3 className="text-sm font-semibold text-slate-950">
        Waiting for review{reviews.length > 0 ? ` (${reviews.length})` : ""}
      </h3>
      <p className="mt-1 text-xs text-slate-500">
        Confirm where each message belongs or dismiss it. This does not change
        an application.
      </p>
      {isPending ? (
        <p className="mt-3 text-sm text-slate-500" role="status">
          Loading Gmail reviews…
        </p>
      ) : isError ? (
        <p className="mt-3 text-sm text-red-700">
          Gmail reviews could not be loaded. Restart the API and try again.
        </p>
      ) : reviews.length === 0 ? (
        <p className="mt-3 text-sm text-slate-600">
          There are no Gmail messages waiting for review.
        </p>
      ) : (
        <ul className="mt-3 space-y-3">
          {reviews.map((review) => (
            <GmailReviewItem
              key={review.reviewId}
              review={review}
              applications={applications}
            />
          ))}
        </ul>
      )}
    </div>
  );
}

function GmailReviewItem({
  review,
  applications,
}: {
  review: GmailCandidateMessage;
  applications: JobApplication[];
}) {
  const queryClient = useQueryClient();
  const suggestedId = review.suggestedApplication?.applicationId;
  const [applicationId, setApplicationId] = useState(
    suggestedId?.toString() ?? "",
  );
  const removeFromQueue = () =>
    queryClient.invalidateQueries({ queryKey: gmailQueryKeys.reviews });
  const matchMutation = useMutation({
    mutationFn: () => matchGmailReview(review.reviewId, Number(applicationId)),
    onSuccess: removeFromQueue,
  });
  const dismissMutation = useMutation({
    mutationFn: () => dismissGmailReview(review.reviewId),
    onSuccess: removeFromQueue,
  });
  const isSaving = matchMutation.isPending || dismissMutation.isPending;

  return (
    <li className="rounded-md border border-slate-200 p-4">
      <p className="font-medium text-slate-950">
        {review.subject || "No subject"}
      </p>
      <p className="mt-1 text-sm text-slate-600">
        {review.sender || "Unknown sender"}
      </p>
      {review.suggestedApplication ? (
        <p className="mt-2 text-sm text-sky-800">
          Suggested: {review.suggestedApplication.roleTitle} at{" "}
          {review.suggestedApplication.companyName} · Match score{" "}
          {review.suggestedApplication.confidenceScore}/100
        </p>
      ) : (
        <p className="mt-2 text-sm text-amber-800">
          No reliable existing-application match. If this is a new role, add it
          manually and then select it here.
        </p>
      )}
      {review.suggestedApplication ? (
        <p className="mt-1 text-xs text-slate-500">
          {review.suggestedApplication.reason}
        </p>
      ) : null}
      <label
        className="mt-3 block text-sm font-medium text-slate-700"
        htmlFor={`gmail-review-${review.reviewId}`}
      >
        Application
      </label>
      <select
        id={`gmail-review-${review.reviewId}`}
        value={applicationId}
        onChange={(event) => setApplicationId(event.target.value)}
        disabled={isSaving}
        className="mt-1 w-full rounded-md border border-slate-300 bg-white px-3 py-2 text-sm"
      >
        <option value="">Choose an application</option>
        {applications.map((application) => (
          <option key={application.id} value={application.id}>
            {application.roleTitle} at {application.companyName}
          </option>
        ))}
      </select>
      <div className="mt-3 flex gap-3">
        <button
          type="button"
          disabled={!applicationId || isSaving}
          onClick={() => matchMutation.mutate()}
          className="rounded-md bg-sky-700 px-3 py-2 text-sm font-medium text-white disabled:bg-slate-400"
        >
          {matchMutation.isPending ? "Confirming…" : "Confirm match"}
        </button>
        <button
          type="button"
          disabled={isSaving}
          onClick={() => dismissMutation.mutate()}
          className="rounded-md border border-slate-300 px-3 py-2 text-sm font-medium text-slate-700 disabled:text-slate-400"
        >
          {dismissMutation.isPending ? "Dismissing…" : "Dismiss"}
        </button>
      </div>
      {matchMutation.isError || dismissMutation.isError ? (
        <p className="mt-2 text-sm text-red-700">
          This review could not be saved. Please try again.
        </p>
      ) : null}
    </li>
  );
}
