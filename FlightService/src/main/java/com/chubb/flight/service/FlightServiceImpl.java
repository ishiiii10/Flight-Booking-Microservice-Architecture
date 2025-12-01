package com.chubb.flight.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.chubb.flight.dto.FlightRequest;
import com.chubb.flight.enums.Airline;
import com.chubb.flight.enums.City;
import com.chubb.flight.model.Flight;
import com.chubb.flight.repository.FlightRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class FlightServiceImpl implements FlightService {

    private final FlightRepository flightRepository;

    @Override
    public Mono<Flight> addFlight(FlightRequest request) {
        Flight flight = Flight.builder()
                .airline(request.getAirline())
                .flightNumber(request.getFlightNumber())
                .source(request.getSource())
                .destination(request.getDestination())
                .departureTime(request.getDepartureTime())
                .arrivalTime(request.getArrivalTime())
                .totalSeats(request.getTotalSeats())
                .availableSeats(request.getTotalSeats())
                .price(request.getPrice())
                .build();

        return flightRepository.save(flight);
    }

    @Override
    public Flux<Flight> listAll() {
        return flightRepository.findAll();
    }

    @Override
    public Flux<Flight> findByAirline(Airline airline) {
        return flightRepository.findByAirline(airline);
    }

    @Override
    public Flux<Flight> searchByRoute(City source, City destination) {
        return flightRepository.findBySourceAndDestination(source, destination);
    }

    @Override
    public Mono<Flight> findById(String id) {
        return flightRepository.findById(id);
    }
}