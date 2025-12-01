package com.chubb.flight.repository;


import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.chubb.flight.model.Airline;
import com.chubb.flight.model.Flight;

import reactor.core.publisher.Flux;

public interface FlightRepository extends ReactiveMongoRepository<Flight, String> {
    Flux<Flight> findByAirline(Airline airline);
    Flux<Flight> findBySourceIgnoreCaseAndDestinationIgnoreCase(String source, String destination);
}
