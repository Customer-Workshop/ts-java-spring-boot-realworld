package com.t20worldcup.ticketbooking;

import com.t20worldcup.ticketbooking.model.BookingRequest;
import com.t20worldcup.ticketbooking.repository.InMemoryBookingRepository;
import com.t20worldcup.ticketbooking.repository.InMemoryMatchRepository;
import com.t20worldcup.ticketbooking.service.BookingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class TicketBookingApplicationTests {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private BookingService bookingService;

    @Test
    void contextLoads() {
    }

    @Test
    void shouldListMatches() {
        webTestClient.get().uri("/api/matches")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Object.class)
                .hasSize(10);
    }

    @Test
    void shouldBookTicket() {
        webTestClient.post().uri("/api/book")
                .bodyValue(new BookingRequest("John Doe", "M001", "A1"))
                .exchange()
                .expectStatus().isCreated();
    }

    @Test
    void shouldRejectDuplicateBooking() {
        webTestClient.post().uri("/api/book")
                .bodyValue(new BookingRequest("Jane Doe", "M002", "B1"))
                .exchange()
                .expectStatus().isCreated();

        webTestClient.post().uri("/api/book")
                .bodyValue(new BookingRequest("Bob Smith", "M002", "B1"))
                .exchange()
                .expectStatus().isEqualTo(409);
    }

    @Test
    void shouldGetGuests() {
        webTestClient.post().uri("/api/book")
                .bodyValue(new BookingRequest("Alice", "M003", "C1"))
                .exchange()
                .expectStatus().isCreated();

        webTestClient.get().uri("/api/guests?matchId=M003")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Object.class)
                .hasSize(1);
    }

    @Test
    void shouldHandleConcurrentBookings() throws InterruptedException {
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final String userName = "User" + i;
            executor.submit(() -> {
                try {
                    webTestClient.post().uri("/api/book")
                            .bodyValue(new BookingRequest(userName, "M004", "D1"))
                            .exchange()
                            .expectStatus().value(status -> {
                                if (status == 201) {
                                    successCount.incrementAndGet();
                                } else {
                                    failCount.incrementAndGet();
                                }
                            });
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        assertEquals(1, successCount.get(), "Only one booking should succeed for the same seat");
        assertEquals(threadCount - 1, failCount.get(), "All other bookings should fail");
    }
}
