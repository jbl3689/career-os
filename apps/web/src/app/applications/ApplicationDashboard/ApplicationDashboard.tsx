"use client";

import { useQuery } from "@tanstack/react-query";
import { applicationQueryKeys } from "@/lib/application-query-keys";
import { listApplications } from "@/lib/applications-api";
import { ApplicationList } from "./components/ApplicationList";
import { CreateApplicationForm } from "./components/CreateApplicationForm";

export function ApplicationDashboard() {
  const applicationsQuery = useQuery({
    queryKey: applicationQueryKeys.all,
    queryFn: listApplications,
  });

  return (
    <main className="mx-auto w-full max-w-5xl px-6 py-10">
      <header>
        <p className="text-sm font-semibold uppercase tracking-wide text-sky-700">
          Career OS
        </p>
        <h1 className="mt-2 text-3xl font-semibold text-slate-950">
          Job applications
        </h1>
        <p className="mt-2 text-slate-600">
          Keep a simple record of the roles you are pursuing.
        </p>
      </header>

      <div className="mt-8 grid gap-8 lg:grid-cols-[minmax(0,1fr)_22rem]">
        <ApplicationList
          applications={applicationsQuery.data ?? []}
          isPending={applicationsQuery.isPending}
          isError={applicationsQuery.isError}
          onRetry={() => applicationsQuery.refetch()}
        />
        <CreateApplicationForm />
      </div>
    </main>
  );
}
