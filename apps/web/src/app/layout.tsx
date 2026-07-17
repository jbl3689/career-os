import type { Metadata } from "next";
import Link from "next/link";
import { AuthStatus } from "@/components/AuthStatus";
import "./globals.css";
import { Providers } from "./Providers";

export const metadata: Metadata = {
  title: "Career OS",
  description: "Keep your job search organised.",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en" className="h-full antialiased">
      <body className="flex min-h-full flex-col">
        <Providers>
          <header className="border-b border-slate-200 bg-white">
            <nav className="mx-auto flex w-full max-w-5xl items-center justify-between px-6 py-4">
              <Link
                href="/applications"
                className="font-semibold text-slate-950"
              >
                Career OS
              </Link>
              <div className="flex items-center gap-5">
                <Link
                  href="/applications"
                  className="text-sm text-slate-600 hover:text-slate-950"
                >
                  Applications
                </Link>
                <AuthStatus />
              </div>
            </nav>
          </header>
          {children}
        </Providers>
      </body>
    </html>
  );
}
