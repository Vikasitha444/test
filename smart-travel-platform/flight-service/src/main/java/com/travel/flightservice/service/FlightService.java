package com.travel.flightservice.service;

import com.travel.flightservice.dto.FlightDTO;
import com.travel.flightservice.entity.Flight;
import com.travel.flightservice.exception.FlightNotFoundException;
import com.travel.flightservice.repository.FlightRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FlightService {

    private final FlightRepository flightRepository;

    public FlightDTO getFlightById(Long id) {
        Flight flight = flightRepository.findById(id)
                .orElseThrow(() -> new FlightNotFoundException("Flight not found with id: " + id));
        return mapToDTO(flight);
    }

    public List<FlightDTO> getAllFlights() {
        return flightRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public FlightDTO createFlight(FlightDTO flightDTO) {
        Flight flight = new Flight();
        flight.setFlightNumber(flightDTO.getFlightNumber());
        flight.setOrigin(flightDTO.getOrigin());
        flight.setDestination(flightDTO.getDestination());
        flight.setPrice(flightDTO.getPrice());
        flight.setAvailableSeats(flightDTO.getAvailableSeats());
        flight.setAvailable(flightDTO.getAvailable());
        Flight savedFlight = flightRepository.save(flight);
        return mapToDTO(savedFlight);
    }

    public boolean checkAvailability(Long flightId) {
        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(() -> new FlightNotFoundException("Flight not found with id: " + flightId));
        return flight.getAvailable() && flight.getAvailableSeats() > 0;
    }

    private FlightDTO mapToDTO(Flight flight) {
        return new FlightDTO(
                flight.getId(),
                flight.getFlightNumber(),
                flight.getOrigin(),
                flight.getDestination(),
                flight.getPrice(),
                flight.getAvailableSeats(),
                flight.getAvailable()
        );
    }
}
