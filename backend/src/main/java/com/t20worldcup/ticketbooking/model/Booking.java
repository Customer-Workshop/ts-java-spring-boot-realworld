package com.t20worldcup.ticketbooking.model;

import java.time.LocalDateTime;

public class Booking {

    private String id;
    private String userName;
    private String matchId;
    private String seatNumber;
    private LocalDateTime bookingTime;

    public Booking() {}

    public Booking(String id, String userName, String matchId, String seatNumber, LocalDateTime bookingTime) {
        this.id = id;
        this.userName = userName;
        this.matchId = matchId;
        this.seatNumber = seatNumber;
        this.bookingTime = bookingTime;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getMatchId() { return matchId; }
    public void setMatchId(String matchId) { this.matchId = matchId; }

    public String getSeatNumber() { return seatNumber; }
    public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }

    public LocalDateTime getBookingTime() { return bookingTime; }
    public void setBookingTime(LocalDateTime bookingTime) { this.bookingTime = bookingTime; }
}
