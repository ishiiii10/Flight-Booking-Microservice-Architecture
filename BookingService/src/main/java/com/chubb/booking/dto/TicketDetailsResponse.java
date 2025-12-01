package com.chubb.booking.dto;


import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

import com.chubb.booking.enums.BookingStatus;
import com.chubb.booking.enums.TripType;
import com.chubb.booking.model.Passenger;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketDetailsResponse {

    private String pnr;

    private String username;

    private TripType tripType;

    private List<String> flightIds;

    private List<Passenger> passengers;

    private LocalDateTime bookingTime;

    private BookingStatus status;
}