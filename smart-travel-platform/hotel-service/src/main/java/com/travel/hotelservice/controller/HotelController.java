package com.travel.hotelservice.controller;

import com.travel.hotelservice.dto.ApiResponse;
import com.travel.hotelservice.dto.HotelDTO;
import com.travel.hotelservice.service.HotelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hotels")
@RequiredArgsConstructor
public class HotelController {

    private final HotelService hotelService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<HotelDTO>> getHotelById(@PathVariable Long id) {
        HotelDTO hotel = hotelService.getHotelById(id);
        return ResponseEntity.ok(ApiResponse.success(hotel, "Hotel retrieved successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<HotelDTO>>> getAllHotels() {
        List<HotelDTO> hotels = hotelService.getAllHotels();
        return ResponseEntity.ok(ApiResponse.success(hotels, "Hotels retrieved successfully"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<HotelDTO>> createHotel(@RequestBody HotelDTO hotelDTO) {
        HotelDTO createdHotel = hotelService.createHotel(hotelDTO);
        return ResponseEntity.ok(ApiResponse.success(createdHotel, "Hotel created successfully"));
    }

    @GetMapping("/{id}/availability")
    public ResponseEntity<ApiResponse<Boolean>> checkAvailability(@PathVariable Long id) {
        boolean isAvailable = hotelService.checkAvailability(id);
        return ResponseEntity.ok(ApiResponse.success(isAvailable, isAvailable ? "Hotel is available" : "Hotel not available"));
    }
}
