package com.booking.bookingservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "flightservice", fallback = FlightClientFallback.class)
public interface FlightClient {

    @GetMapping("/flights/internal/{id}/check")
    boolean checkAvailability(@PathVariable("id") String id);

    @PutMapping("/flights/internal/{id}/reduce")
    boolean reduceSeats(@PathVariable("id") String id, @RequestParam("count") int count);

    @PutMapping("/flights/internal/{id}/increase")
    void increaseSeats(@PathVariable("id") String id, @RequestParam("count") int count);
}
