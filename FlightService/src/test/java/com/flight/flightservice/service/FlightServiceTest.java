package com.flight.flightservice.service;

import com.flight.flightservice.exception.FlightNotFoundException;
import com.flight.flightservice.model.Flight;
import com.flight.flightservice.repo.FlightRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FlightServiceTest {

    private FlightRepository repository;
    private FlightService service;

    @BeforeEach
    void setup() {
        repository = Mockito.mock(FlightRepository.class);
        service = new FlightService(repository);
    }

    @Test
    void testAddFlight() {
        Flight flight = new Flight();
        flight.setFlightNumber("AI-202");

        when(repository.save(flight)).thenReturn(flight);

        Flight saved = service.addFlight(flight);

        assertEquals("AI-202", saved.getFlightNumber());
        verify(repository, times(1)).save(flight);
    }

    @Test
    void testSearchFlights() {
        when(repository.findBySourceAndDestinationAndDate("DEL", "BOM", "2025-01-01"))
                .thenReturn(List.of(new Flight()));

        List<Flight> result = service.searchFlights("DEL", "BOM", "2025-01-01");

        assertEquals(1, result.size());
    }

    @Test
    void testGetFlightSuccess() {
        Flight flight = new Flight();
        flight.setId("123");

        when(repository.findById("123")).thenReturn(Optional.of(flight));

        Flight result = service.getFlight("123");

        assertNotNull(result);
        assertEquals("123", result.getId());
    }

    @Test
    void testGetFlightNotFound() {
        when(repository.findById("999")).thenReturn(Optional.empty());

        assertThrows(FlightNotFoundException.class, () -> service.getFlight("999"));
    }

    @Test
    void testReduceSeatsSuccess() {
        Flight flight = new Flight();
        flight.setAvailableSeats(50);

        when(repository.findById("1")).thenReturn(Optional.of(flight));

        boolean result = service.reduceSeats("1", 10);

        assertTrue(result);
        assertEquals(40, flight.getAvailableSeats());
        verify(repository, times(1)).save(flight);
    }

    @Test
    void testReduceSeatsFailure() {
        Flight flight = new Flight();
        flight.setAvailableSeats(5);

        when(repository.findById("1")).thenReturn(Optional.of(flight));

        boolean result = service.reduceSeats("1", 10);

        assertFalse(result);
        verify(repository, never()).save(flight);
    }

    @Test
    void testIncreaseSeats() {
        Flight flight = new Flight();
        flight.setAvailableSeats(20);

        when(repository.findById("55")).thenReturn(Optional.of(flight));

        service.increaseSeats("55", 5);

        assertEquals(25, flight.getAvailableSeats());
        verify(repository, times(1)).save(flight);
    }
}
