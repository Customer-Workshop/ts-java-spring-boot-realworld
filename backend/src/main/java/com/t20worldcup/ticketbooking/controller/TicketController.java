package com.t20worldcup.ticketbooking.controller;

import com.t20worldcup.ticketbooking.model.Booking;
import com.t20worldcup.ticketbooking.model.BookingRequest;
import com.t20worldcup.ticketbooking.model.Match;
import com.t20worldcup.ticketbooking.model.SeatUpdate;
import com.t20worldcup.ticketbooking.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class TicketController {

    private final BookingService bookingService;

    public TicketController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping("/matches")
    public Flux<Match> getMatches() {
        return bookingService.getAllMatches();
    }

    @GetMapping("/matches/{matchId}")
    public Mono<ResponseEntity<Match>> getMatch(@PathVariable String matchId) {
        return bookingService.getMatch(matchId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping("/book")
    public Mono<ResponseEntity<Object>> bookTicket(@Valid @RequestBody BookingRequest request) {
        return bookingService.bookSeat(request)
                .map(booking -> ResponseEntity.status(HttpStatus.CREATED).body((Object) booking))
                .onErrorResume(IllegalStateException.class, e ->
                        Mono.just(ResponseEntity.status(HttpStatus.CONFLICT)
                                .body(Map.of("error", e.getMessage()))))
                .onErrorResume(IllegalArgumentException.class, e ->
                        Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(Map.of("error", e.getMessage()))));
    }

    @GetMapping("/guests")
    public Flux<Booking> getGuests(@RequestParam(required = false) String matchId) {
        if (matchId != null && !matchId.isEmpty()) {
            return bookingService.getGuestsByMatch(matchId);
        }
        return bookingService.getAllGuests();
    }

    @GetMapping("/seats/{matchId}")
    public Mono<ResponseEntity<Object>> getSeatStatus(@PathVariable String matchId) {
        return bookingService.getSeatStatus(matchId)
                .map(status -> ResponseEntity.ok().body((Object) status))
                .onErrorResume(IllegalArgumentException.class, e ->
                        Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(Map.of("error", e.getMessage()))));
    }

    @GetMapping(value = "/seats/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<SeatUpdate> streamSeatUpdates() {
        return bookingService.getSeatUpdates();
    }
}
