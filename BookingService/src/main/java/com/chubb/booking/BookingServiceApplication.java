package com.chubb.booking;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.chubb.booking.client")
@Slf4j
public class BookingServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(BookingServiceApplication.class, args);
    }

    @Value("${flight.service.url:http://localhost:8081}")
    private String flightServiceUrl;

    @Bean
    public CommandLineRunner debugStartup() {
        return args -> {
            log.info("DEBUG: booking will call flight.service.url = {}", flightServiceUrl);
        };
    }
}