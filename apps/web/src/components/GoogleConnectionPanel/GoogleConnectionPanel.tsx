"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  disconnectGoogleConnection,
  getGoogleConnection,
} from "@/lib/google-connection-api";
import { googleConnectionQueryKeys } from "@/lib/google-connection-query-keys";
import { listGmailReviews, scanGmail } from "@/lib/gmail-api";
import { gmailQueryKeys } from "@/lib/gmail-query-keys";
import type { JobApplication } from "@/lib/applications-api";
import { GmailScanResults } from "./components/GmailScanResults";
import { GmailReviewQueue } from "./components/GmailReviewQueue";

type GoogleConnectionPanelProps = {
  applications: JobApplication[];
};

export function GoogleConnectionPanel({
  applications,
}: GoogleConnectionPanelProps) {
  const queryClient = useQueryClient();
  const connectionQuery = useQuery({
    queryKey: googleConnectionQueryKeys.connection,
    queryFn: getGoogleConnection,
    retry: false,
  });
  const scanMutation = useMutation({
    mutationFn: scanGmail,
    onSuccess: () =>
      queryClient.invalidateQueries({ queryKey: gmailQueryKeys.reviews }),
  });
  const reviewsQuery = useQuery({
    queryKey: gmailQueryKeys.reviews,
    queryFn: listGmailReviews,
    enabled: connectionQuery.data?.connected === true,
  });
  const disconnectMutation = useMutation({
    mutationFn: disconnectGoogleConnection,
    onSuccess: () => {
      scanMutation.reset();
      queryClient.setQueryData(googleConnectionQueryKeys.connection, {
        connected: false,
        gmailAddress: null,
        connectedAt: null,
      });
    },
  });

  return (
    <section
      aria-labelledby="gmail-connection-heading"
      className="mt-8 rounded-lg border border-slate-200 bg-white p-5"
    >
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h2
            id="gmail-connection-heading"
            className="font-semibold text-slate-950"
          >
            Gmail connection
          </h2>
          {connectionQuery.isPending ? (
            <p className="mt-1 text-sm text-slate-500">
              Checking Gmail connection…
            </p>
          ) : connectionQuery.isError ? (
            <p className="mt-1 text-sm text-red-700">
              We could not check your Gmail connection.
            </p>
          ) : connectionQuery.data.connected ? (
            <p className="mt-1 text-sm text-slate-600">
              Connected as {connectionQuery.data.gmailAddress}
            </p>
          ) : (
            <p className="mt-1 text-sm text-slate-600">
              Optional. Connect the same Google account to allow read-only Gmail
              access during manual scans.
            </p>
          )}
        </div>

        {!connectionQuery.isPending && !connectionQuery.isError ? (
          connectionQuery.data.connected ? (
            <div className="flex flex-wrap gap-3">
              <button
                type="button"
                className="rounded-md bg-sky-700 px-4 py-2 text-sm font-medium text-white hover:bg-sky-800 disabled:bg-slate-400"
                disabled={
                  scanMutation.isPending || disconnectMutation.isPending
                }
                onClick={() => scanMutation.mutate()}
              >
                {scanMutation.isPending ? "Scanning…" : "Scan Gmail"}
              </button>
              <button
                type="button"
                className="rounded-md border border-slate-300 px-4 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50 disabled:text-slate-400"
                disabled={
                  disconnectMutation.isPending || scanMutation.isPending
                }
                onClick={() => disconnectMutation.mutate()}
              >
                {disconnectMutation.isPending
                  ? "Disconnecting…"
                  : "Disconnect Gmail"}
              </button>
            </div>
          ) : (
            <a
              href="/oauth2/authorization/google-gmail"
              className="self-start rounded-md bg-sky-700 px-4 py-2 text-sm font-medium text-white hover:bg-sky-800 sm:self-auto"
            >
              Connect Gmail
            </a>
          )
        ) : null}
      </div>

      {scanMutation.isError ? (
        <p className="mt-3 text-sm text-red-700">
          {scanMutation.error instanceof Error
            ? scanMutation.error.message
            : "Gmail could not be scanned. Please try again."}
        </p>
      ) : null}

      {connectionQuery.data?.connected ? (
        <GmailReviewQueue
          reviews={reviewsQuery.data ?? []}
          applications={applications}
          isPending={reviewsQuery.isPending}
          isError={reviewsQuery.isError}
        />
      ) : null}

      {scanMutation.data ? (
        <GmailScanResults candidates={scanMutation.data.candidates} />
      ) : null}

      {disconnectMutation.isError ? (
        <p className="mt-3 text-sm text-red-700">
          Gmail could not be disconnected. Please try again.
        </p>
      ) : null}
    </section>
  );
}
