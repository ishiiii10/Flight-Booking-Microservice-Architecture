package com.chubb.booking.repository;


import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.chubb.booking.model.Booking;

import reactor.core.publisher.Mono;

public interface BookingRepository extends ReactiveMongoRepository<Booking, String> {

    Mono<Booking> findByPnr(String pnr);
}