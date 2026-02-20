import { Match, Booking, BookingRequest, SeatUpdate } from "./types";

const API_URL = import.meta.env.VITE_API_URL || "http://localhost:8080/api";

export async function fetchMatches(): Promise<Match[]> {
  const res = await fetch(`${API_URL}/matches`);
  if (!res.ok) throw new Error("Failed to fetch matches");
  return res.json();
}

export async function fetchMatch(matchId: string): Promise<Match> {
  const res = await fetch(`${API_URL}/matches/${matchId}`);
  if (!res.ok) throw new Error("Failed to fetch match");
  return res.json();
}

export async function bookTicket(request: BookingRequest): Promise<Booking> {
  const res = await fetch(`${API_URL}/book`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(request),
  });
  if (!res.ok) {
    const err = await res.json();
    throw new Error(err.error || "Booking failed");
  }
  return res.json();
}

export async function fetchGuests(matchId?: string): Promise<Booking[]> {
  const url = matchId ? `${API_URL}/guests?matchId=${matchId}` : `${API_URL}/guests`;
  const res = await fetch(url);
  if (!res.ok) throw new Error("Failed to fetch guests");
  return res.json();
}

export async function fetchSeatStatus(matchId: string): Promise<SeatUpdate> {
  const res = await fetch(`${API_URL}/seats/${matchId}`);
  if (!res.ok) throw new Error("Failed to fetch seat status");
  return res.json();
}

export function subscribeSeatUpdates(onUpdate: (update: SeatUpdate) => void): () => void {
  const eventSource = new EventSource(`${API_URL}/seats/stream`);
  eventSource.onmessage = (event) => {
    const update: SeatUpdate = JSON.parse(event.data);
    onUpdate(update);
  };
  eventSource.onerror = () => {
    eventSource.close();
  };
  return () => eventSource.close();
}
