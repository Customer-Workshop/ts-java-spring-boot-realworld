import { useState, useEffect } from "react";
import { SeatUpdate } from "../types";
import { fetchSeatStatus } from "../api";
import { cn } from "@/lib/utils";

interface SeatMapProps {
  matchId: string;
  totalSeats: number;
  onSelectSeat: (seat: string) => void;
  selectedSeat: string | null;
  seatUpdate: SeatUpdate | null;
}

const ROWS = ["A", "B", "C", "D", "E", "F", "G", "H", "I", "J"];
const COLS = 10;

export default function SeatMap({
  matchId,
  totalSeats: _totalSeats,
  onSelectSeat,
  selectedSeat,
  seatUpdate,
}: SeatMapProps) {
  void _totalSeats;
  const [bookedSeats, setBookedSeats] = useState<Set<string>>(new Set());
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchSeatStatus(matchId)
      .then((status) => {
        setBookedSeats(new Set(status.bookedSeats));
        setLoading(false);
      })
      .catch(() => setLoading(false));
  }, [matchId]);

  useEffect(() => {
    if (seatUpdate && seatUpdate.matchId === matchId) {
      setBookedSeats(new Set(seatUpdate.bookedSeats));
    }
  }, [seatUpdate, matchId]);

  if (loading) {
    return (
      <div className="flex items-center justify-center py-10">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-zinc-900"></div>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      <div className="bg-zinc-800 text-white text-center py-2 rounded-t-lg text-sm font-medium tracking-wider">
        PITCH VIEW
      </div>
      <div className="overflow-x-auto">
        <div className="min-w-fit mx-auto">
          {ROWS.map((row) => (
            <div key={row} className="flex items-center gap-1 mb-1 justify-center">
              <span className="w-6 text-xs font-medium text-zinc-500 text-right">
                {row}
              </span>
              {Array.from({ length: COLS }, (_, i) => {
                const seatId = `${row}${i + 1}`;
                const isBooked = bookedSeats.has(seatId);
                const isSelected = selectedSeat === seatId;

                return (
                  <button
                    key={seatId}
                    disabled={isBooked}
                    onClick={() => onSelectSeat(seatId)}
                    title={isBooked ? `${seatId} - Booked` : `${seatId} - Available`}
                    className={cn(
                      "w-8 h-8 rounded text-xs font-medium transition-all",
                      isBooked
                        ? "bg-red-200 text-red-600 cursor-not-allowed"
                        : isSelected
                        ? "bg-green-500 text-white ring-2 ring-green-700 scale-110"
                        : "bg-emerald-100 text-emerald-700 hover:bg-emerald-200 hover:scale-105 cursor-pointer"
                    )}
                  >
                    {i + 1}
                  </button>
                );
              })}
            </div>
          ))}
        </div>
      </div>
      <div className="flex gap-4 justify-center text-xs">
        <div className="flex items-center gap-1">
          <div className="w-4 h-4 rounded bg-emerald-100 border border-emerald-300"></div>
          <span>Available</span>
        </div>
        <div className="flex items-center gap-1">
          <div className="w-4 h-4 rounded bg-green-500"></div>
          <span>Selected</span>
        </div>
        <div className="flex items-center gap-1">
          <div className="w-4 h-4 rounded bg-red-200"></div>
          <span>Booked</span>
        </div>
      </div>
    </div>
  );
}
