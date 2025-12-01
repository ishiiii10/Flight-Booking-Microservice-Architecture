package com.chubb.booking.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    // Provide a WebClient.Builder in case auto-config is not present
    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    // Provide a ready-to-use WebClient (built from the builder)
    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        return builder.build();
    }
}