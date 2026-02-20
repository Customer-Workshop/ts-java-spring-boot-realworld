import { useState, useEffect } from "react";
import { Match, Booking, SeatUpdate } from "../types";
import { bookTicket, subscribeSeatUpdates } from "../api";
import { Button } from "./ui/button";
import { Input } from "./ui/input";
import { Label } from "./ui/label";
import { Card, CardHeader, CardTitle, CardContent } from "./ui/card";
import { Badge } from "./ui/badge";
import SeatMap from "./SeatMap";
import { ArrowLeft, CheckCircle, AlertCircle, MapPin, Calendar, Users } from "lucide-react";

interface BookingFormProps {
  match: Match;
  onBack: () => void;
}

export default function BookingForm({ match, onBack }: BookingFormProps) {
  const [userName, setUserName] = useState("");
  const [selectedSeat, setSelectedSeat] = useState<string | null>(null);
  const [booking, setBooking] = useState<Booking | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [seatUpdate, setSeatUpdate] = useState<SeatUpdate | null>(null);

  useEffect(() => {
    const unsub = subscribeSeatUpdates((update) => {
      if (update.matchId === match.id) {
        setSeatUpdate(update);
      }
    });
    return unsub;
  }, [match.id]);

  const handleBook = async () => {
    if (!userName.trim()) {
      setError("Please enter your name");
      return;
    }
    if (!selectedSeat) {
      setError("Please select a seat");
      return;
    }

    setLoading(true);
    setError(null);

    try {
      const result = await bookTicket({
        userName: userName.trim(),
        matchId: match.id,
        seatNumber: selectedSeat,
      });
      setBooking(result);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Booking failed");
    } finally {
      setLoading(false);
    }
  };

  if (booking) {
    return (
      <div className="max-w-lg mx-auto">
        <Card className="border-green-200 bg-green-50">
          <CardHeader className="text-center">
            <CheckCircle className="h-16 w-16 text-green-500 mx-auto mb-2" />
            <CardTitle className="text-2xl text-green-800">Booking Confirmed!</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            <div className="bg-white rounded-lg p-4 space-y-2">
              <div className="flex justify-between">
                <span className="text-zinc-500">Booking ID</span>
                <span className="font-mono text-sm">{booking.id.slice(0, 8)}...</span>
              </div>
              <div className="flex justify-between">
                <span className="text-zinc-500">Name</span>
                <span className="font-medium">{booking.userName}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-zinc-500">Match</span>
                <span className="font-medium">{match.teamA} vs {match.teamB}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-zinc-500">Seat</span>
                <Badge className="bg-green-100 text-green-800 border-green-200">{booking.seatNumber}</Badge>
              </div>
              <div className="flex justify-between">
                <span className="text-zinc-500">Venue</span>
                <span className="text-sm">{match.venue}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-zinc-500">Date</span>
                <span className="text-sm">
                  {new Date(match.dateTime).toLocaleDateString("en-US", {
                    weekday: "short",
                    year: "numeric",
                    month: "short",
                    day: "numeric",
                  })}
                </span>
              </div>
            </div>
            <Button onClick={onBack} className="w-full mt-4" variant="outline">
              <ArrowLeft className="h-4 w-4 mr-2" />
              Back to Matches
            </Button>
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <Button variant="ghost" onClick={onBack} className="mb-2">
        <ArrowLeft className="h-4 w-4 mr-2" />
        Back to Matches
      </Button>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2">
          <Card>
            <CardHeader>
              <CardTitle className="text-xl">Select Your Seat</CardTitle>
              <p className="text-sm text-zinc-500">
                {match.teamA} vs {match.teamB}
              </p>
            </CardHeader>
            <CardContent>
              <SeatMap
                matchId={match.id}
                totalSeats={match.totalSeats}
                onSelectSeat={setSelectedSeat}
                selectedSeat={selectedSeat}
                seatUpdate={seatUpdate}
              />
            </CardContent>
          </Card>
        </div>

        <div>
          <Card>
            <CardHeader>
              <CardTitle className="text-lg">Match Details</CardTitle>
            </CardHeader>
            <CardContent className="space-y-3 text-sm">
              <div className="flex items-center gap-2 text-zinc-600">
                <MapPin className="h-4 w-4" />
                <span>{match.venue}</span>
              </div>
              <div className="flex items-center gap-2 text-zinc-600">
                <Calendar className="h-4 w-4" />
                <span>
                  {new Date(match.dateTime).toLocaleDateString("en-US", {
                    weekday: "long",
                    year: "numeric",
                    month: "long",
                    day: "numeric",
                  })}
                </span>
              </div>
              <div className="flex items-center gap-2 text-zinc-600">
                <Users className="h-4 w-4" />
                <span>
                  {seatUpdate ? seatUpdate.availableSeats : match.availableSeats} seats available
                </span>
              </div>
            </CardContent>
          </Card>

          <Card className="mt-4">
            <CardHeader>
              <CardTitle className="text-lg">Book Ticket</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="name">Your Name</Label>
                <Input
                  id="name"
                  placeholder="Enter your name"
                  value={userName}
                  onChange={(e) => setUserName(e.target.value)}
                />
              </div>

              <div className="space-y-2">
                <Label>Selected Seat</Label>
                <div className="h-9 flex items-center px-3 rounded-md border border-zinc-200 bg-zinc-50 text-sm">
                  {selectedSeat ? (
                    <Badge className="bg-green-100 text-green-800 border-green-200">
                      {selectedSeat}
                    </Badge>
                  ) : (
                    <span className="text-zinc-400">Click a seat on the map</span>
                  )}
                </div>
              </div>

              {error && (
                <div className="flex items-center gap-2 text-red-600 text-sm bg-red-50 p-2 rounded">
                  <AlertCircle className="h-4 w-4" />
                  {error}
                </div>
              )}

              <Button
                className="w-full"
                onClick={handleBook}
                disabled={!userName.trim() || !selectedSeat || loading}
              >
                {loading ? "Booking..." : "Confirm Booking"}
              </Button>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}
