package com.chubb.booking.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import com.chubb.booking.model.Booking;
import reactor.core.publisher.Mono;

public interface BookingRepository extends ReactiveCrudRepository<Booking, String> {
    Mono<Booking> findByPnr(String pnr);
}