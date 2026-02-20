import type { Metadata } from "next";
import "./globals.css";
import { NavHeader } from "@/components/NavHeader";

export const metadata: Metadata = {
  title: "T20 World Cup 2026 - Ticket Booking",
  description: "2026 T20 Cricket World Cup Ticket Booking System",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body className="antialiased">
        <div className="min-h-screen bg-zinc-50">
          <NavHeader />
          <main className="max-w-7xl mx-auto px-4 py-8">
            {children}
          </main>
          <footer className="bg-zinc-900 text-zinc-400 text-center py-4 text-xs mt-8">
            2026 T20 Cricket World Cup Ticket Booking System
          </footer>
        </div>
      </body>
    </html>
  );
}
