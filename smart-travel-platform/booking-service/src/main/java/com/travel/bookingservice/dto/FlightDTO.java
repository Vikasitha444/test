package com.travel.bookingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlightDTO {
    private Long id;
    private String flightNumber;
    private String origin;
    private String destination;
    private Double price;
    private Integer availableSeats;
    private Boolean available;
}
