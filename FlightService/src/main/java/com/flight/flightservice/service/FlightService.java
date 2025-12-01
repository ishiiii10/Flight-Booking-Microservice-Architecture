package com.flight.flightservice.service;

import com.flight.flightservice.exception.FlightNotFoundException;
import com.flight.flightservice.model.Flight;
import com.flight.flightservice.repo.FlightRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FlightService {

    private static final Logger log = LoggerFactory.getLogger(FlightService.class);

    private final FlightRepository repository;

    public FlightService(FlightRepository repository) {
        this.repository = repository;
    }

    public Flight addFlight(Flight flight) {
        log.info("Adding flight: {}", flight.getFlightNumber());
        return repository.save(flight);
    }

    public List<Flight> searchFlights(String source, String destination, String date) {
        log.info("Searching flights from {} to {} on {}", source, destination, date);
        return repository.findBySourceAndDestinationAndDate(source, destination, date);
    }

    public Flight getFlight(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new FlightNotFoundException(id));
    }

    public boolean reduceSeats(String id, int count) {
        log.info("Reducing {} seats from flight {}", count, id);

        Flight flight = getFlight(id);

        if (flight.getAvailableSeats() < count) {
            log.warn("Not enough seats available for flight {}. Requested: {}, Available: {}",
                    id, count, flight.getAvailableSeats());
            return false;
        }

        flight.setAvailableSeats(flight.getAvailableSeats() - count);
        repository.save(flight);

        log.info("Successfully reduced seats. Remaining seats: {}", flight.getAvailableSeats());
        return true;
    }

    public void increaseSeats(String id, int count) {
        log.info("Increasing {} seats for flight {}", count, id);

        Flight flight = getFlight(id);

        flight.setAvailableSeats(flight.getAvailableSeats() + count);
        repository.save(flight);

        log.info("Successfully increased seats. New seat count: {}", flight.getAvailableSeats());
    }
}
