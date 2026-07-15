import type { Metadata } from "next";
import "./globals.css";

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
      <body className="min-h-full flex flex-col">{children}</body>
    </html>
  );
}
