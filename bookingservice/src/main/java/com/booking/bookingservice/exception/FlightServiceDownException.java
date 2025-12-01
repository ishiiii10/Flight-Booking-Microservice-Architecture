package com.booking.bookingservice.exception;

public class FlightServiceDownException extends RuntimeException {
    public FlightServiceDownException(String message) {
        super(message);
    }
}
