package com.t20worldcup.ticketbooking.model;

import java.time.LocalDateTime;

public class Match {

    private String id;
    private String teamA;
    private String teamB;
    private String venue;
    private LocalDateTime dateTime;
    private int totalSeats;
    private int availableSeats;
    private String stage;

    public Match() {}

    public Match(String id, String teamA, String teamB, String venue, LocalDateTime dateTime, int totalSeats, String stage) {
        this.id = id;
        this.teamA = teamA;
        this.teamB = teamB;
        this.venue = venue;
        this.dateTime = dateTime;
        this.totalSeats = totalSeats;
        this.availableSeats = totalSeats;
        this.stage = stage;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTeamA() { return teamA; }
    public void setTeamA(String teamA) { this.teamA = teamA; }

    public String getTeamB() { return teamB; }
    public void setTeamB(String teamB) { this.teamB = teamB; }

    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }

    public LocalDateTime getDateTime() { return dateTime; }
    public void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }

    public int getTotalSeats() { return totalSeats; }
    public void setTotalSeats(int totalSeats) { this.totalSeats = totalSeats; }

    public int getAvailableSeats() { return availableSeats; }
    public void setAvailableSeats(int availableSeats) { this.availableSeats = availableSeats; }

    public String getStage() { return stage; }
    public void setStage(String stage) { this.stage = stage; }
}
