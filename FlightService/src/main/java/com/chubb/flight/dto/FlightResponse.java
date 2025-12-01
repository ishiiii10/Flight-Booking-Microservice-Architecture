package com.chubb.flight.dto;


import com.chubb.flight.enums.Airline;
import com.chubb.flight.enums.City;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlightResponse {
    private String id;
    private Airline airline;
    private String flightNumber;
    private City source;
    private City destination;
}
