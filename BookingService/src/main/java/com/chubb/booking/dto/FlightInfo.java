package com.chubb.booking.dto;


import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlightInfo {
    private String id;
    private String flightNumber;
    private String airline;       // e.g. "INDIGO"
    private String source;        // e.g. "BHUBANESWAR"
    private String destination;   // e.g. "BENGALURU"
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private int totalSeats;
    private int availableSeats;
    private double price;
}