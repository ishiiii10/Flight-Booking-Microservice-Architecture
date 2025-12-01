package com.flight.flightservice.controller;

import com.flight.flightservice.model.Flight;
import com.flight.flightservice.service.FlightService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/flights")
public class FlightController {

    private static final Logger log = LoggerFactory.getLogger(FlightController.class);

    private final FlightService service;

    public FlightController(FlightService service) {
        this.service = service;
    }

    // -------------------- ADD FLIGHT --------------------
    @PostMapping("/add")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add a new flight", description = "Creates a new flight record")
    @ApiResponse(responseCode = "201", description = "Flight created successfully")
    public Flight addFlight(@RequestBody Flight flight) {
        log.info("Request received to add flight");
        return service.addFlight(flight);
    }

    // -------------------- SEARCH FLIGHTS --------------------
    @GetMapping("/search")
    @Operation(summary = "Search flights", description = "Searches flights by source, destination and date")
    public List<Flight> searchFlights(
            @RequestParam String source,
            @RequestParam String destination,
            @RequestParam String date) {
        log.info("Search request received for flights {} -> {} on {}", source, destination, date);
        return service.searchFlights(source, destination, date);
    }

    // -------------------- CHECK SEAT AVAILABILITY (INTERNAL) --------------------
    @GetMapping("/internal/{id}/check")
    @Operation(summary = "Check flight availability (internal API)")
    public boolean checkAvailability(@PathVariable String id) {
        log.info("Checking availability for flight {}", id);
        Flight flight = service.getFlight(id);
        return flight.getAvailableSeats() > 0;
    }

    // -------------------- REDUCE SEATS (INTERNAL) --------------------
    @PutMapping("/internal/{id}/reduce")
    @Operation(summary = "Reduce seats (internal API)")
    public boolean reduceSeats(@PathVariable String id, @RequestParam int count) {
        log.info("Request received to reduce seats for flight {}", id);
        return service.reduceSeats(id, count);
    }

    // -------------------- INCREASE SEATS (INTERNAL) --------------------
    @PutMapping("/internal/{id}/increase")
    @Operation(summary = "Increase seats (internal API)")
    public void increaseSeats(@PathVariable String id, @RequestParam int count) {
        log.info("Request received to increase seats for flight {}", id);
        service.increaseSeats(id, count);
    }
}
