package com.chubb.flight.model;



import com.chubb.flight.enums.Airline;
import com.chubb.flight.enums.City;
import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "flights")
public class Flight {

    @Id
    private String id;

    @NotNull(message = "Airline must be provided")
    private Airline airline;

    @NotBlank(message = "Flight number is required")
    private String flightNumber;

    @NotBlank(message = "Source is required")
    public City source;

    @NotBlank(message = "Destination is required")
    public City destination;

    /**
     * Expect ISO-8601 string from client, e.g. "2025-12-10T10:30:00"
     * If you prefer another format, change the pattern.
     */
    @NotNull(message = "Departure time is required")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime departureTime;

    @NotNull(message = "Arrival time is required")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime arrivalTime;

    @Min(value = 1, message = "Total seats must be at least 1")
    private int totalSeats;

    @Min(value = 0, message = "Available seats cannot be negative")
    private int availableSeats;

    @PositiveOrZero(message = "Price must be zero or positive")
    private double price;
}