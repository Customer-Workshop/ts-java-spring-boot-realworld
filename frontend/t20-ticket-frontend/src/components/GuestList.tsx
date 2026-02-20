import { useState, useEffect } from "react";
import { Booking, Match } from "../types";
import { fetchGuests, fetchMatches } from "../api";
import { Card, CardHeader, CardTitle, CardContent } from "./ui/card";
import { Badge } from "./ui/badge";
import { Button } from "./ui/button";
import { Users, Ticket } from "lucide-react";

export default function GuestList() {
  const [guests, setGuests] = useState<Booking[]>([]);
  const [matches, setMatches] = useState<Match[]>([]);
  const [selectedMatch, setSelectedMatch] = useState<string>("");
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchMatches().then(setMatches).catch(() => {});
    loadGuests();
  }, []);

  const loadGuests = (matchId?: string) => {
    setLoading(true);
    fetchGuests(matchId)
      .then((data) => {
        setGuests(data);
        setLoading(false);
      })
      .catch(() => setLoading(false));
  };

  const handleFilterChange = (matchId: string) => {
    setSelectedMatch(matchId);
    loadGuests(matchId || undefined);
  };

  const getMatchLabel = (matchId: string) => {
    const match = matches.find((m) => m.id === matchId);
    return match ? `${match.teamA} vs ${match.teamB}` : matchId;
  };

  return (
    <Card>
      <CardHeader>
        <div className="flex items-center justify-between flex-wrap gap-4">
          <div className="flex items-center gap-2">
            <Users className="h-5 w-5" />
            <CardTitle>Guest Register</CardTitle>
            <Badge variant="secondary">{guests.length} bookings</Badge>
          </div>
          <div className="flex items-center gap-2">
            <select
              value={selectedMatch}
              onChange={(e) => handleFilterChange(e.target.value)}
              className="h-9 rounded-md border border-zinc-200 bg-transparent px-3 py-1 text-sm shadow-sm focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-zinc-950"
            >
              <option value="">All Matches</option>
              {matches.map((m) => (
                <option key={m.id} value={m.id}>
                  {m.teamA} vs {m.teamB}
                </option>
              ))}
            </select>
            <Button variant="outline" size="sm" onClick={() => loadGuests(selectedMatch || undefined)}>
              Refresh
            </Button>
          </div>
        </div>
      </CardHeader>
      <CardContent>
        {loading ? (
          <div className="flex items-center justify-center py-10">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-zinc-900"></div>
          </div>
        ) : guests.length === 0 ? (
          <div className="text-center py-10 text-zinc-400">
            <Ticket className="h-12 w-12 mx-auto mb-2 opacity-50" />
            <p>No bookings found</p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-zinc-200">
                  <th className="text-left py-3 px-2 font-medium text-zinc-500">Name</th>
                  <th className="text-left py-3 px-2 font-medium text-zinc-500">Match</th>
                  <th className="text-left py-3 px-2 font-medium text-zinc-500">Seat</th>
                  <th className="text-left py-3 px-2 font-medium text-zinc-500">Booked At</th>
                </tr>
              </thead>
              <tbody>
                {guests.map((guest) => (
                  <tr key={guest.id} className="border-b border-zinc-100 hover:bg-zinc-50">
                    <td className="py-3 px-2 font-medium">{guest.userName}</td>
                    <td className="py-3 px-2">{getMatchLabel(guest.matchId)}</td>
                    <td className="py-3 px-2">
                      <Badge variant="outline">{guest.seatNumber}</Badge>
                    </td>
                    <td className="py-3 px-2 text-zinc-500">
                      {new Date(guest.bookingTime).toLocaleString()}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </CardContent>
    </Card>
  );
}
