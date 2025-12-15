package com.travel.paymentservice.service;

import com.travel.paymentservice.dto.PaymentDTO;
import com.travel.paymentservice.dto.PaymentRequest;
import com.travel.paymentservice.entity.Payment;
import com.travel.paymentservice.exception.PaymentException;
import com.travel.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final WebClient.Builder webClientBuilder;

    @Value("${booking.service.url}")
    private String bookingServiceUrl;

    public PaymentDTO processPayment(PaymentRequest request) {
        // Validate booking exists via WebClient call to Booking Service
        Boolean bookingExists = webClientBuilder.build()
                .get()
                .uri(bookingServiceUrl + "/api/bookings/" + request.getBookingId() + "/exists")
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();

        if (bookingExists == null || !bookingExists) {
            throw new PaymentException("Booking not found with id: " + request.getBookingId());
        }

        // Process payment
        Payment payment = new Payment();
        payment.setBookingId(request.getBookingId());
        payment.setAmount(request.getAmount());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setStatus("COMPLETED");
        payment.setPaymentDate(LocalDateTime.now());

        Payment savedPayment = paymentRepository.save(payment);

        // Notify Booking Service about payment completion via WebClient
        webClientBuilder.build()
                .put()
                .uri(bookingServiceUrl + "/api/bookings/" + request.getBookingId() + "/confirm")
                .retrieve()
                .bodyToMono(Void.class)
                .block();

        return mapToDTO(savedPayment);
    }

    public PaymentDTO getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentException("Payment not found with id: " + id));
        return mapToDTO(payment);
    }

    public PaymentDTO getPaymentByBookingId(Long bookingId) {
        Payment payment = paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new PaymentException("Payment not found for booking id: " + bookingId));
        return mapToDTO(payment);
    }

    public List<PaymentDTO> getAllPayments() {
        return paymentRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private PaymentDTO mapToDTO(Payment payment) {
        return new PaymentDTO(
                payment.getId(),
                payment.getBookingId(),
                payment.getAmount(),
                payment.getStatus(),
                payment.getPaymentMethod(),
                payment.getPaymentDate()
        );
    }
}
