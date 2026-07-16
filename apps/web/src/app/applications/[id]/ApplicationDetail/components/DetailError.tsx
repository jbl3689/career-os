import Link from "next/link";

export function DetailError({ message }: { message: string }) {
  return (
    <main className="mx-auto w-full max-w-3xl px-6 py-10">
      <div
        className="rounded-lg border border-rose-200 bg-rose-50 p-5"
        role="alert"
      >
        <p className="font-medium text-rose-900">{message}</p>
        <Link
          href="/applications"
          className="mt-3 inline-block text-sm font-medium text-rose-900 underline"
        >
          Back to applications
        </Link>
      </div>
    </main>
  );
}
