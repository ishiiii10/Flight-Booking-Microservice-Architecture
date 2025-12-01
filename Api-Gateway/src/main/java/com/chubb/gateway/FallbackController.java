package com.chubb.gateway;



import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
public class FallbackController {

    @RequestMapping("/flight-fallback")
    public ResponseEntity<?> flightFallback() {
        return ResponseEntity.status(503).body(Map.of("code","503 SERVICE_UNAVAILABLE", "message","Flight service currently unavailable"));
    }

    @RequestMapping("/booking-fallback")
    public ResponseEntity<?> bookingFallback() {
        return ResponseEntity.status(503).body(Map.of("code","503 SERVICE_UNAVAILABLE", "message","Booking service currently unavailable"));
    }
}