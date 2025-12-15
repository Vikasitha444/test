package com.travel.bookingservice.client;

import com.travel.bookingservice.dto.ApiResponse;
import com.travel.bookingservice.dto.FlightDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "flight-service", url = "${flight.service.url}")
public interface FlightServiceClient {

    @GetMapping("/api/flights/{id}")
    ApiResponse<FlightDTO> getFlightById(@PathVariable("id") Long id);

    @GetMapping("/api/flights/{id}/availability")
    ApiResponse<Boolean> checkFlightAvailability(@PathVariable("id") Long id);
}
