export interface Match {
  id: string;
  teamA: string;
  teamB: string;
  venue: string;
  dateTime: string;
  totalSeats: number;
  availableSeats: number;
  stage: string;
}

export interface Booking {
  id: string;
  userName: string;
  matchId: string;
  seatNumber: string;
  bookingTime: string;
}

export interface BookingRequest {
  userName: string;
  matchId: string;
  seatNumber: string;
}

export interface SeatUpdate {
  matchId: string;
  availableSeats: number;
  bookedSeats: string[];
}
