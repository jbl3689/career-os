"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { ApiRequestError } from "@/lib/api-client";
import { applicationQueryKeys } from "@/lib/application-query-keys";
import { getCurrentUser, logout } from "@/lib/auth-api";
import { authQueryKeys } from "@/lib/auth-query-keys";

export function AuthStatus() {
  const queryClient = useQueryClient();
  const currentUserQuery = useQuery({
    queryKey: authQueryKeys.currentUser,
    queryFn: getCurrentUser,
    retry: false,
  });
  const logoutMutation = useMutation({
    mutationFn: logout,
    onSuccess: () => {
      queryClient.removeQueries({ queryKey: applicationQueryKeys.all });
      queryClient.setQueryData(authQueryKeys.currentUser, null);
    },
  });

  if (currentUserQuery.isPending) {
    return <span className="text-sm text-slate-500">Checking session…</span>;
  }

  if (currentUserQuery.isError || currentUserQuery.data === null) {
    const signedOut =
      currentUserQuery.data === null ||
      currentUserQuery.error instanceof ApiRequestError &&
      currentUserQuery.error.status === 401;

    return signedOut ? (
      <a
        href="/oauth2/authorization/google"
        className="text-sm font-medium text-sky-700 hover:underline"
      >
        Sign in
      </a>
    ) : null;
  }

  return (
    <div className="flex items-center gap-3">
      <span className="hidden text-sm text-slate-600 sm:inline">
        {currentUserQuery.data.displayName ?? currentUserQuery.data.email}
      </span>
      <button
        type="button"
        className="text-sm font-medium text-sky-700 hover:underline disabled:text-slate-400"
        disabled={logoutMutation.isPending}
        onClick={() => logoutMutation.mutate()}
      >
        {logoutMutation.isPending ? "Signing out…" : "Sign out"}
      </button>
    </div>
  );
}
