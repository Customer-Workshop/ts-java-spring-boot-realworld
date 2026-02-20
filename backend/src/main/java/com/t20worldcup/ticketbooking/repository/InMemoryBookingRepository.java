package com.t20worldcup.ticketbooking.repository;

import com.t20worldcup.ticketbooking.model.Booking;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class InMemoryBookingRepository {

    private final ConcurrentHashMap<String, Booking> bookings = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Set<String>> bookedSeatsPerMatch = new ConcurrentHashMap<>();

    public boolean isSeatBooked(String matchId, String seatNumber) {
        Set<String> seats = bookedSeatsPerMatch.get(matchId);
        return seats != null && seats.contains(seatNumber);
    }

    public boolean bookSeat(String matchId, String seatNumber, Booking booking) {
        bookedSeatsPerMatch.putIfAbsent(matchId, ConcurrentHashMap.newKeySet());
        Set<String> seats = bookedSeatsPerMatch.get(matchId);
        boolean added = seats.add(seatNumber);
        if (added) {
            bookings.put(booking.getId(), booking);
        }
        return added;
    }

    public Set<String> getBookedSeats(String matchId) {
        Set<String> seats = bookedSeatsPerMatch.get(matchId);
        if (seats == null) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(seats);
    }

    public Collection<Booking> getAllBookings() {
        return Collections.unmodifiableCollection(bookings.values());
    }

    public List<Booking> getBookingsByMatch(String matchId) {
        return bookings.values().stream()
                .filter(b -> b.getMatchId().equals(matchId))
                .collect(Collectors.toList());
    }

    public int getBookedSeatCount(String matchId) {
        Set<String> seats = bookedSeatsPerMatch.get(matchId);
        return seats == null ? 0 : seats.size();
    }
}
