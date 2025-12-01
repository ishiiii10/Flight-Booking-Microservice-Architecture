package com.chubb.booking.service;

import com.chubb.booking.dto.BookingRequest;
import com.chubb.booking.dto.BookingResponse;
import com.chubb.booking.dto.TicketDetailsResponse;

import reactor.core.publisher.Mono;

public interface BookingService {
    Mono<BookingResponse> createBooking(BookingRequest request);
    Mono<TicketDetailsResponse> getTicketByPnr(String pnr);
    Mono<Void> cancelBooking(String pnr);
}