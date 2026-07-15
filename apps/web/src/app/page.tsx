type HealthResponse = {
  status: string;
  message: string;
};

type HealthResult =
  | { available: true; health: HealthResponse }
  | { available: false };

async function getApiHealth(): Promise<HealthResult> {
  const apiBaseUrl = process.env.API_BASE_URL ?? "http://localhost:8080";

  try {
    const response = await fetch(`${apiBaseUrl}/api/v1/health`, {
      cache: "no-store",
      signal: AbortSignal.timeout(3000),
    });

    if (!response.ok) {
      return { available: false };
    }

    const health = (await response.json()) as HealthResponse;
    return { available: true, health };
  } catch {
    return { available: false };
  }
}

export default async function Home() {
  const result = await getApiHealth();

  return (
    <main className="mx-auto flex min-h-screen w-full max-w-4xl items-center px-6 py-16 sm:px-10">
      <section className="w-full rounded-3xl border border-slate-200 bg-white p-8 shadow-sm sm:p-12">
        <p className="mb-3 text-sm font-semibold uppercase tracking-[0.2em] text-sky-700">
          Career OS
        </p>
        <h1 className="max-w-2xl text-4xl font-semibold tracking-tight text-slate-950 sm:text-6xl">
          Your career search, organised.
        </h1>
        <p className="mt-6 max-w-xl text-lg leading-8 text-slate-600">
          The application skeleton is ready. Job tracking arrives in the next
          stage, after the frontend and API connection are proven.
        </p>

        <div
          className={`mt-10 rounded-2xl border p-5 ${
            result.available
              ? "border-emerald-200 bg-emerald-50"
              : "border-rose-200 bg-rose-50"
          }`}
          role="status"
        >
          <div className="flex items-center gap-3">
            <span
              className={`h-3 w-3 rounded-full ${
                result.available ? "bg-emerald-500" : "bg-rose-500"
              }`}
              aria-hidden="true"
            />
            <h2 className="font-semibold text-slate-950">
              {result.available ? "API connected" : "API unavailable"}
            </h2>
          </div>
          <p className="mt-2 pl-6 text-sm leading-6 text-slate-700">
            {result.available
              ? result.health.message
              : "Start the Spring Boot API on port 8080, then refresh this page."}
          </p>
        </div>
      </section>
    </main>
  );
}
