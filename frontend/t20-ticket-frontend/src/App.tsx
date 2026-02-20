import { useState } from "react";
import { Match } from "./types";
import MatchList from "./components/MatchList";
import BookingForm from "./components/BookingForm";
import GuestList from "./components/GuestList";
import { Button } from "./components/ui/button";
import { Trophy, Ticket, Users } from "lucide-react";

type View = "matches" | "booking" | "guests";

function App() {
  const [view, setView] = useState<View>("matches");
  const [selectedMatch, setSelectedMatch] = useState<Match | null>(null);

  const handleSelectMatch = (match: Match) => {
    setSelectedMatch(match);
    setView("booking");
  };

  const handleBack = () => {
    setSelectedMatch(null);
    setView("matches");
  };

  return (
    <div className="min-h-screen bg-zinc-50">
      <header className="bg-zinc-900 text-white shadow-lg">
        <div className="max-w-7xl mx-auto px-4 py-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-3">
              <Trophy className="h-8 w-8 text-yellow-400" />
              <div>
                <h1 className="text-xl font-bold tracking-tight">
                  T20 World Cup 2026
                </h1>
                <p className="text-zinc-400 text-xs">Ticket Booking System</p>
              </div>
            </div>
            <nav className="flex gap-2">
              <Button
                variant={view === "matches" || view === "booking" ? "secondary" : "ghost"}
                size="sm"
                onClick={() => {
                  setView("matches");
                  setSelectedMatch(null);
                }}
                className={view === "matches" || view === "booking" ? "" : "text-zinc-300 hover:text-white"}
              >
                <Ticket className="h-4 w-4 mr-1" />
                Matches
              </Button>
              <Button
                variant={view === "guests" ? "secondary" : "ghost"}
                size="sm"
                onClick={() => setView("guests")}
                className={view === "guests" ? "" : "text-zinc-300 hover:text-white"}
              >
                <Users className="h-4 w-4 mr-1" />
                Guest Register
              </Button>
            </nav>
          </div>
        </div>
      </header>

      <main className="max-w-7xl mx-auto px-4 py-8">
        {view === "matches" && <MatchList onSelectMatch={handleSelectMatch} />}
        {view === "booking" && selectedMatch && (
          <BookingForm match={selectedMatch} onBack={handleBack} />
        )}
        {view === "guests" && <GuestList />}
      </main>

      <footer className="bg-zinc-900 text-zinc-400 text-center py-4 text-xs mt-8">
        2026 T20 Cricket World Cup Ticket Booking System
      </footer>
    </div>
  );
}

export default App;
