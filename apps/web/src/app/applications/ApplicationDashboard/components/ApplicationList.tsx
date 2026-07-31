"use client";

import { useState } from "react";
import Link from "next/link";
import {
  applicationStatuses,
  applicationStatusLabels,
} from "@/lib/applications-api";
import type { JobApplication } from "@/lib/applications-api";

type ApplicationListProps = {
  applications: JobApplication[];
  isPending: boolean;
  isError: boolean;
  onRetry: () => void;
};

const closedApplicationStatuses = new Set(["REJECTED", "WITHDRAWN"]);

export function ApplicationList({
  applications,
  isPending,
  isError,
  onRetry,
}: ApplicationListProps) {
  const [showClosedApplications, setShowClosedApplications] = useState(false);
  const visibleStatuses = showClosedApplications
    ? applicationStatuses
    : applicationStatuses.filter(
        (status) => !closedApplicationStatuses.has(status),
      );

  return (
    <section aria-labelledby="application-list-heading">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <h2
          id="application-list-heading"
          className="text-xl font-semibold text-slate-950"
        >
          Your applications
        </h2>
        {!isPending && !isError && applications.length > 0 ? (
          <button
            type="button"
            className="rounded-md border border-slate-300 bg-white px-3 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50"
            aria-expanded={showClosedApplications}
            onClick={() => setShowClosedApplications((current) => !current)}
          >
            {showClosedApplications
              ? "Hide rejected and withdrawn"
              : "Show rejected and withdrawn"}
          </button>
        ) : null}
      </div>

      {isPending ? (
        <p className="mt-4 text-slate-600" role="status">
          Loading applications…
        </p>
      ) : isError ? (
        <div
          className="mt-4 rounded-lg border border-rose-200 bg-rose-50 p-4"
          role="alert"
        >
          <p className="font-medium text-rose-900">
            Applications could not be loaded.
          </p>
          <p className="mt-1 text-sm text-rose-800">
            Check that the Spring Boot API is running, then try again.
          </p>
          <button
            type="button"
            className="mt-3 text-sm font-medium text-rose-900 underline"
            onClick={onRetry}
          >
            Try again
          </button>
        </div>
      ) : applications.length === 0 ? (
        <div className="mt-4 rounded-lg border border-slate-200 bg-white p-6">
          <p className="font-medium text-slate-950">No applications yet</p>
          <p className="mt-1 text-sm text-slate-600">
            Add your first application using the form.
          </p>
        </div>
      ) : (
        <div className="mt-4 grid auto-cols-[minmax(16rem,1fr)] grid-flow-col gap-4 overflow-x-auto pb-4">
          {visibleStatuses.map((status) => {
            const applicationsForStatus = applications.filter(
              (application) => application.status === status,
            );

            return (
              <section
                key={status}
                aria-labelledby={`application-status-${status}`}
                className="rounded-lg bg-slate-100 p-3"
              >
                <div className="flex items-center justify-between gap-3 px-1">
                  <h3
                    id={`application-status-${status}`}
                    className="font-semibold text-slate-900"
                  >
                    {applicationStatusLabels[status]}
                  </h3>
                  <span
                    className="rounded-full bg-white px-2 py-0.5 text-xs font-medium text-slate-600"
                    aria-label={`${applicationsForStatus.length} applications`}
                  >
                    {applicationsForStatus.length}
                  </span>
                </div>

                {applicationsForStatus.length === 0 ? (
                  <p className="mt-3 rounded-lg border border-dashed border-slate-300 p-4 text-center text-sm text-slate-500">
                    No applications
                  </p>
                ) : (
                  <ul className="mt-3 space-y-3">
                    {applicationsForStatus.map((application) => (
                      <li
                        key={application.id}
                        className="rounded-lg border border-slate-200 bg-white p-4"
                      >
                        <h4 className="font-semibold text-slate-950">
                          <Link
                            href={`/applications/${application.id}`}
                            className="hover:underline"
                          >
                            {application.companyName}
                          </Link>
                        </h4>
                        <p className="mt-1 text-sm text-slate-600">
                          {application.roleTitle}
                        </p>
                        {application.source ? (
                          <p className="mt-2 text-xs text-slate-500">
                            {application.source}
                          </p>
                        ) : null}
                        <p className="mt-4 text-sm text-slate-500">
                          Applied {formatDate(application.applicationDate)}
                        </p>
                      </li>
                    ))}
                  </ul>
                )}
              </section>
            );
          })}
        </div>
      )}
    </section>
  );
}

function formatDate(date: string) {
  return new Intl.DateTimeFormat("en-GB", { dateStyle: "medium" }).format(
    new Date(`${date}T00:00:00`),
  );
}
