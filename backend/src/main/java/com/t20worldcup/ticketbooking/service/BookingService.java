package com.t20worldcup.ticketbooking.service;

import com.t20worldcup.ticketbooking.model.Booking;
import com.t20worldcup.ticketbooking.model.BookingRequest;
import com.t20worldcup.ticketbooking.model.Match;
import com.t20worldcup.ticketbooking.model.SeatUpdate;
import com.t20worldcup.ticketbooking.repository.InMemoryBookingRepository;
import com.t20worldcup.ticketbooking.repository.InMemoryMatchRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class BookingService {

    private final InMemoryBookingRepository bookingRepository;
    private final InMemoryMatchRepository matchRepository;
    private final Sinks.Many<SeatUpdate> seatUpdateSink;

    public BookingService(InMemoryBookingRepository bookingRepository, InMemoryMatchRepository matchRepository) {
        this.bookingRepository = bookingRepository;
        this.matchRepository = matchRepository;
        this.seatUpdateSink = Sinks.many().multicast().onBackpressureBuffer();
    }

    public Flux<Match> getAllMatches() {
        return Flux.fromIterable(matchRepository.getAllMatches());
    }

    public Mono<Match> getMatch(String matchId) {
        return Mono.justOrEmpty(matchRepository.getMatch(matchId));
    }

    public Mono<Booking> bookSeat(BookingRequest request) {
        return Mono.defer(() -> {
            String matchId = request.getMatchId();
            String seatNumber = request.getSeatNumber();

            return matchRepository.getMatch(matchId)
                    .map(match -> {
                        if (match.getAvailableSeats() <= 0) {
                            throw new IllegalStateException("No seats available for this match");
                        }

                        String bookingId = UUID.randomUUID().toString();
                        Booking booking = new Booking(
                                bookingId,
                                request.getUserName(),
                                matchId,
                                seatNumber,
                                LocalDateTime.now()
                        );

                        boolean success = bookingRepository.bookSeat(matchId, seatNumber, booking);
                        if (!success) {
                            throw new IllegalStateException("Seat " + seatNumber + " is already booked for this match");
                        }

                        int bookedCount = bookingRepository.getBookedSeatCount(matchId);
                        int available = match.getTotalSeats() - bookedCount;
                        matchRepository.updateAvailableSeats(matchId, available);

                        Set<String> bookedSeats = bookingRepository.getBookedSeats(matchId);
                        SeatUpdate update = new SeatUpdate(matchId, available, bookedSeats);
                        seatUpdateSink.tryEmitNext(update);

                        return booking;
                    })
                    .map(Mono::just)
                    .orElse(Mono.error(new IllegalArgumentException("Match not found: " + matchId)));
        });
    }

    public Flux<SeatUpdate> getSeatUpdates() {
        return seatUpdateSink.asFlux();
    }

    public Mono<SeatUpdate> getSeatStatus(String matchId) {
        return Mono.defer(() -> {
            return matchRepository.getMatch(matchId)
                    .map(match -> {
                        Set<String> bookedSeats = bookingRepository.getBookedSeats(matchId);
                        int available = match.getTotalSeats() - bookedSeats.size();
                        return Mono.just(new SeatUpdate(matchId, available, bookedSeats));
                    })
                    .orElse(Mono.error(new IllegalArgumentException("Match not found: " + matchId)));
        });
    }

    public Flux<Booking> getAllGuests() {
        return Flux.fromIterable(bookingRepository.getAllBookings());
    }

    public Flux<Booking> getGuestsByMatch(String matchId) {
        return Flux.fromIterable(bookingRepository.getBookingsByMatch(matchId));
    }
}
