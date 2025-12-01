package com.booking.bookingservice.service;

import com.booking.bookingservice.dto.BookingRequest;
import com.booking.bookingservice.dto.EmailNotification;
import com.booking.bookingservice.exception.FlightServiceDownException;
import com.booking.bookingservice.feign.FlightClient;
import com.booking.bookingservice.model.Booking;
import com.booking.bookingservice.producer.EmailProducer;
import com.booking.bookingservice.repo.BookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class BookingServiceTest {

    private BookingRepository bookingRepository;
    private FlightClient flightClient;
    private EmailProducer emailProducer;
    private BookingService bookingService;

    @BeforeEach
    void setUp() {
        bookingRepository = mock(BookingRepository.class);
        flightClient = mock(FlightClient.class);
        emailProducer = mock(EmailProducer.class);

        bookingService = new BookingService(bookingRepository, flightClient, emailProducer);
    }

    // --------------------------------------------------------------------
    // 1. SUCCESSFUL BOOKING
    // --------------------------------------------------------------------
    @Test
    void testBookTicket_Success() {
        BookingRequest request = new BookingRequest("FL123", "John", "john@gmail.com", 2);

        when(flightClient.checkAvailability("FL123")).thenReturn(true);
        when(flightClient.reduceSeats("FL123", 2)).thenReturn(true);

        Booking result = bookingService.bookTicket(request);

        assertEquals("CONFIRMED", result.getStatus());
        verify(bookingRepository, times(1)).save(any(Booking.class));
        verify(emailProducer, times(1)).sendEmail(any(EmailNotification.class));
    }

    // --------------------------------------------------------------------
    // 2. FLIGHT SERVICE DOWN → FALLBACK SIGNAL (false returned)
    // --------------------------------------------------------------------
    @Test
    void testBookTicket_FlightServiceDown() {
        BookingRequest request = new BookingRequest("FL123", "John", "john@gmail.com", 2);

        // fallback returns false → service down
        when(flightClient.checkAvailability("FL123")).thenReturn(false);

        Exception ex = assertThrows(FlightServiceDownException.class,
                () -> bookingService.bookTicket(request));

        assertEquals("Flight Service is DOWN", ex.getMessage());
    }

    // --------------------------------------------------------------------
    // 3. SEATS NOT AVAILABLE (availability = true but reduceSeats = false)
    // --------------------------------------------------------------------
    @Test
    void testBookTicket_SeatsNotAvailable() {
        BookingRequest request = new BookingRequest("FL123", "John", "john@gmail.com", 2);

        when(flightClient.checkAvailability("FL123")).thenReturn(true);
        when(flightClient.reduceSeats("FL123", 2)).thenReturn(false);

        Exception ex = assertThrows(FlightServiceDownException.class,
                () -> bookingService.bookTicket(request));

        assertEquals("Flight Service is DOWN", ex.getMessage());
    }

    // --------------------------------------------------------------------
    // 4. CANCEL BOOKING SUCCESSFULLY
    // --------------------------------------------------------------------
    @Test
    void testCancelBooking_Success() {

        Booking existing = new Booking();
        existing.setId("B1");
        existing.setFlightId("FL123");
        existing.setSeats(2);
        existing.setStatus("CONFIRMED");

        when(bookingRepository.findById("B1")).thenReturn(Optional.of(existing));

        Booking result = bookingService.cancelBooking("B1");

        assertEquals("CANCELLED", result.getStatus());
        verify(flightClient, times(1)).increaseSeats("FL123", 2);
        verify(emailProducer, times(1)).sendEmail(any(EmailNotification.class));
    }

    // --------------------------------------------------------------------
    // 5. CANCEL BOOKING ALREADY CANCELLED — should not update again
    // --------------------------------------------------------------------
    @Test
    void testCancelBooking_AlreadyCancelled() {

        Booking existing = new Booking();
        existing.setId("B1");
        existing.setStatus("CANCELLED");

        when(bookingRepository.findById("B1")).thenReturn(Optional.of(existing));

        Booking result = bookingService.cancelBooking("B1");

        assertEquals("CANCELLED", result.getStatus());
        verify(flightClient, never()).increaseSeats(anyString(), anyInt());
    }

}