package com.booking.bookingservice.feign;

import org.springframework.stereotype.Component;

@Component
public class FlightClientFallback implements FlightClient {

    @Override
    public boolean checkAvailability(String id) {
        return false;  // service is down
    }

    @Override
    public boolean reduceSeats(String id, int count) {
        return false;  // service is down
    }

    @Override
    public void increaseSeats(String id, int count) {
        // do nothing
    }
}

