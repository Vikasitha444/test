package com.travel.bookingservice.service;

import com.travel.bookingservice.client.FlightServiceClient;
import com.travel.bookingservice.client.HotelServiceClient;
import com.travel.bookingservice.dto.*;
import com.travel.bookingservice.entity.Booking;
import com.travel.bookingservice.exception.BookingException;
import com.travel.bookingservice.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final FlightServiceClient flightServiceClient;
    private final HotelServiceClient hotelServiceClient;
    private final WebClient.Builder webClientBuilder;

    @Value("${user.service.url}")
    private String userServiceUrl;

    @Value("${notification.service.url}")
    private String notificationServiceUrl;

    public BookingDTO createBooking(BookingRequest request) {
        // Step 1: Validate user via WebClient
        ApiResponse<UserDTO> userResponse = webClientBuilder.build()
                .get()
                .uri(userServiceUrl + "/api/users/" + request.getUserId())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<UserDTO>>() {})
                .block();

        if (userResponse == null || !userResponse.isSuccess()) {
            throw new BookingException("User not found with id: " + request.getUserId());
        }

        // Step 2: Check flight availability via Feign Client
        ApiResponse<Boolean> flightAvailability = flightServiceClient.checkFlightAvailability(request.getFlightId());
        if (!flightAvailability.isSuccess() || !flightAvailability.getData()) {
            throw new BookingException("Flight is not available with id: " + request.getFlightId());
        }

        // Step 3: Check hotel availability via Feign Client
        ApiResponse<Boolean> hotelAvailability = hotelServiceClient.checkHotelAvailability(request.getHotelId());
        if (!hotelAvailability.isSuccess() || !hotelAvailability.getData()) {
            throw new BookingException("Hotel is not available with id: " + request.getHotelId());
        }

        // Step 4: Get flight and hotel details to calculate total cost
        ApiResponse<FlightDTO> flightResponse = flightServiceClient.getFlightById(request.getFlightId());
        ApiResponse<HotelDTO> hotelResponse = hotelServiceClient.getHotelById(request.getHotelId());

        Double flightPrice = flightResponse.getData().getPrice();
        Double hotelPrice = hotelResponse.getData().getPricePerNight();
        Double totalCost = flightPrice + hotelPrice;

        // Step 5: Create booking with PENDING status
        Booking booking = new Booking();
        booking.setUserId(request.getUserId());
        booking.setFlightId(request.getFlightId());
        booking.setHotelId(request.getHotelId());
        booking.setTravelDate(request.getTravelDate());
        booking.setTotalCost(totalCost);
        booking.setStatus("PENDING");
        booking.setCreatedAt(LocalDateTime.now());
        booking.setUpdatedAt(LocalDateTime.now());

        Booking savedBooking = bookingRepository.save(booking);

        // Step 6: Send notification via WebClient
        NotificationRequest notificationRequest = new NotificationRequest(
                request.getUserId(),
                savedBooking.getId(),
                "BOOKING_CREATED",
                "Your booking has been created with ID: " + savedBooking.getId() + ". Total cost: $" + totalCost
        );

        webClientBuilder.build()
                .post()
                .uri(notificationServiceUrl + "/api/notifications")
                .bodyValue(notificationRequest)
                .retrieve()
                .bodyToMono(Void.class)
                .block();

        return mapToDTO(savedBooking);
    }

    public BookingDTO getBookingById(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new BookingException("Booking not found with id: " + id));
        return mapToDTO(booking);
    }

    public List<BookingDTO> getBookingsByUserId(Long userId) {
        return bookingRepository.findByUserId(userId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<BookingDTO> getAllBookings() {
        return bookingRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public boolean bookingExists(Long id) {
        return bookingRepository.existsById(id);
    }

    public BookingDTO confirmBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new BookingException("Booking not found with id: " + id));

        booking.setStatus("CONFIRMED");
        booking.setUpdatedAt(LocalDateTime.now());
        Booking updatedBooking = bookingRepository.save(booking);

        // Send confirmation notification via WebClient
        NotificationRequest notificationRequest = new NotificationRequest(
                booking.getUserId(),
                booking.getId(),
                "BOOKING_CONFIRMED",
                "Your booking with ID: " + booking.getId() + " has been confirmed!"
        );

        webClientBuilder.build()
                .post()
                .uri(notificationServiceUrl + "/api/notifications")
                .bodyValue(notificationRequest)
                .retrieve()
                .bodyToMono(Void.class)
                .block();

        return mapToDTO(updatedBooking);
    }

    public BookingDTO cancelBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new BookingException("Booking not found with id: " + id));

        booking.setStatus("CANCELLED");
        booking.setUpdatedAt(LocalDateTime.now());
        Booking updatedBooking = bookingRepository.save(booking);

        return mapToDTO(updatedBooking);
    }

    private BookingDTO mapToDTO(Booking booking) {
        return new BookingDTO(
                booking.getId(),
                booking.getUserId(),
                booking.getFlightId(),
                booking.getHotelId(),
                booking.getTravelDate(),
                booking.getTotalCost(),
                booking.getStatus(),
                booking.getCreatedAt(),
                booking.getUpdatedAt()
        );
    }
}
