package com.chubb.booking.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.chubb.booking.dto.BookingRequest;
import com.chubb.booking.dto.BookingResponse;
import com.chubb.booking.dto.TicketDetailsResponse;
import com.chubb.booking.model.Booking;
import com.chubb.booking.service.BookingService;

import jakarta.validation.Valid;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;



@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Validated
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public Mono<ResponseEntity<BookingResponse>> createBooking(@Valid @RequestBody BookingRequest request) {
        return bookingService.createBooking(request)
                .map(resp -> ResponseEntity.status(201).body(resp));
    }

    @GetMapping("/{pnr}")
    public Mono<ResponseEntity<TicketDetailsResponse>> getTicket(@PathVariable String pnr) {
        return bookingService.getTicketByPnr(pnr)
                .map(resp -> ResponseEntity.ok(resp));
    }

    @DeleteMapping("/{pnr}")
    public Mono<ResponseEntity<Void>> cancelBooking(@PathVariable String pnr) {
        return bookingService.cancelBooking(pnr)
                .thenReturn(ResponseEntity.ok().<Void>build());
    }
    
    @GetMapping("/api/bookings")
    public Mono<ResponseEntity<Flux<Booking>>> getAllBookings() {
        Flux<Booking> all = bookingService.listAllBookings();
        return Mono.just(ResponseEntity.ok().body(all));
    }
}