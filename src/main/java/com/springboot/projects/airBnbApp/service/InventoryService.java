package com.springboot.projects.airBnbApp.service;

import com.springboot.projects.airBnbApp.dto.HotelDto;
import com.springboot.projects.airBnbApp.dto.HotelPriceDto;
import com.springboot.projects.airBnbApp.dto.HotelSearchRequest;
import com.springboot.projects.airBnbApp.dto.RoomDto;
import com.springboot.projects.airBnbApp.entity.Room;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface InventoryService {

    void addInventory(Room room);
    void deleteRoomsByRoomId(Long roomId);

    Page<HotelPriceDto> browseHotels(HotelSearchRequest hotelSearchRequest);


}
