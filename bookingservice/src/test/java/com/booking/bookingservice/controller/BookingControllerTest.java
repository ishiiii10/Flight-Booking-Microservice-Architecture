package com.booking.bookingservice.controller;

import com.booking.bookingservice.dto.BookingRequest;
import com.booking.bookingservice.exception.GlobalExceptionHandler;
import com.booking.bookingservice.model.Booking;
import com.booking.bookingservice.service.BookingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;

import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(controllers = BookingController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
public class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;

    @Autowired
    private ObjectMapper objectMapper;

    // --------------------------------------------------
    // 1. CREATE BOOKING TEST
    // --------------------------------------------------
    @Test
    void testCreateBooking_Success() throws Exception {

        BookingRequest request = new BookingRequest(
                "FL123", "John Doe", "john@gmail.com", 2
        );

        Booking mockBooking = new Booking();
        mockBooking.setId("B001");
        mockBooking.setFlightId("FL123");
        mockBooking.setPassengerName("John Doe");
        mockBooking.setEmail("john@gmail.com");
        mockBooking.setSeats(2);
        mockBooking.setStatus("CONFIRMED");
        mockBooking.setBookingDate(new Date());

        when(bookingService.bookTicket(any())).thenReturn(mockBooking);

        mockMvc.perform(post("/booking/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value("B001"))
        .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    // --------------------------------------------------
    // 2. GET BOOKING BY ID
    // --------------------------------------------------
    @Test
    void testGetBookingById() throws Exception {

        Booking booking = new Booking();
        booking.setId("B123");
        booking.setFlightId("FL999");
        booking.setPassengerName("Alice");
        booking.setEmail("alice@mail.com");
        booking.setSeats(1);
        booking.setStatus("CONFIRMED");
        booking.setBookingDate(new Date());

        when(bookingService.getBooking("B123")).thenReturn(booking);

        mockMvc.perform(get("/booking/B123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("B123"))
                .andExpect(jsonPath("$.passengerName").value("Alice"));
    }

    // --------------------------------------------------
    // 3. GET BOOKINGS BY EMAIL
    // --------------------------------------------------
    @Test
    void testGetBookingsByEmail() throws Exception {

        Booking b1 = new Booking();
        b1.setId("B101");
        b1.setPassengerName("Kam");
        b1.setEmail("a@mail.com");

        Booking b2 = new Booking();
        b2.setId("B102");
        b2.setPassengerName("Sam");
        b2.setEmail("a@mail.com");

        List<Booking> mockList = Arrays.asList(b1, b2);

        when(bookingService.getBookingsByEmail("a@mail.com"))
                .thenReturn(mockList);

        mockMvc.perform(get("/booking/history")
                .param("email", "a@mail.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

    }


    // --------------------------------------------------
    // 4. CANCEL BOOKING
    // --------------------------------------------------
    @Test
    void testCancelBooking() throws Exception {

        Booking cancelled = new Booking();
        cancelled.setId("B500");
        cancelled.setStatus("CANCELLED");

        when(bookingService.cancelBooking("B500")).thenReturn(cancelled);

        mockMvc.perform(put("/booking/cancel/B500"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    // --------------------------------------------------
    // 5. flightservice DOWN → SERVICE RETURNS 503
    // --------------------------------------------------
    @Test
    void testCreateBooking_FlightServiceDown() throws Exception {

        BookingRequest request = new BookingRequest(
                "FL555", "Ravi", "ravi@mail.com", 1
        );

        when(bookingService.bookTicket(any()))
                .thenThrow(new RuntimeException("Flight Service is DOWN"));

        mockMvc.perform(post("/booking/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isServiceUnavailable())
        .andExpect(content().string("Flight Service is DOWN"));
    }
}