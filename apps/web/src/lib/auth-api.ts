import { apiRequest } from "./api-client";

export type CurrentUser = {
  id: number;
  email: string;
  displayName: string | null;
  avatarUrl: string | null;
};

export function getCurrentUser(): Promise<CurrentUser> {
  return apiRequest<CurrentUser>("/api/v1/auth/me");
}

export function logout(): Promise<void> {
  return apiRequest<void>("/api/v1/auth/logout", { method: "POST" });
}
