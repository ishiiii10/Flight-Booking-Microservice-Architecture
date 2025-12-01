package com.flight.flightservice.repo;

import com.flight.flightservice.model.Flight;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface FlightRepository extends MongoRepository<Flight, String> {

    List<Flight> findBySourceAndDestinationAndDate(String source, String destination, String date);

}
