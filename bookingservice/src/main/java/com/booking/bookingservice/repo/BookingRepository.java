package com.booking.bookingservice.repo;

import com.booking.bookingservice.model.Booking;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface BookingRepository extends MongoRepository<Booking, String> {
    List<Booking> findByEmail(String email);
}
