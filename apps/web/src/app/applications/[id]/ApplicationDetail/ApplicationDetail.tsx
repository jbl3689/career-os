"use client";

import { useQuery } from "@tanstack/react-query";
import Link from "next/link";
import { applicationQueryKeys } from "@/lib/application-query-keys";
import { ApiRequestError, getApplication } from "@/lib/applications-api";
import { ApplicationSummary } from "./components/ApplicationSummary";
import { DetailError } from "./components/DetailError";
import { EditApplicationForm } from "./components/EditApplicationForm";

export function ApplicationDetail({ applicationId }: { applicationId: number }) {
  const validId = Number.isSafeInteger(applicationId) && applicationId > 0;
  const applicationQuery = useQuery({
    queryKey: applicationQueryKeys.detail(applicationId),
    queryFn: () => getApplication(applicationId),
    enabled: validId,
  });

  if (!validId) {
    return <DetailError message="This application ID is invalid." />;
  }

  if (applicationQuery.isPending) {
    return (
      <main className="mx-auto w-full max-w-3xl px-6 py-10">
        <p className="text-slate-600" role="status">
          Loading application…
        </p>
      </main>
    );
  }

  if (applicationQuery.isError) {
    const notFound =
      applicationQuery.error instanceof ApiRequestError &&
      applicationQuery.error.status === 404;

    return (
      <DetailError
        message={
          notFound
            ? "This application could not be found."
            : "This application could not be loaded. Check that the API is running."
        }
      />
    );
  }

  const application = applicationQuery.data;

  return (
    <main className="mx-auto w-full max-w-3xl px-6 py-10">
      <Link
        href="/applications"
        className="text-sm font-medium text-sky-700 hover:underline"
      >
        ← Back to applications
      </Link>

      <article className="mt-5 rounded-lg border border-slate-200 bg-white p-6 sm:p-8">
        <ApplicationSummary application={application} />
        <EditApplicationForm application={application} />
      </article>
    </main>
  );
}
