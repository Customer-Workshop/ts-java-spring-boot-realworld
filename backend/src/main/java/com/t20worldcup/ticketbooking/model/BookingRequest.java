package com.t20worldcup.ticketbooking.model;

import jakarta.validation.constraints.NotBlank;

public class BookingRequest {

    @NotBlank(message = "User name is required")
    private String userName;

    @NotBlank(message = "Match ID is required")
    private String matchId;

    @NotBlank(message = "Seat number is required")
    private String seatNumber;

    public BookingRequest() {}

    public BookingRequest(String userName, String matchId, String seatNumber) {
        this.userName = userName;
        this.matchId = matchId;
        this.seatNumber = seatNumber;
    }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getMatchId() { return matchId; }
    public void setMatchId(String matchId) { this.matchId = matchId; }

    public String getSeatNumber() { return seatNumber; }
    public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }
}
