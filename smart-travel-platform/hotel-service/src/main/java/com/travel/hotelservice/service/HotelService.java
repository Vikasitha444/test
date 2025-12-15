package com.travel.hotelservice.service;

import com.travel.hotelservice.dto.HotelDTO;
import com.travel.hotelservice.entity.Hotel;
import com.travel.hotelservice.exception.HotelNotFoundException;
import com.travel.hotelservice.repository.HotelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HotelService {

    private final HotelRepository hotelRepository;

    public HotelDTO getHotelById(Long id) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new HotelNotFoundException("Hotel not found with id: " + id));
        return mapToDTO(hotel);
    }

    public List<HotelDTO> getAllHotels() {
        return hotelRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public HotelDTO createHotel(HotelDTO hotelDTO) {
        Hotel hotel = new Hotel();
        hotel.setName(hotelDTO.getName());
        hotel.setLocation(hotelDTO.getLocation());
        hotel.setPricePerNight(hotelDTO.getPricePerNight());
        hotel.setAvailableRooms(hotelDTO.getAvailableRooms());
        hotel.setAvailable(hotelDTO.getAvailable());
        Hotel savedHotel = hotelRepository.save(hotel);
        return mapToDTO(savedHotel);
    }

    public boolean checkAvailability(Long hotelId) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new HotelNotFoundException("Hotel not found with id: " + hotelId));
        return hotel.getAvailable() && hotel.getAvailableRooms() > 0;
    }

    private HotelDTO mapToDTO(Hotel hotel) {
        return new HotelDTO(
                hotel.getId(),
                hotel.getName(),
                hotel.getLocation(),
                hotel.getPricePerNight(),
                hotel.getAvailableRooms(),
                hotel.getAvailable()
        );
    }
}
