package com.travel.bookingservice.client;

import com.travel.bookingservice.dto.ApiResponse;
import com.travel.bookingservice.dto.HotelDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "hotel-service", url = "${hotel.service.url}")
public interface HotelServiceClient {

    @GetMapping("/api/hotels/{id}")
    ApiResponse<HotelDTO> getHotelById(@PathVariable("id") Long id);

    @GetMapping("/api/hotels/{id}/availability")
    ApiResponse<Boolean> checkHotelAvailability(@PathVariable("id") Long id);
}
