"use client";

import { useState } from "react";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import {
  applicationStatusLabels,
  applicationStatuses,
} from "@/lib/applications-api";
import { applicationQueryKeys } from "@/lib/application-query-keys";
import type {
  ApplicationStatus,
  JobApplication,
} from "@/lib/applications-api";
import {
  createApplicationFromGmailReview,
  dismissGmailReview,
  gmailEventTypes,
  matchGmailReview,
} from "@/lib/gmail-api";
import type {
  GmailCandidateMessage,
  GmailEventType,
} from "@/lib/gmail-api";
import { gmailQueryKeys } from "@/lib/gmail-query-keys";
import {
  formatGmailReceivedAt,
  gmailClassificationLabels,
  gmailEventTypeLabels,
} from "../GmailDisplay";

type GmailReviewQueueProps = {
  reviews: GmailCandidateMessage[];
  applications: JobApplication[];
  isPending: boolean;
  isError: boolean;
  onMarkedNotJobRelated: (reviewId: number) => void;
};

export function GmailReviewQueue({
  reviews,
  applications,
  isPending,
  isError,
  onMarkedNotJobRelated,
}: GmailReviewQueueProps) {
  return (
    <div className="mt-6">
      <h3 className="text-sm font-semibold text-slate-950">
        Waiting for review{reviews.length > 0 ? ` (${reviews.length})` : ""}
      </h3>
      <p className="mt-1 text-xs text-slate-500">
        Confirm where each message belongs or mark it as not job-related. This
        does not change an application.
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
              onMarkedNotJobRelated={onMarkedNotJobRelated}
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
  onMarkedNotJobRelated,
}: {
  review: GmailCandidateMessage;
  applications: JobApplication[];
  onMarkedNotJobRelated: (reviewId: number) => void;
}) {
  const queryClient = useQueryClient();
  const suggestedId = review.suggestedApplication?.applicationId;
  const [applicationId, setApplicationId] = useState(
    suggestedId?.toString() ?? "new",
  );
  const [companyName, setCompanyName] = useState(
    review.applicationDraft.companyName,
  );
  const [roleTitle, setRoleTitle] = useState(review.applicationDraft.roleTitle);
  const [applicationDate, setApplicationDate] = useState(
    review.receivedAt.slice(0, 10),
  );
  const [eventType, setEventType] = useState(review.eventType);
  const [resultingStatus, setResultingStatus] = useState<ApplicationStatus>(
    getSuggestedStatus(review.eventType, suggestedId, applications),
  );
  const removeFromQueue = () =>
    queryClient.invalidateQueries({ queryKey: gmailQueryKeys.reviews });
  const matchMutation = useMutation({
    mutationFn: () => matchGmailReview(review.reviewId, Number(applicationId)),
    onSuccess: removeFromQueue,
  });
  const createMutation = useMutation({
    mutationFn: () =>
      createApplicationFromGmailReview(review.reviewId, {
        companyName,
        roleTitle,
        status: resultingStatus,
        applicationDate,
        notes: "",
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: applicationQueryKeys.all });
      return removeFromQueue();
    },
  });
  const dismissMutation = useMutation({
    mutationFn: () => dismissGmailReview(review.reviewId),
    onSuccess: (dismissedReview) => {
      onMarkedNotJobRelated(dismissedReview.reviewId);
      return removeFromQueue();
    },
  });
  const isCreating = applicationId === "new";
  const isSaving =
    matchMutation.isPending ||
    createMutation.isPending ||
    dismissMutation.isPending;

  return (
    <li className="rounded-md border border-slate-200 p-4">
      <p className="font-medium text-slate-950">
        {review.subject || "No subject"}
      </p>
      <p className="mt-1 text-sm text-slate-600">
        {review.sender || "Unknown sender"}
      </p>
      <p className="mt-1 text-xs text-slate-500">
        {formatGmailReceivedAt(review.receivedAt)}
      </p>
      {review.excerpt ? (
        <blockquote className="mt-3 border-l-2 border-slate-200 pl-3 text-sm text-slate-600">
          {review.excerpt}
        </blockquote>
      ) : (
        <p className="mt-3 text-sm text-slate-500">
          No message excerpt was available.
        </p>
      )}
      <p className="mt-3 text-sm font-medium text-slate-800">
        Detected: {gmailEventTypeLabels[review.eventType]} ·{" "}
        {gmailClassificationLabels[review.classification]} · Rule score{" "}
        {review.confidenceScore}/100
      </p>
      <p className="mt-1 text-xs text-slate-500">
        {review.classificationReason}
      </p>
      {review.suggestedApplication ? (
        <p className="mt-2 text-sm text-sky-800">
          Suggested: {review.suggestedApplication.roleTitle} at{" "}
          {review.suggestedApplication.companyName} · Match score{" "}
          {review.suggestedApplication.confidenceScore}/100
        </p>
      ) : (
        <p className="mt-2 text-sm text-amber-800">
          No reliable existing-application match. Review the proposed new
          application below.
        </p>
      )}
      {review.suggestedApplication ? (
        <p className="mt-1 text-xs text-slate-500">
          {review.suggestedApplication.reason}
        </p>
      ) : null}
      <fieldset className="mt-3" disabled={isSaving}>
        <legend className="text-sm font-medium text-slate-700">
          Application action
        </legend>
        <div className="mt-2 flex flex-wrap gap-4 text-sm text-slate-700">
          <label className="flex items-center gap-2">
            <input
              type="radio"
              name={`gmail-review-action-${review.reviewId}`}
              checked={!isCreating}
              onChange={() =>
                setApplicationId(suggestedId?.toString() ?? "")
              }
            />
            Use an existing application
          </label>
          <label className="flex items-center gap-2">
            <input
              type="radio"
              name={`gmail-review-action-${review.reviewId}`}
              checked={isCreating}
              onChange={() => {
                setApplicationId("new");
                setResultingStatus(
                  getSuggestedStatus(eventType, undefined, applications),
                );
              }}
            />
            Create a new application
          </label>
        </div>
      </fieldset>
      {!isCreating ? (
        <>
          <label
            className="mt-3 block text-sm font-medium text-slate-700"
            htmlFor={`gmail-review-${review.reviewId}`}
          >
            Application
          </label>
          <select
            id={`gmail-review-${review.reviewId}`}
            value={applicationId}
            onChange={(event) => {
              const nextApplicationId = event.target.value;
              setApplicationId(nextApplicationId);
              setResultingStatus(
                getSuggestedStatus(
                  eventType,
                  nextApplicationId ? Number(nextApplicationId) : undefined,
                  applications,
                ),
              );
            }}
            disabled={isSaving}
            className="mt-1 w-full rounded-md border border-slate-300 bg-white px-3 py-2 text-sm"
          >
            <option value="">Choose an existing application</option>
            {applications.map((application) => (
              <option key={application.id} value={application.id}>
                {application.roleTitle} at {application.companyName}
              </option>
            ))}
          </select>
        </>
      ) : null}
      {isCreating ? (
        <div className="mt-3 rounded-md border border-slate-200 bg-slate-50 p-3">
          <p className="text-sm font-medium text-slate-800">
            Proposed new application
          </p>
          <p className="mt-1 text-xs text-slate-500">
            These are rule-based guesses. Check them before confirming.
          </p>
          <label
            className="mt-3 block text-sm font-medium text-slate-700"
            htmlFor={`gmail-review-company-${review.reviewId}`}
          >
            Company
          </label>
          <input
            id={`gmail-review-company-${review.reviewId}`}
            value={companyName}
            onChange={(event) => setCompanyName(event.target.value)}
            disabled={isSaving}
            className="mt-1 w-full rounded-md border border-slate-300 bg-white px-3 py-2 text-sm"
          />
          <label
            className="mt-3 block text-sm font-medium text-slate-700"
            htmlFor={`gmail-review-role-${review.reviewId}`}
          >
            Role
          </label>
          <input
            id={`gmail-review-role-${review.reviewId}`}
            value={roleTitle}
            onChange={(event) => setRoleTitle(event.target.value)}
            disabled={isSaving}
            className="mt-1 w-full rounded-md border border-slate-300 bg-white px-3 py-2 text-sm"
          />
          <label
            className="mt-3 block text-sm font-medium text-slate-700"
            htmlFor={`gmail-review-date-${review.reviewId}`}
          >
            Application date
          </label>
          <input
            id={`gmail-review-date-${review.reviewId}`}
            type="date"
            value={applicationDate}
            onChange={(event) => setApplicationDate(event.target.value)}
            disabled={isSaving}
            className="mt-1 w-full rounded-md border border-slate-300 bg-white px-3 py-2 text-sm"
          />
        </div>
      ) : null}
      <label
        className="mt-3 block text-sm font-medium text-slate-700"
        htmlFor={`gmail-review-event-${review.reviewId}`}
      >
        Event type
      </label>
      <select
        id={`gmail-review-event-${review.reviewId}`}
        value={eventType}
        onChange={(event) => {
          const nextEventType = event.target.value as GmailEventType;
          setEventType(nextEventType);
          setResultingStatus(
            getSuggestedStatus(
              nextEventType,
              applicationId ? Number(applicationId) : undefined,
              applications,
            ),
          );
        }}
        disabled={isSaving}
        className="mt-1 w-full rounded-md border border-slate-300 bg-white px-3 py-2 text-sm"
      >
        {gmailEventTypes.map((type) => (
          <option key={type} value={type}>
            {gmailEventTypeLabels[type]}
          </option>
        ))}
      </select>
      <label
        className="mt-3 block text-sm font-medium text-slate-700"
        htmlFor={`gmail-review-status-${review.reviewId}`}
      >
        Resulting status
      </label>
      <select
        id={`gmail-review-status-${review.reviewId}`}
        value={resultingStatus}
        onChange={(event) =>
          setResultingStatus(event.target.value as ApplicationStatus)
        }
        disabled={isSaving}
        className="mt-1 w-full rounded-md border border-slate-300 bg-white px-3 py-2 text-sm"
      >
        {applicationStatuses.map((status) => (
          <option key={status} value={status}>
            {applicationStatusLabels[status]}
          </option>
        ))}
      </select>
      <p className="mt-2 text-xs text-slate-500">
        Creating a new application uses the selected status. Email-sourced
        timeline events are part of the next slice.
      </p>
      <div className="mt-3 flex gap-3">
        <button
          type="button"
          disabled={
            !applicationId ||
            (isCreating &&
              (!companyName.trim() || !roleTitle.trim() || !applicationDate)) ||
            isSaving
          }
          onClick={() =>
            isCreating ? createMutation.mutate() : matchMutation.mutate()
          }
          className="rounded-md bg-sky-700 px-3 py-2 text-sm font-medium text-white disabled:bg-slate-400"
        >
          {matchMutation.isPending || createMutation.isPending
            ? "Confirming…"
            : isCreating
              ? "Create application"
              : "Confirm match"}
        </button>
        <button
          type="button"
          disabled={isSaving}
          onClick={() => dismissMutation.mutate()}
          className="rounded-md border border-slate-300 px-3 py-2 text-sm font-medium text-slate-700 disabled:text-slate-400"
        >
          {dismissMutation.isPending
            ? "Removing…"
            : "Not job-related"}
        </button>
      </div>
      {matchMutation.isError || createMutation.isError || dismissMutation.isError ? (
        <p className="mt-2 text-sm text-red-700">
          This review could not be saved. Please try again.
        </p>
      ) : null}
    </li>
  );
}

function getSuggestedStatus(
  eventType: GmailEventType,
  applicationId: number | undefined,
  applications: JobApplication[],
): ApplicationStatus {
  const eventStatus: Partial<Record<GmailEventType, ApplicationStatus>> = {
    APPLICATION: "APPLIED",
    INTERVIEW: "INTERVIEWING",
    OFFER: "OFFER",
    REJECTION: "REJECTED",
  };

  return (
    eventStatus[eventType] ??
    applications.find((application) => application.id === applicationId)
      ?.status ??
    "APPLIED"
  );
}
