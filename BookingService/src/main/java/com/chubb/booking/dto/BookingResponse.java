package com.chubb.booking.dto;



import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingResponse {

    private String pnr;

    private String message;

    // optional for confirmation page
    private String bookingId;

    private List<String> flightIds;
}
