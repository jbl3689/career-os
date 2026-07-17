"use client";

import { useQuery } from "@tanstack/react-query";
import { ApiRequestError } from "@/lib/api-client";
import { getCurrentUser } from "@/lib/auth-api";
import { authQueryKeys } from "@/lib/auth-query-keys";

export function RequireAuthentication({
  children,
}: {
  children: React.ReactNode;
}) {
  const currentUserQuery = useQuery({
    queryKey: authQueryKeys.currentUser,
    queryFn: getCurrentUser,
    retry: false,
  });

  if (currentUserQuery.isPending) {
    return (
      <main className="mx-auto w-full max-w-5xl px-6 py-10">
        <p className="text-slate-600" role="status">
          Checking your session…
        </p>
      </main>
    );
  }

  if (currentUserQuery.isError || currentUserQuery.data === null) {
    const signedOut =
      currentUserQuery.data === null ||
      currentUserQuery.error instanceof ApiRequestError &&
      currentUserQuery.error.status === 401;

    return (
      <main className="mx-auto w-full max-w-5xl px-6 py-10">
        <h1 className="text-3xl font-semibold text-slate-950">
          {signedOut ? "Sign in to Career OS" : "Authentication unavailable"}
        </h1>
        <p className="mt-3 max-w-xl text-slate-600">
          {signedOut
            ? "Use your Google account to access your private job application tracker."
            : "Career OS could not check your session. Confirm that the API is running and try again."}
        </p>
        {signedOut ? (
          <a
            href="/oauth2/authorization/google"
            className="mt-6 inline-flex rounded-md bg-slate-950 px-4 py-2 text-sm font-medium text-white hover:bg-slate-800"
          >
            Continue with Google
          </a>
        ) : null}
      </main>
    );
  }

  return children;
}
