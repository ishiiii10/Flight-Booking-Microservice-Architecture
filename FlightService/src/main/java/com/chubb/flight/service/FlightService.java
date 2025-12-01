package com.chubb.flight.service;


import com.chubb.flight.dto.FlightRequest;
import com.chubb.flight.enums.Airline;
import com.chubb.flight.enums.City;
import com.chubb.flight.model.Flight;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FlightService {

    Mono<Flight> addFlight(FlightRequest request);

    Flux<Flight> listAll();

    Flux<Flight> findByAirline(Airline airline);

    Flux<Flight> searchByRoute(City source, City destination);

    Mono<Flight> findById(String id);
}