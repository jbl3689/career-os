export const applicationStatuses = [
  "APPLIED",
  "INTERVIEWING",
  "OFFER",
  "REJECTED",
  "WITHDRAWN",
] as const;

export type ApplicationStatus = (typeof applicationStatuses)[number];

export const applicationStatusLabels: Record<ApplicationStatus, string> = {
  APPLIED: "Applied",
  INTERVIEWING: "Interviewing",
  OFFER: "Offer",
  REJECTED: "Rejected",
  WITHDRAWN: "Withdrawn",
};

export type JobApplication = {
  id: number;
  companyName: string;
  roleTitle: string;
  status: ApplicationStatus;
  applicationDate: string;
  notes: string;
  lastActivityDate: string;
};

export type CreateJobApplication = {
  companyName: string;
  roleTitle: string;
  status: ApplicationStatus;
  applicationDate: string;
  notes: string;
};

export type UpdateJobApplication = {
  status: ApplicationStatus;
  notes: string;
};

type ApiErrorBody = {
  error?: string;
  fieldErrors?: Record<string, string>;
};

export class ApiRequestError extends Error {
  constructor(
    message: string,
    readonly status: number,
    readonly fieldErrors: Record<string, string> = {},
  ) {
    super(message);
    this.name = "ApiRequestError";
  }
}

async function request<T>(path: string, options?: RequestInit): Promise<T> {
  const response = await fetch(path, {
    ...options,
    headers: {
      Accept: "application/json",
      ...options?.headers,
    },
    signal: AbortSignal.timeout(5_000),
  });

  if (!response.ok) {
    let errorBody: ApiErrorBody = {};

    try {
      errorBody = (await response.json()) as ApiErrorBody;
    } catch {
      // The status code still provides a useful fallback if the body is not JSON.
    }

    throw new ApiRequestError(
      errorBody.error ?? "The API request failed",
      response.status,
      errorBody.fieldErrors,
    );
  }

  return (await response.json()) as T;
}

export function listApplications(): Promise<JobApplication[]> {
  return request<JobApplication[]>("/api/v1/applications");
}

export function getApplication(id: number): Promise<JobApplication> {
  return request<JobApplication>(`/api/v1/applications/${id}`);
}

export function createApplication(
  application: CreateJobApplication,
): Promise<JobApplication> {
  return request<JobApplication>("/api/v1/applications", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(application),
  });
}

export function updateApplication(
  id: number,
  application: UpdateJobApplication,
): Promise<JobApplication> {
  return request<JobApplication>(`/api/v1/applications/${id}`, {
    method: "PATCH",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(application),
  });
}
