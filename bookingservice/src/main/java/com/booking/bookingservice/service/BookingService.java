package com.booking.bookingservice.service;

import com.booking.bookingservice.dto.BookingRequest;
import com.booking.bookingservice.dto.EmailNotification;
import com.booking.bookingservice.feign.FlightClient;
import com.booking.bookingservice.model.Booking;
import com.booking.bookingservice.producer.EmailProducer;
import com.booking.bookingservice.repo.BookingRepository;
import com.booking.bookingservice.exception.BookingNotFoundException;
import com.booking.bookingservice.exception.FlightServiceDownException;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingService.class);
    private final BookingRepository repository;
    private final FlightClient flightClient;
    private final EmailProducer emailProducer;

    public BookingService(BookingRepository repository,
                          FlightClient flightClient,
                          EmailProducer emailProducer) {
        this.repository = repository;
        this.flightClient = flightClient;
        this.emailProducer = emailProducer;
    }

    @CircuitBreaker(name = "flightservice", fallbackMethod = "bookFallback")
    public Booking bookTicket(BookingRequest req) {
        log.info("Attempt to book {} seats on flight {}", req.getSeats(), req.getFlightId());

        boolean available = flightClient.checkAvailability(req.getFlightId());

        if (!available) {
            // If service is down, fallback returned false => detect that
            throw new FlightServiceDownException("Flight Service is DOWN");
        }

        boolean reduced = flightClient.reduceSeats(req.getFlightId(), req.getSeats());
        if (!reduced) {
            throw new FlightServiceDownException("Flight Service is DOWN");
        }

        Booking booking = new Booking();
        booking.setFlightId(req.getFlightId());
        booking.setPassengerName(req.getPassengerName());
        booking.setEmail(req.getEmail());
        booking.setSeats(req.getSeats());
        booking.setStatus("CONFIRMED");
        booking.setBookingDate(new Date());

        repository.save(booking);

        // send email notification via RabbitMQ
        emailProducer.sendEmail(new EmailNotification(
                booking.getEmail(),
                "Booking Confirmed",
                "Your booking for flight " + booking.getFlightId() + " is confirmed. Booking id: " + booking.getId()
        ));

        log.info("Booking successful: {}", booking.getId());
        return booking;
    }

    // Fallback for circuit breaker
    public Booking bookFallback(BookingRequest req, Throwable ex) {
        log.warn("Fallback triggered due to: {}", ex.toString());

        Booking fallbackBooking = new Booking();
        fallbackBooking.setPassengerName(req.getPassengerName());
        fallbackBooking.setEmail(req.getEmail());
        fallbackBooking.setFlightId(req.getFlightId());
        fallbackBooking.setSeats(req.getSeats());
        fallbackBooking.setStatus("FAILED");
        fallbackBooking.setBookingDate(new Date());

        return fallbackBooking; 
    }


    public Booking cancelBooking(String id) {
        Booking booking = repository.findById(id).orElseThrow(() -> new BookingNotFoundException(id));
        if ("CANCELLED".equals(booking.getStatus())) {
            return booking;
        }

        booking.setStatus("CANCELLED");
        repository.save(booking);

        // restore seats
        flightClient.increaseSeats(booking.getFlightId(), booking.getSeats());

        // optional: send cancellation email
        emailProducer.sendEmail(new EmailNotification(
                booking.getEmail(),
                "Booking Cancelled",
                "Your booking " + booking.getId() + " has been cancelled"
        ));

        return booking;
    }

    public Booking getBooking(String id) {
        return repository.findById(id).orElseThrow(() -> new BookingNotFoundException(id));
    }

    public List<Booking> getBookingsByEmail(String email) {
        return repository.findByEmail(email);
    }
}
