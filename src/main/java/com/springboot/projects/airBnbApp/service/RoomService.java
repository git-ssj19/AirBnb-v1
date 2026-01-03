package com.springboot.projects.airBnbApp.service;

import com.springboot.projects.airBnbApp.dto.RoomDto;

import java.util.List;

public interface RoomService {
    RoomDto addRoomToHotel(Long hotelId, RoomDto roomDto);
    List<RoomDto> getAllRoomsByHotelId(Long hotelId);

    RoomDto getRoomById(Long roomId);

    void deleteRoomById(Long roomId);
}
