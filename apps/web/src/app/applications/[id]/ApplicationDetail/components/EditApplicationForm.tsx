"use client";

import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import type { FormEvent } from "react";
import { ApplicationSourceSelect } from "@/components/ApplicationSourceSelect";
import { ApplicationStatusSelect } from "@/components/ApplicationStatusSelect";
import { applicationQueryKeys } from "@/lib/application-query-keys";
import {
  ApiRequestError,
  updateApplication,
} from "@/lib/applications-api";
import type {
  ApplicationStatus,
  JobApplication,
  UpdateJobApplication,
} from "@/lib/applications-api";

export function EditApplicationForm({
  application,
}: {
  application: JobApplication;
}) {
  const queryClient = useQueryClient();
  const [status, setStatus] = useState<ApplicationStatus>(application.status);
  const [source, setSource] = useState(application.source);
  const [notes, setNotes] = useState(application.notes);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const updateMutation = useMutation({
    mutationFn: (update: UpdateJobApplication) =>
      updateApplication(application.id, update),
    onSuccess: (updatedApplication) => {
      queryClient.setQueryData(
        applicationQueryKeys.detail(application.id),
        updatedApplication,
      );
      void queryClient.invalidateQueries({
        queryKey: applicationQueryKeys.all,
        exact: true,
      });
    },
  });

  async function handleUpdate(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setSuccessMessage(null);
    updateMutation.reset();

    try {
      await updateMutation.mutateAsync({ status, source, notes });
      setSuccessMessage("Application updated.");
    } catch {
      // The mutation error is rendered below the form.
    }
  }

  const fieldErrors =
    updateMutation.error instanceof ApiRequestError
      ? updateMutation.error.fieldErrors
      : {};

  return (
    <section className="mt-6" aria-labelledby="edit-application-heading">
      <h2
        id="edit-application-heading"
        className="text-xl font-semibold text-slate-950"
      >
        Update application
      </h2>

      <form onSubmit={handleUpdate} className="mt-4 space-y-4">
        <div>
          <ApplicationStatusSelect
            id="edit-status"
            value={status}
            onChange={setStatus}
          />
          {fieldErrors.status ? (
            <p className="mt-1 text-sm text-rose-700">{fieldErrors.status}</p>
          ) : null}
        </div>

        <div>
          <ApplicationSourceSelect
            id="edit-source"
            value={source}
            onChange={setSource}
          />
          {fieldErrors.source ? (
            <p className="mt-1 text-sm text-rose-700">{fieldErrors.source}</p>
          ) : null}
        </div>

        <div>
          <label
            htmlFor="edit-notes"
            className="block text-sm font-medium text-slate-700"
          >
            Notes
          </label>
          <textarea
            id="edit-notes"
            value={notes}
            onChange={(event) => setNotes(event.target.value)}
            rows={6}
            maxLength={5000}
            className="mt-1 w-full resize-y rounded-md border border-slate-300 px-3 py-2 text-slate-950"
          />
          {fieldErrors.notes ? (
            <p className="mt-1 text-sm text-rose-700">{fieldErrors.notes}</p>
          ) : null}
        </div>

        {updateMutation.isError ? (
          <p className="text-sm text-rose-700" role="alert">
            {updateMutation.error.message}
          </p>
        ) : null}
        {successMessage ? (
          <p className="text-sm text-emerald-700" role="status">
            {successMessage}
          </p>
        ) : null}

        <button
          type="submit"
          disabled={updateMutation.isPending}
          className="rounded-md bg-slate-900 px-4 py-2 font-medium text-white disabled:cursor-not-allowed disabled:opacity-60"
        >
          {updateMutation.isPending ? "Saving…" : "Save changes"}
        </button>
      </form>
    </section>
  );
}
