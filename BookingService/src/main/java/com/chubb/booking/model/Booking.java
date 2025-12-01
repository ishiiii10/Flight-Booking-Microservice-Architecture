package com.chubb.booking.model;


import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.chubb.booking.enums.BookingStatus;
import com.chubb.booking.enums.TripType;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "bookings")
public class Booking {

    @Id
    private String id;

    @NotBlank(message = "Username is required")
    private String username;

    // ONE_WAY, ROUND_TRIP, MULTI_CITY
    private TripType tripType;

    /**
     * List of flight IDs (one entry for ONE_WAY, two for ROUND_TRIP, >=2 for MULTI_CITY).
     * Logical constraints (size checks per tripType) will be enforced in service layer.
     */
    @NotEmpty(message = "At least one flight id is required")
    private List<@NotBlank(message = "FlightId cannot be blank") String> flightIds;

    /**
     * Embedded passengers. Validate each passenger using @Valid; service layer will enforce
     * additional rules like max passengers if required.
     */
    @Size(max = 9, message = "A maximum of 9 passengers are allowed in one booking")
    @NotEmpty(message = "At least one passenger is required")
    private List<@Valid Passenger> passengers;

    // generated PNR (e.g. 6-char)
    private String pnr;

    private BookingStatus status;

    private LocalDateTime bookingTime;
}