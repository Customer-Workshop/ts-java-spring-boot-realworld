"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { Button } from "./ui/button";
import { Trophy, Ticket, Users } from "lucide-react";

export function NavHeader() {
  const pathname = usePathname();
  const isMatches = pathname === "/" || pathname.startsWith("/booking");
  const isGuests = pathname === "/guests";

  return (
    <header className="bg-zinc-900 text-white shadow-lg">
      <div className="max-w-7xl mx-auto px-4 py-4">
        <div className="flex items-center justify-between">
          <Link href="/" className="flex items-center gap-3">
            <Trophy className="h-8 w-8 text-yellow-400" />
            <div>
              <h1 className="text-xl font-bold tracking-tight">
                T20 World Cup 2026
              </h1>
              <p className="text-zinc-400 text-xs">Ticket Booking System</p>
            </div>
          </Link>
          <nav className="flex gap-2">
            <Link href="/">
              <Button
                variant={isMatches ? "secondary" : "ghost"}
                size="sm"
                className={isMatches ? "" : "text-zinc-300 hover:text-white"}
              >
                <Ticket className="h-4 w-4 mr-1" />
                Matches
              </Button>
            </Link>
            <Link href="/guests">
              <Button
                variant={isGuests ? "secondary" : "ghost"}
                size="sm"
                className={isGuests ? "" : "text-zinc-300 hover:text-white"}
              >
                <Users className="h-4 w-4 mr-1" />
                Guest Register
              </Button>
            </Link>
          </nav>
        </div>
      </div>
    </header>
  );
}
