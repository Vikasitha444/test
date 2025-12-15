package com.travel.bookingservice.controller;

import com.travel.bookingservice.dto.ApiResponse;
import com.travel.bookingservice.dto.BookingDTO;
import com.travel.bookingservice.dto.BookingRequest;
import com.travel.bookingservice.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<ApiResponse<BookingDTO>> createBooking(@RequestBody BookingRequest request) {
        BookingDTO booking = bookingService.createBooking(request);
        return ResponseEntity.ok(ApiResponse.success(booking, "Booking created successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookingDTO>> getBookingById(@PathVariable Long id) {
        BookingDTO booking = bookingService.getBookingById(id);
        return ResponseEntity.ok(ApiResponse.success(booking, "Booking retrieved successfully"));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<BookingDTO>>> getBookingsByUserId(@PathVariable Long userId) {
        List<BookingDTO> bookings = bookingService.getBookingsByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(bookings, "Bookings retrieved successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<BookingDTO>>> getAllBookings() {
        List<BookingDTO> bookings = bookingService.getAllBookings();
        return ResponseEntity.ok(ApiResponse.success(bookings, "Bookings retrieved successfully"));
    }

    @GetMapping("/{id}/exists")
    public ResponseEntity<Boolean> bookingExists(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.bookingExists(id));
    }

    @PutMapping("/{id}/confirm")
    public ResponseEntity<ApiResponse<BookingDTO>> confirmBooking(@PathVariable Long id) {
        BookingDTO booking = bookingService.confirmBooking(id);
        return ResponseEntity.ok(ApiResponse.success(booking, "Booking confirmed successfully"));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<BookingDTO>> cancelBooking(@PathVariable Long id) {
        BookingDTO booking = bookingService.cancelBooking(id);
        return ResponseEntity.ok(ApiResponse.success(booking, "Booking cancelled successfully"));
    }
}
