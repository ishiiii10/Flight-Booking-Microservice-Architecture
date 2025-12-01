package com.chubb.booking.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.chubb.booking.dto.FlightInfo;

import java.util.Map;

@FeignClient(name = "FlightService")
public interface FlightClient {

    @GetMapping("/api/flights/{id}")
    FlightInfo getFlightById(@PathVariable String id);

    @PostMapping("/api/flights/{id}/reserve")
    Map<String, Object> reserveSeats(@PathVariable String id, @RequestBody Map<String, Integer> body);

    @PostMapping("/api/flights/{id}/release")
    Map<String, Object> releaseSeats(@PathVariable String id, @RequestBody Map<String, Integer> body);
}