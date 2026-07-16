import Link from "next/link";
import { applicationStatusLabels } from "@/lib/applications-api";
import type { JobApplication } from "@/lib/applications-api";

type ApplicationListProps = {
  applications: JobApplication[];
  isPending: boolean;
  isError: boolean;
  onRetry: () => void;
};

export function ApplicationList({
  applications,
  isPending,
  isError,
  onRetry,
}: ApplicationListProps) {
  return (
    <section aria-labelledby="application-list-heading">
      <h2
        id="application-list-heading"
        className="text-xl font-semibold text-slate-950"
      >
        Your applications
      </h2>

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
        <ul className="mt-4 space-y-3">
          {applications.map((application) => (
            <li
              key={application.id}
              className="rounded-lg border border-slate-200 bg-white p-5"
            >
              <div className="flex items-start justify-between gap-4">
                <div>
                  <h3 className="font-semibold text-slate-950">
                    <Link
                      href={`/applications/${application.id}`}
                      className="hover:underline"
                    >
                      {application.roleTitle}
                    </Link>
                  </h3>
                  <p className="mt-1 text-sm text-slate-600">
                    {application.companyName}
                  </p>
                </div>
                <span className="rounded-full bg-slate-100 px-3 py-1 text-xs font-medium text-slate-700">
                  {applicationStatusLabels[application.status]}
                </span>
              </div>
              <p className="mt-4 text-sm text-slate-500">
                Applied {formatDate(application.applicationDate)}
              </p>
            </li>
          ))}
        </ul>
      )}
    </section>
  );
}

function formatDate(date: string) {
  return new Intl.DateTimeFormat("en-GB", { dateStyle: "medium" }).format(
    new Date(`${date}T00:00:00`),
  );
}
