import { applicationStatusLabels } from "@/lib/applications-api";
import type { JobApplication } from "@/lib/applications-api";
import { formatDate } from "../formatDate";

export function ApplicationSummary({
  application,
}: {
  application: JobApplication;
}) {
  return (
    <>
      <div className="flex flex-wrap items-start justify-between gap-4">
        <div>
          <p className="text-sm text-slate-600">{application.companyName}</p>
          <h1 className="mt-1 text-3xl font-semibold text-slate-950">
            {application.roleTitle}
          </h1>
        </div>
        <span className="rounded-full bg-slate-100 px-3 py-1 text-sm font-medium text-slate-700">
          {applicationStatusLabels[application.status]}
        </span>
      </div>

      <dl className="mt-6 grid gap-4 border-y border-slate-200 py-5 sm:grid-cols-2">
        <div>
          <dt className="text-sm font-medium text-slate-600">
            Application date
          </dt>
          <dd className="mt-1 text-slate-950">
            {formatDate(application.applicationDate)}
          </dd>
        </div>
        <div>
          <dt className="text-sm font-medium text-slate-600">Last activity</dt>
          <dd className="mt-1 text-slate-950">
            {formatDate(application.lastActivityDate)}
          </dd>
        </div>
        <div>
          <dt className="text-sm font-medium text-slate-600">
            Application source
          </dt>
          <dd className="mt-1 text-slate-950">
            {application.source || "Not specified"}
          </dd>
        </div>
      </dl>
    </>
  );
}
