package com.chubb.booking.dto;

import java.util.List;

import com.chubb.booking.enums.TripType;
import com.chubb.booking.model.Passenger;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingRequest {

    @NotBlank(message = "Username is required")
    private String username;

    @NotNull(message = "Trip type is required")
    private TripType tripType;

    @NotEmpty(message = "At least one flight ID is required")
    private List<@NotBlank(message = "Flight ID cannot be empty") String> flightIds;

    @Size(max = 9, message = "A maximum of 9 passengers are allowed")
    @NotEmpty(message = "At least one passenger is required")
    private List<@Valid Passenger> passengers;
}