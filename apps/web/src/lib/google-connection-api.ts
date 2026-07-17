import { apiRequest } from "./api-client";

export type GoogleConnection = {
  connected: boolean;
  gmailAddress: string | null;
  connectedAt: string | null;
};

export function getGoogleConnection(): Promise<GoogleConnection> {
  return apiRequest<GoogleConnection>("/api/v1/google-connection");
}

export function disconnectGoogleConnection(): Promise<void> {
  return apiRequest<void>("/api/v1/google-connection", { method: "DELETE" });
}
