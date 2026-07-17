type ApiErrorBody = {
  error?: string;
  fieldErrors?: Record<string, string>;
};

type CsrfResponse = {
  token: string;
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

async function readError(response: Response): Promise<ApiRequestError> {
  let errorBody: ApiErrorBody = {};

  try {
    errorBody = (await response.json()) as ApiErrorBody;
  } catch {
    // The status code still provides a useful fallback if the body is not JSON.
  }

  return new ApiRequestError(
    errorBody.error ?? "The API request failed",
    response.status,
    errorBody.fieldErrors,
  );
}

async function getCsrfToken(): Promise<string> {
  const response = await fetch("/api/v1/auth/csrf", {
    headers: { Accept: "application/json" },
    signal: AbortSignal.timeout(5_000),
  });

  if (!response.ok) {
    throw await readError(response);
  }

  const body = (await response.json()) as CsrfResponse;
  return body.token;
}

function changesServerState(method: string | undefined): boolean {
  return !["GET", "HEAD", "OPTIONS"].includes(method?.toUpperCase() ?? "GET");
}

export async function apiRequest<T>(
  path: string,
  options?: RequestInit,
): Promise<T> {
  const csrfToken = changesServerState(options?.method)
    ? await getCsrfToken()
    : undefined;
  const response = await fetch(path, {
    ...options,
    headers: {
      Accept: "application/json",
      ...(csrfToken ? { "X-XSRF-TOKEN": csrfToken } : {}),
      ...options?.headers,
    },
    signal: AbortSignal.timeout(5_000),
  });

  if (!response.ok) {
    throw await readError(response);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return (await response.json()) as T;
}
