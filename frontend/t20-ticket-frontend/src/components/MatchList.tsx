import { useState, useEffect } from "react";
import { Match, SeatUpdate } from "../types";
import { fetchMatches, subscribeSeatUpdates } from "../api";
import { Card, CardHeader, CardTitle, CardContent, CardFooter } from "./ui/card";
import { Badge } from "./ui/badge";
import { Button } from "./ui/button";
import { MapPin, Calendar, Users } from "lucide-react";

interface MatchListProps {
  onSelectMatch: (match: Match) => void;
}

const stageColors: Record<string, string> = {
  "Group Stage": "bg-blue-100 text-blue-800 border-blue-200",
  "Super 8": "bg-purple-100 text-purple-800 border-purple-200",
  "Semi-Final": "bg-orange-100 text-orange-800 border-orange-200",
  "Final": "bg-red-100 text-red-800 border-red-200",
};

const teamFlags: Record<string, string> = {
  India: "\u{1F1EE}\u{1F1F3}",
  Pakistan: "\u{1F1F5}\u{1F1F0}",
  Australia: "\u{1F1E6}\u{1F1FA}",
  England: "\u{1F3F4}\u{E0067}\u{E0062}\u{E0065}\u{E006E}\u{E0067}\u{E007F}",
  "South Africa": "\u{1F1FF}\u{1F1E6}",
  "New Zealand": "\u{1F1F3}\u{1F1FF}",
  "West Indies": "\u{1F3CF}",
  "Sri Lanka": "\u{1F1F1}\u{1F1F0}",
  Bangladesh: "\u{1F1E7}\u{1F1E9}",
  Afghanistan: "\u{1F1E6}\u{1F1EB}",
  TBD: "\u{1F3C6}",
};

export default function MatchList({ onSelectMatch }: MatchListProps) {
  const [matches, setMatches] = useState<Match[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [filter, setFilter] = useState<string>("All");

  useEffect(() => {
    fetchMatches()
      .then((data) => {
        const sorted = data.sort(
          (a, b) => new Date(a.dateTime).getTime() - new Date(b.dateTime).getTime()
        );
        setMatches(sorted);
        setLoading(false);
      })
      .catch((err) => {
        setError(err.message);
        setLoading(false);
      });

    const unsub = subscribeSeatUpdates((update: SeatUpdate) => {
      setMatches((prev) =>
        prev.map((m) =>
          m.id === update.matchId ? { ...m, availableSeats: update.availableSeats } : m
        )
      );
    });

    return unsub;
  }, []);

  const stages = ["All", "Group Stage", "Super 8", "Semi-Final", "Final"];
  const filteredMatches =
    filter === "All" ? matches : matches.filter((m) => m.stage === filter);

  if (loading) {
    return (
      <div className="flex items-center justify-center py-20">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-zinc-900"></div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="text-center py-20">
        <p className="text-red-500 text-lg">{error}</p>
        <p className="text-zinc-500 mt-2">Make sure the backend server is running.</p>
      </div>
    );
  }

  return (
    <div>
      <div className="flex gap-2 mb-6 flex-wrap">
        {stages.map((stage) => (
          <Button
            key={stage}
            variant={filter === stage ? "default" : "outline"}
            size="sm"
            onClick={() => setFilter(stage)}
          >
            {stage}
          </Button>
        ))}
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {filteredMatches.map((match) => (
          <Card
            key={match.id}
            className="hover:shadow-lg transition-shadow cursor-pointer border-l-4"
            style={{
              borderLeftColor:
                match.stage === "Final"
                  ? "#ef4444"
                  : match.stage === "Semi-Final"
                  ? "#f97316"
                  : match.stage === "Super 8"
                  ? "#a855f7"
                  : "#3b82f6",
            }}
          >
            <CardHeader className="pb-3">
              <div className="flex items-center justify-between">
                <Badge className={stageColors[match.stage] || ""}>
                  {match.stage}
                </Badge>
                <span className="text-xs text-zinc-400 font-mono">{match.id}</span>
              </div>
              <CardTitle className="text-lg mt-2">
                <span className="mr-1">{teamFlags[match.teamA] || ""}</span>
                {match.teamA}
                <span className="mx-2 text-zinc-400 font-normal">vs</span>
                <span className="mr-1">{teamFlags[match.teamB] || ""}</span>
                {match.teamB}
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-2 text-sm text-zinc-600">
              <div className="flex items-center gap-2">
                <MapPin className="h-4 w-4 text-zinc-400" />
                <span>{match.venue}</span>
              </div>
              <div className="flex items-center gap-2">
                <Calendar className="h-4 w-4 text-zinc-400" />
                <span>
                  {new Date(match.dateTime).toLocaleDateString("en-US", {
                    weekday: "short",
                    year: "numeric",
                    month: "short",
                    day: "numeric",
                  })}{" "}
                  at{" "}
                  {new Date(match.dateTime).toLocaleTimeString("en-US", {
                    hour: "2-digit",
                    minute: "2-digit",
                  })}
                </span>
              </div>
              <div className="flex items-center gap-2">
                <Users className="h-4 w-4 text-zinc-400" />
                <span>
                  <strong className={match.availableSeats < 20 ? "text-red-600" : "text-green-600"}>
                    {match.availableSeats}
                  </strong>{" "}
                  / {match.totalSeats} seats available
                </span>
              </div>
            </CardContent>
            <CardFooter>
              <Button
                className="w-full"
                onClick={() => onSelectMatch(match)}
                disabled={match.availableSeats === 0}
              >
                {match.availableSeats === 0 ? "Sold Out" : "Book Tickets"}
              </Button>
            </CardFooter>
          </Card>
        ))}
      </div>
    </div>
  );
}
