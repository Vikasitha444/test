package com.travel.flightservice.controller;

import com.travel.flightservice.dto.ApiResponse;
import com.travel.flightservice.dto.FlightDTO;
import com.travel.flightservice.service.FlightService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/flights")
@RequiredArgsConstructor
public class FlightController {

    private final FlightService flightService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FlightDTO>> getFlightById(@PathVariable Long id) {
        FlightDTO flight = flightService.getFlightById(id);
        return ResponseEntity.ok(ApiResponse.success(flight, "Flight retrieved successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<FlightDTO>>> getAllFlights() {
        List<FlightDTO> flights = flightService.getAllFlights();
        return ResponseEntity.ok(ApiResponse.success(flights, "Flights retrieved successfully"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<FlightDTO>> createFlight(@RequestBody FlightDTO flightDTO) {
        FlightDTO createdFlight = flightService.createFlight(flightDTO);
        return ResponseEntity.ok(ApiResponse.success(createdFlight, "Flight created successfully"));
    }

    @GetMapping("/{id}/availability")
    public ResponseEntity<ApiResponse<Boolean>> checkAvailability(@PathVariable Long id) {
        boolean isAvailable = flightService.checkAvailability(id);
        return ResponseEntity.ok(ApiResponse.success(isAvailable, isAvailable ? "Flight is available" : "Flight not available"));
    }
}
