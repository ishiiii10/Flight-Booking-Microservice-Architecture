package com.booking.bookingservice.controller;

import com.booking.bookingservice.dto.BookingRequest;
import com.booking.bookingservice.model.Booking;
import com.booking.bookingservice.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/booking")
public class BookingController {

    private static final Logger log = LoggerFactory.getLogger(BookingController.class);
    private final BookingService service;

    public BookingController(BookingService service) {
        this.service = service;
    }

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a booking")
    public Booking createBooking(@RequestBody BookingRequest req) {
        log.info("Create booking request for flight {}", req.getFlightId());
        return service.bookTicket(req);
    }

    @PutMapping("/cancel/{id}")
    @Operation(summary = "Cancel booking")
    public Booking cancelBooking(@PathVariable String id) {
        log.info("Cancel booking {}", id);
        return service.cancelBooking(id);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get booking by id")
    public Booking getBooking(@PathVariable String id) {
        return service.getBooking(id);
    }

    @GetMapping("/history")
    @Operation(summary = "Get bookings by email")
    public List<Booking> history(@RequestParam String email) {
        return service.getBookingsByEmail(email);
    }
}
