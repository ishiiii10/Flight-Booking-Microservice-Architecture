package com.chubb.gateway.controller;



import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
public class FallbackController {

    @RequestMapping("/fallback")
    public Mono<ResponseEntity<Map<String, String>>> fallback() {
        return Mono.just(ResponseEntity.status(503).body(Map.of(
                "message", "service.unavailable",
                "detail", "The downstream service is unavailable. Please try again later."
        )));
    }
}