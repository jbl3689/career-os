"use client";

import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useRef, useState } from "react";
import type { FormEvent } from "react";
import { ApplicationSourceSelect } from "@/components/ApplicationSourceSelect";
import { ApplicationStatusSelect } from "@/components/ApplicationStatusSelect";
import { applicationQueryKeys } from "@/lib/application-query-keys";
import {
  ApiRequestError,
  createApplication,
} from "@/lib/applications-api";
import type {
  ApplicationStatus,
  CreateJobApplication,
  JobApplication,
} from "@/lib/applications-api";
import { FieldError, FormField } from "./FormField";

export function CreateApplicationForm() {
  const queryClient = useQueryClient();
  const formRef = useRef<HTMLFormElement>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const createMutation = useMutation({
    mutationFn: (application: CreateJobApplication) =>
      createApplication(application),
    onSuccess: (createdApplication) => {
      queryClient.setQueryData<JobApplication[]>(
        applicationQueryKeys.all,
        (applications = []) => [...applications, createdApplication],
      );
    },
  });

  async function handleCreate(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setSuccessMessage(null);
    createMutation.reset();

    const formData = new FormData(event.currentTarget);
    const application: CreateJobApplication = {
      companyName: String(formData.get("companyName") ?? ""),
      roleTitle: String(formData.get("roleTitle") ?? ""),
      status: String(formData.get("status")) as ApplicationStatus,
      applicationDate: String(formData.get("applicationDate") ?? ""),
      source: String(formData.get("source") ?? ""),
      notes: String(formData.get("notes") ?? ""),
    };

    try {
      const createdApplication = await createMutation.mutateAsync(application);
      formRef.current?.reset();
      setSuccessMessage(`${createdApplication.roleTitle} was added.`);
    } catch {
      // The mutation error is rendered below the form.
    }
  }

  const fieldErrors =
    createMutation.error instanceof ApiRequestError
      ? createMutation.error.fieldErrors
      : {};

  return (
    <section
      className="rounded-lg border border-slate-200 bg-white p-6"
      aria-labelledby="create-application-heading"
    >
      <h2
        id="create-application-heading"
        className="text-xl font-semibold text-slate-950"
      >
        Add an application
      </h2>

      <form ref={formRef} onSubmit={handleCreate} className="mt-5 space-y-4">
        <FormField
          id="companyName"
          label="Company name"
          error={fieldErrors.companyName}
        />
        <FormField
          id="roleTitle"
          label="Role title"
          error={fieldErrors.roleTitle}
        />

        <div>
          <ApplicationStatusSelect
            id="status"
            name="status"
            defaultValue="APPLIED"
          />
          <FieldError message={fieldErrors.status} />
        </div>

        <div>
          <ApplicationSourceSelect id="source" name="source" />
          <FieldError message={fieldErrors.source} />
        </div>

        <div>
          <label
            htmlFor="applicationDate"
            className="block text-sm font-medium text-slate-700"
          >
            Application date
          </label>
          <input
            id="applicationDate"
            name="applicationDate"
            type="date"
            required
            className="mt-1 w-full rounded-md border border-slate-300 px-3 py-2 text-slate-950"
          />
          <FieldError message={fieldErrors.applicationDate} />
        </div>

        <div>
          <label
            htmlFor="notes"
            className="block text-sm font-medium text-slate-700"
          >
            Notes
          </label>
          <textarea
            id="notes"
            name="notes"
            rows={4}
            maxLength={5000}
            className="mt-1 w-full resize-y rounded-md border border-slate-300 px-3 py-2 text-slate-950"
          />
          <FieldError message={fieldErrors.notes} />
        </div>

        {createMutation.isError ? (
          <p className="text-sm text-rose-700" role="alert">
            {createMutation.error.message}
          </p>
        ) : null}
        {successMessage ? (
          <p className="text-sm text-emerald-700" role="status">
            {successMessage}
          </p>
        ) : null}

        <button
          type="submit"
          disabled={createMutation.isPending}
          className="w-full rounded-md bg-slate-900 px-4 py-2 font-medium text-white disabled:cursor-not-allowed disabled:opacity-60"
        >
          {createMutation.isPending ? "Adding…" : "Add application"}
        </button>
      </form>
    </section>
  );
}
