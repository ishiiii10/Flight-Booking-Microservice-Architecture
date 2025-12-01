package com.chubb.booking.service;

import com.chubb.booking.client.FlightClient; // optional - kept for later
import com.chubb.booking.dto.BookingRequest;
import com.chubb.booking.dto.BookingResponse;
import com.chubb.booking.dto.FlightInfo;
import com.chubb.booking.dto.TicketDetailsResponse;
import com.chubb.booking.enums.BookingStatus;
import com.chubb.booking.enums.TripType;
import com.chubb.booking.model.Booking;
import com.chubb.booking.repository.BookingRepository;
import com.chubb.booking.util.PnrGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final WebClient webClient;           // injected WebClient bean
    private final FlightClient flightClient;     // optional - left in case you want to use Feign later

    private static final Duration CANCELLATION_WINDOW = Duration.ofHours(24);

    @Override
    public Mono<BookingResponse> createBooking(BookingRequest request) {
        // 1) Basic trip-type validations (sizes)
        validateTripTypeCounts(request);

        // 2) Validate passenger count
        if (request.getPassengers() == null || request.getPassengers().isEmpty()) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "passengers.required"));
        }
        if (request.getPassengers().size() > 9) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "max 9 passengers allowed"));
        }

        int seatsNeeded = request.getPassengers().size();

        // 3) Fetch flight infos (sequential blocking via boundedElastic)
        return Mono.fromCallable(() -> request.getFlightIds()
                        .stream()
                        .map(this::fetchFlightOrThrow)    // uses WebClient-based fetch
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
                    List<Mono<Map<String, Object>>> reserveCalls = flightInfos.stream()
                            .map(fi -> Mono.fromCallable(() ->
                                    reserveSeatsOnFlight(fi.getId(), seatsNeeded)
                            ).subscribeOn(Schedulers.boundedElastic()))
                            .collect(Collectors.toList());

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
                            // On any error during reserve, attempt best-effort release
                            .onErrorResume(err -> {
                                log.warn("reserve failed - attempting best-effort release: {}", err.getMessage());
                                flightInfos.forEach(fi -> {
                                    try {
                                        releaseSeatsOnFlight(fi.getId(), seatsNeeded);
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
                        Mono.fromCallable(() -> bk.getFlightIds().stream()
                                .map(this::fetchFlightOrThrow)
                                .collect(Collectors.toList()))
                                .subscribeOn(Schedulers.boundedElastic())
                                .map(flightInfos -> TicketDetailsResponse.builder()
                                        .pnr(bk.getPnr())
                                        .username(bk.getUsername())
                                        .tripType(bk.getTripType())
                                        .flightIds(bk.getFlightIds())
                                        .passengers(bk.getPassengers())
                                        .bookingTime(bk.getBookingTime())
                                        .status(bk.getStatus())
                                        .build())
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

                    // fetch flight infos
                    return Mono.fromCallable(() -> bk.getFlightIds().stream()
                                    .map(this::fetchFlightOrThrow)
                                    .collect(Collectors.toList()))
                            .subscribeOn(Schedulers.boundedElastic())
                            .flatMap(flightInfos -> {
                                // earliest departure
                                Optional<LocalDateTime> earliest = flightInfos.stream()
                                        .map(FlightInfo::getDepartureTime)
                                        .min(LocalDateTime::compareTo);

                                if (earliest.isPresent() && Duration.between(LocalDateTime.now(), earliest.get()).compareTo(CANCELLATION_WINDOW) < 0) {
                                    return Mono.error(new ResponseStatusException(HttpStatus.CONFLICT, "cancellation not allowed within 24 hours"));
                                }

                                int seatsToRelease = bk.getPassengers().size();

                                List<Mono<Map<String, Object>>> releaseCalls = flightInfos.stream()
                                        .map(fi -> Mono.fromCallable(() ->
                                                releaseSeatsOnFlight(fi.getId(), seatsToRelease)
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

    @Override
    public Flux<Booking> listAllBookings() {
        return bookingRepository.findAll();
    }

    // -------------------- helpers --------------------

    private void validateTripTypeCounts(BookingRequest request) {
        TripType t = request.getTripType();
        int n = request.getFlightIds() == null ? 0 : request.getFlightIds().size();
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
            FlightInfo f1 = flightInfos.get(0);
            FlightInfo f2 = flightInfos.get(1);
            if (!f1.getSource().equalsIgnoreCase(f2.getDestination()) || !f1.getDestination().equalsIgnoreCase(f2.getSource())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "round trip segments do not match reverse route");
            }
        } else if (t == TripType.MULTI_CITY) {
            for (int i = 0; i < flightInfos.size() - 1; i++) {
                FlightInfo a = flightInfos.get(i);
                FlightInfo b = flightInfos.get(i + 1);
                if (!a.getDestination().equalsIgnoreCase(b.getSource())) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "multi-city segments are not connected at index " + i);
                }
            }
        }
    }

    /**
     * Fetch flight info by calling Flight service directly via WebClient.
     * Throws ResponseStatusException(BAD_REQUEST) if flight not found (404),
     * throws ResponseStatusException(SERVICE_UNAVAILABLE) for other remote problems.
     */
    private FlightInfo fetchFlightOrThrow(String flightId) {
        String base = System.getProperty("flight.service.url");
        if (base == null || base.isBlank()) {
            base = System.getenv("FLIGHT_SERVICE_URL");
        }
        if (base == null || base.isBlank()) {
            base = "http://localhost:8081";
        }
        String url = base + "/api/flights/" + flightId;

        try {
            FlightInfo fi = Mono.fromCallable(() ->
                    webClient.get()
                            .uri(url)
                            .retrieve()
                            .onStatus(s -> s.is4xxClientError(), resp -> Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "flight.not.found")))
                            .onStatus(s -> s.is5xxServerError(), resp -> Mono.error(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "flight.service.error")))
                            .bodyToMono(FlightInfo.class)
                            .block(Duration.ofSeconds(5))
            ).subscribeOn(Schedulers.boundedElastic()).block();

            if (fi == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "flight not found: " + flightId);
            }
            return fi;

        } catch (WebClientResponseException.NotFound nf) {
            log.warn("Flight not found for id {} : {}", flightId, nf.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "flight not found: " + flightId, nf);
        } catch (WebClientResponseException wce) {
            log.error("WebClient error calling Flight service {} : {} {}", url, wce.getStatusCode(), wce.getResponseBodyAsString(), wce);
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Flight service unavailable for id: " + flightId, wce);
        } catch (ResponseStatusException rse) {
            throw rse;
        } catch (Exception ex) {
            log.error("Unexpected error when calling Flight {} : {}", url, ex.toString(), ex);
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Unable to contact flight service for id: " + flightId, ex);
        }
    }

    private Map<String,Object> reserveSeatsOnFlight(String flightId, int count) {
        String base = System.getProperty("flight.service.url");
        if (base == null || base.isBlank()) base = System.getenv("FLIGHT_SERVICE_URL");
        if (base == null || base.isBlank()) base = "http://localhost:8081";
        String url = base + "/api/flights/" + flightId + "/reserve";

        try {
            Map<String,Object> res = Mono.fromCallable(() ->
                    webClient.post()
                            .uri(url)
                            .bodyValue(Map.of("count", count))
                            .retrieve()
                            .bodyToMono(Map.class)
                            .block(Duration.ofSeconds(5))
            ).subscribeOn(Schedulers.boundedElastic()).block();

            return res == null ? Collections.emptyMap() : res;
        } catch (WebClientResponseException wce) {
            log.error("Reserve seats failed for flight {} : {}", flightId, wce.getMessage(), wce);
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "reserve.failed");
        }
    }

    private Map<String,Object> releaseSeatsOnFlight(String flightId, int count) {
        String base = System.getProperty("flight.service.url");
        if (base == null || base.isBlank()) base = System.getenv("FLIGHT_SERVICE_URL");
        if (base == null || base.isBlank()) base = "http://localhost:8081";
        String url = base + "/api/flights/" + flightId + "/release";

        try {
            Map<String,Object> res = Mono.fromCallable(() ->
                    webClient.post()
                            .uri(url)
                            .bodyValue(Map.of("count", count))
                            .retrieve()
                            .bodyToMono(Map.class)
                            .block(Duration.ofSeconds(5))
            ).subscribeOn(Schedulers.boundedElastic()).block();

            return res == null ? Collections.emptyMap() : res;
        } catch (WebClientResponseException wce) {
            log.warn("Release seats failed for flight {} : {}", flightId, wce.getMessage());
            return Collections.emptyMap();
        }
    }
}