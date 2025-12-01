package com.flight.flightservice.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "flights")
public class Flight {
    @Id
    private String id;
    private String flightNumber;
    private String airline;
    private String source;
    private String destination;
    private String date;
    private int availableSeats;
}
