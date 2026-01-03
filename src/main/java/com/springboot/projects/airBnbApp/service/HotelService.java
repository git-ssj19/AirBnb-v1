package com.springboot.projects.airBnbApp.service;

import com.springboot.projects.airBnbApp.dto.HotelDto;
import com.springboot.projects.airBnbApp.dto.RoomDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface HotelService {
    HotelDto createNewHotel(HotelDto hotelDto);
    HotelDto getHotelById(Long id);

    List<RoomDto> getHotelInfoById(Long hotelId);
}
