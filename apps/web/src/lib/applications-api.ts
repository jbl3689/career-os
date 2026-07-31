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

export const applicationSources = [
  "LinkedIn recruiter message",
  "LinkedIn job post",
  "Indeed",
  "Jack & Jill AI",
  "Welcome to the Jungle",
  "Company website",
  "Referral",
  "Other",
] as const;

export type JobApplication = {
  id: number;
  companyName: string;
  roleTitle: string;
  status: ApplicationStatus;
  applicationDate: string;
  source: string;
  notes: string;
  lastActivityDate: string;
};

export type CreateJobApplication = {
  companyName: string;
  roleTitle: string;
  status: ApplicationStatus;
  applicationDate: string;
  source: string;
  notes: string;
};

export type UpdateJobApplication = {
  status: ApplicationStatus;
  source: string;
  notes: string;
};

export function listApplications(): Promise<JobApplication[]> {
  return apiRequest<JobApplication[]>("/api/v1/applications");
}

export function getApplication(id: number): Promise<JobApplication> {
  return apiRequest<JobApplication>(`/api/v1/applications/${id}`);
}

export function createApplication(
  application: CreateJobApplication,
): Promise<JobApplication> {
  return apiRequest<JobApplication>("/api/v1/applications", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(application),
  });
}

export function updateApplication(
  id: number,
  application: UpdateJobApplication,
): Promise<JobApplication> {
  return apiRequest<JobApplication>(`/api/v1/applications/${id}`, {
    method: "PATCH",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(application),
  });
}
import { apiRequest } from "./api-client";

export { ApiRequestError } from "./api-client";
