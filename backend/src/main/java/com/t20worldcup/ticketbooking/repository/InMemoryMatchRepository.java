package com.t20worldcup.ticketbooking.repository;

import com.t20worldcup.ticketbooking.model.Match;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryMatchRepository {

    private final ConcurrentHashMap<String, Match> matches = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        matches.put("M001", new Match("M001", "India", "Pakistan", "Nassau County International Cricket Stadium, New York", LocalDateTime.of(2026, 2, 9, 14, 30), 100, "Group Stage"));
        matches.put("M002", new Match("M002", "Australia", "England", "Kensington Oval, Barbados", LocalDateTime.of(2026, 2, 10, 10, 0), 100, "Group Stage"));
        matches.put("M003", new Match("M003", "South Africa", "New Zealand", "Providence Stadium, Guyana", LocalDateTime.of(2026, 2, 11, 14, 30), 100, "Group Stage"));
        matches.put("M004", new Match("M004", "West Indies", "Sri Lanka", "Brian Lara Cricket Academy, Trinidad", LocalDateTime.of(2026, 2, 12, 10, 0), 100, "Group Stage"));
        matches.put("M005", new Match("M005", "Bangladesh", "Afghanistan", "Arnos Vale Ground, St Vincent", LocalDateTime.of(2026, 2, 13, 14, 30), 100, "Group Stage"));
        matches.put("M006", new Match("M006", "India", "Australia", "Kensington Oval, Barbados", LocalDateTime.of(2026, 2, 15, 14, 30), 100, "Super 8"));
        matches.put("M007", new Match("M007", "England", "South Africa", "Sir Vivian Richards Stadium, Antigua", LocalDateTime.of(2026, 2, 16, 10, 0), 100, "Super 8"));
        matches.put("M008", new Match("M008", "India", "England", "Providence Stadium, Guyana", LocalDateTime.of(2026, 2, 19, 14, 30), 100, "Semi-Final"));
        matches.put("M009", new Match("M009", "Australia", "South Africa", "Brian Lara Cricket Academy, Trinidad", LocalDateTime.of(2026, 2, 20, 14, 30), 100, "Semi-Final"));
        matches.put("M010", new Match("M010", "TBD", "TBD", "Kensington Oval, Barbados", LocalDateTime.of(2026, 2, 22, 14, 30), 100, "Final"));
    }

    public Collection<Match> getAllMatches() {
        return Collections.unmodifiableCollection(matches.values());
    }

    public Optional<Match> getMatch(String id) {
        return Optional.ofNullable(matches.get(id));
    }

    public void updateAvailableSeats(String matchId, int availableSeats) {
        Match match = matches.get(matchId);
        if (match != null) {
            match.setAvailableSeats(availableSeats);
        }
    }
}
