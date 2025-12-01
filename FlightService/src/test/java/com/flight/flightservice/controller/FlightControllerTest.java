package com.flight.flightservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flight.flightservice.model.Flight;
import com.flight.flightservice.service.FlightService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FlightController.class)
class FlightControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FlightService service;

    @Autowired
    private ObjectMapper mapper;

    @Test
    void testAddFlight() throws Exception {
        Flight flight = new Flight();
        flight.setFlightNumber("AI-202");

        when(service.addFlight(any())).thenReturn(flight);

        mockMvc.perform(post("/flights/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(flight)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.flightNumber").value("AI-202"));
    }

    @Test
    void testSearchFlights() throws Exception {
        when(service.searchFlights("DEL", "BOM", "2025-01-01"))
                .thenReturn(List.of(new Flight()));

        mockMvc.perform(get("/flights/search")
                        .param("source", "DEL")
                        .param("destination", "BOM")
                        .param("date", "2025-01-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void testCheckAvailability() throws Exception {
        Flight flight = new Flight();
        flight.setAvailableSeats(20);

        when(service.getFlight("1")).thenReturn(flight);

        mockMvc.perform(get("/flights/internal/1/check"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void testReduceSeats() throws Exception {
        when(service.reduceSeats("1", 5)).thenReturn(true);

        mockMvc.perform(put("/flights/internal/1/reduce?count=5"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void testIncreaseSeats() throws Exception {
        mockMvc.perform(put("/flights/internal/1/increase?count=5"))
                .andExpect(status().isOk());
    }
}
