package com.chubb.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.chubb.booking.client.FlightClient;
import com.chubb.booking.dto.BookingRequest;
import com.chubb.booking.dto.BookingResponse;
import com.chubb.booking.dto.FlightInfo;
import com.chubb.booking.dto.TicketDetailsResponse;
import com.chubb.booking.enums.BookingStatus;
import com.chubb.booking.enums.TripType;
import com.chubb.booking.model.Booking;
import com.chubb.booking.repository.BookingRepository;
import com.chubb.booking.util.PnrGenerator;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final FlightClient flightClient;

    private static final Duration CANCELLATION_WINDOW = Duration.ofHours(24);

    @Override
    public Mono<BookingResponse> createBooking(BookingRequest request) {

        // 1) Basic trip-type validations (sizes)
        validateTripTypeCounts(request);

        // 2) Validate passenger count (already done by @Size) - additional check
        if (request.getPassengers().size() > 9) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "max 9 passengers allowed"));
        }

        int seatsNeeded = request.getPassengers().size();

        // 3) Validate that all flights exist and have enough seats (calls to flight service)
        // We'll fetch flight infos in sequence and check.
        return Mono.fromCallable(() -> request.getFlightIds()
                    .stream()
                    .map(id -> {
                        // blocking Feign call — run on boundedElastic
                        FlightInfo fi = flightClient.getFlightById(id);
                        if (fi == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "flight not found: " + id);
                        return fi;
                    })
                    .collect(Collectors.toList()))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(flightInfos -> {

                    // Validate route connectivity for round trip / multi-city
                	validateSegmentsLogic(request, flightInfos);

                    // Check seat availability across each segment
                    boolean allHaveSeats = flightInfos.stream()
                            .allMatch(fi -> fi.getAvailableSeats() >= seatsNeeded);
                    if (!allHaveSeats) {
                        return Mono.error(new ResponseStatusException(HttpStatus.CONFLICT, "not enough seats on one or more flights"));
                    }

                    // 4) Reserve seats on each flight by calling flight-service reserve endpoint
                    List<Mono<Map<String,Object>>> reserveCalls = flightInfos.stream()
                            .map(fi ->
                                Mono.fromCallable(() ->
                                    flightClient.reserveSeats(fi.getId(), Collections.singletonMap("count", seatsNeeded))
                                ).subscribeOn(Schedulers.boundedElastic())
                            ).collect(Collectors.toList());

                    // combine all reserve calls
                    return Mono.when(reserveCalls)
                            .then(Mono.defer(() -> {
                                // 5) Create Booking record
                                Booking booking = Booking.builder()
                                        .username(request.getUsername())
                                        .tripType(request.getTripType())
                                        .flightIds(request.getFlightIds())
                                        .passengers(request.getPassengers())
                                        .pnr(PnrGenerator.generatePNR())
                                        .status(BookingStatus.BOOKED)
                                        .bookingTime(LocalDateTime.now())
                                        .build();

                                return bookingRepository.save(booking)
                                        .map(b -> BookingResponse.builder()
                                                .pnr(b.getPnr())
                                                .bookingId(b.getId())
                                                .flightIds(b.getFlightIds())
                                                .message("booking.created")
                                                .build());
                            }))
                            // If reserve fails, release any reserved seats (best-effort). Note: Feign may throw exceptions that will skip here.
                            .onErrorResume(err -> {
                                // attempt to rollback: call release on any flights that might have reserved seats
                                // best-effort: fire-and-forget on boundedElastic
                                flightInfos.forEach(fi -> {
                                    try {
                                        flightClient.releaseSeats(fi.getId(), Collections.singletonMap("count", seatsNeeded));
                                    } catch (Exception ignore) {}
                                });
                                return Mono.error(err);
                            });
                });
    }

    @Override
    public Mono<TicketDetailsResponse> getTicketByPnr(String pnr) {
        return bookingRepository.findByPnr(pnr)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "pnr.not.found")))
                .flatMap(bk ->
                    // fetch flight info for all flights in booking
                    Mono.fromCallable(() -> bk.getFlightIds().stream()
                            .map(id -> flightClient.getFlightById(id))
                            .collect(Collectors.toList()))
                        .subscribeOn(Schedulers.boundedElastic())
                        .map(flightInfos -> {
                            // Map to TicketDetailsResponse
                            return TicketDetailsResponse.builder()
                                    .pnr(bk.getPnr())
                                    .username(bk.getUsername())
                                    .tripType(bk.getTripType())
                                    .flightIds(bk.getFlightIds())
                                    .passengers(bk.getPassengers())
                                    .bookingTime(bk.getBookingTime())
                                    .status(bk.getStatus())
                                    .build();
                        })
                );
    }

    @Override
    public Mono<Void> cancelBooking(String pnr) {
        return bookingRepository.findByPnr(pnr)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "pnr.not.found")))
                .flatMap(bk -> {
                    if (bk.getStatus() == BookingStatus.CANCELLED) {
                        return Mono.error(new ResponseStatusException(HttpStatus.CONFLICT, "booking.already.cancelled"));
                    }

                    // Determine earliest departure among segments
                    return Mono.fromCallable(() -> bk.getFlightIds().stream()
                                    .map(id -> flightClient.getFlightById(id))
                                    .collect(Collectors.toList()))
                            .subscribeOn(Schedulers.boundedElastic())
                            .flatMap(flightInfos -> {
                                // find earliest departure time
                                Optional<LocalDateTime> earliest = flightInfos.stream()
                                        .map(FlightInfo::getDepartureTime)
                                        .min(LocalDateTime::compareTo);

                                if (earliest.isPresent() && Duration.between(LocalDateTime.now(), earliest.get()).compareTo(CANCELLATION_WINDOW) < 0) {
                                    return Mono.error(new ResponseStatusException(HttpStatus.CONFLICT, "cancellation not allowed within 24 hours"));
                                }

                                int seatsToRelease = bk.getPassengers().size();

                                // release seats on each flight
                                List<Mono<Map<String,Object>>> releaseCalls = flightInfos.stream()
                                        .map(fi -> Mono.fromCallable(() ->
                                                flightClient.releaseSeats(fi.getId(), Collections.singletonMap("count", seatsToRelease))
                                        ).subscribeOn(Schedulers.boundedElastic()))
                                        .collect(Collectors.toList());

                                return Mono.when(releaseCalls)
                                        .then(Mono.defer(() -> {
                                            bk.setStatus(BookingStatus.CANCELLED);
                                            return bookingRepository.save(bk).then();
                                        }));
                            });
                });
    }

    // -------------------- helper methods --------------------

    private void validateTripTypeCounts(BookingRequest request) {
        TripType t = request.getTripType();
        int n = request.getFlightIds().size();
        if (t == TripType.ONE_WAY && n != 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ONE_WAY requires exactly 1 flightId");
        }
        if (t == TripType.ROUND_TRIP && n != 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ROUND_TRIP requires exactly 2 flightIds");
        }
        if (t == TripType.MULTI_CITY && n < 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "MULTI_CITY requires at least 2 flightIds");
        }
    }

    private void validateSegmentsLogic(BookingRequest request, List<FlightInfo> flightInfos) {
        TripType t = request.getTripType();

        if (t == TripType.ROUND_TRIP) {
            // flight1: A->B ; flight2: B->A
            FlightInfo f1 = flightInfos.get(0);
            FlightInfo f2 = flightInfos.get(1);
            if (!f1.getSource().equalsIgnoreCase(f2.getDestination()) || !f1.getDestination().equalsIgnoreCase(f2.getSource())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "round trip segments do not match reverse route");
            }
        } else if (t == TripType.MULTI_CITY) {
            // each segment must connect: flight[i].destination == flight[i+1].source
            for (int i = 0; i < flightInfos.size() - 1; i++) {
                FlightInfo a = flightInfos.get(i);
                FlightInfo b = flightInfos.get(i + 1);
                if (!a.getDestination().equalsIgnoreCase(b.getSource())) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "multi-city segments are not connected at index " + i);
                }
            }
        }
    }
}