package com.booking.bookingservice.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Document(collection = "bookings")
public class Booking {
    @Id
    private String id;
    private String flightId;
    private String passengerName;
    private String email;
    private int seats;
    private String status; // CONFIRMED / CANCELLED
    private Date bookingDate;
}
