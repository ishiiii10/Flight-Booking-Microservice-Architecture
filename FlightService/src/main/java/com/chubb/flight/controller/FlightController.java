package com.chubb.flight.controller;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.chubb.flight.dto.FlightRequest;
import com.chubb.flight.enums.Airline;
import com.chubb.flight.enums.City;
import com.chubb.flight.model.Flight;
import com.chubb.flight.service.FlightService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


import java.util.Map;

@RestController
@RequestMapping("/api/flights")
@RequiredArgsConstructor
@Validated
public class FlightController {

    private final FlightService flightService;

    /**
     * CREATE FLIGHT → 201 CREATED
     */
    @PostMapping
    public Mono<ResponseEntity<Map<String, String>>> addFlight(@Valid @RequestBody FlightRequest request) {
        return flightService.listAll()
                .filter(f -> f.getFlightNumber().equalsIgnoreCase(request.getFlightNumber())
                        && f.getAirline().equals(request.getAirline()))
                .hasElements()
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.just(ResponseEntity
                                .status(HttpStatus.CONFLICT)
                                .body(Map.of(
                                        "message", "flight already exists for this airline"
                                )));
                    }
                    return flightService.addFlight(request)
                            .map(flight -> ResponseEntity
                                    .status(HttpStatus.CREATED)
                                    .body(Map.of(
                                            "id", flight.getId(),
                                            "message", "flight.created"
                                    )));
                });
    }

    /**
     * LIST FLIGHTS → 200 OK
     */
    @GetMapping
    public Mono<ResponseEntity<Flux<Map<String, String>>>> listAll(
            @RequestParam(value = "source", required = false) City source,
            @RequestParam(value = "destination", required = false) City destination,
            @RequestParam(value = "airline", required = false) Airline airline) {

        Flux<Flight> result;

        if (airline != null) {
            result = flightService.findByAirline(airline);
        } else if (source != null && destination != null) {
            result = flightService.searchByRoute(source, destination);
        } else {
            result = flightService.listAll();
        }

        Flux<Map<String, String>> response = result.map(f ->
                Map.of("id", f.getId(), "flightNumber", f.getFlightNumber())
        );

        return Mono.just(ResponseEntity.ok(response));
    }

    /**
     * GET BY ID → 200 OK or 404 NOT FOUND
     */
    @GetMapping("/{id}")
    public Mono<ResponseEntity<Map<String, String>>> getById(@PathVariable String id) {
        return flightService.findById(id)
                .map(f -> ResponseEntity.ok(
                        Map.of(
                                "id", f.getId(),
                                "flightNumber", f.getFlightNumber()
                        )))
                .defaultIfEmpty(ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "flight.not.found")));
    }
}