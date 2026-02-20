package com.t20worldcup.ticketbooking.model;

import java.util.Set;

public class SeatUpdate {

    private String matchId;
    private int availableSeats;
    private Set<String> bookedSeats;

    public SeatUpdate() {}

    public SeatUpdate(String matchId, int availableSeats, Set<String> bookedSeats) {
        this.matchId = matchId;
        this.availableSeats = availableSeats;
        this.bookedSeats = bookedSeats;
    }

    public String getMatchId() { return matchId; }
    public void setMatchId(String matchId) { this.matchId = matchId; }

    public int getAvailableSeats() { return availableSeats; }
    public void setAvailableSeats(int availableSeats) { this.availableSeats = availableSeats; }

    public Set<String> getBookedSeats() { return bookedSeats; }
    public void setBookedSeats(Set<String> bookedSeats) { this.bookedSeats = bookedSeats; }
}
