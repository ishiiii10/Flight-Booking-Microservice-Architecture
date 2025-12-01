package com.chubb.booking.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import com.chubb.booking.dto.FlightInfo;

import reactor.core.publisher.Mono; // not used here; Feign is blocking

import java.util.Map;

/**
 * Feign client talking to flight-service.
 * The booking-service will wrap calls to this client inside Reactor's boundedElastic scheduler.
 *
 * Make sure flight-service app name matches "flight-service" in Eureka / application.yml.
 */
@FeignClient(name = "flight-service", url = "${flight.service.url:http://localhost:8081}")
public interface FlightClient {

    @GetMapping("/api/flights/{id}")
    FlightInfo getFlightById(@PathVariable("id") String id);

    // Reserve seats; Flight Service must implement these endpoints.
    @PostMapping("/api/flights/{id}/reserve")
    Map<String, Object> reserveSeats(@PathVariable("id") String id, @RequestBody Map<String, Integer> body);

    @PostMapping("/api/flights/{id}/release")
    Map<String, Object> releaseSeats(@PathVariable("id") String id, @RequestBody Map<String, Integer> body);
}
