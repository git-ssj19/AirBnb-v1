package com.springboot.projects.airBnbApp.service;

import com.springboot.projects.airBnbApp.dto.HotelDto;
import com.springboot.projects.airBnbApp.dto.HotelPriceDto;
import com.springboot.projects.airBnbApp.dto.HotelSearchRequest;
import com.springboot.projects.airBnbApp.dto.RoomDto;
import com.springboot.projects.airBnbApp.entity.Hotel;
import com.springboot.projects.airBnbApp.entity.Inventory;
import com.springboot.projects.airBnbApp.entity.Room;
import com.springboot.projects.airBnbApp.exception.ResourceNotFoundException;
import com.springboot.projects.airBnbApp.repository.HotelMinPriceRepository;
import com.springboot.projects.airBnbApp.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.chrono.ChronoLocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService{

    private final InventoryRepository inventoryRepository;
    private final HotelMinPriceRepository hotelMinPriceRepository;
    @Override
    public void addInventory(Room room) {
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusYears(1);
        for(;!today.equals(endDate);today=today.plusDays(1)){
            Inventory inventory = Inventory.builder()
                    .hotel(room.getHotel())
                    .room(room)
                    .date(today)
                    .price(room.getBasePrice())
                    .totalCount(room.getTotalCount())
                    .bookedCount(0)
                    .reservedCount(0)
                    .closed(false)
                    .surgeFactor(BigDecimal.ONE)
                    .city(room.getHotel().getCity())
                    .build();
            inventoryRepository.save(inventory);
        }
    }

    @Override
    public void deleteRoomsByRoomId(Long roomId) {
        if(inventoryRepository.existsByRoomId(roomId)){
            inventoryRepository.deleteByRoomId(roomId);
        }
        else{
            throw new ResourceNotFoundException("Room not found in Inventory with Id "+roomId);
        }

    }

    @Override
    public Page<HotelPriceDto> browseHotels(HotelSearchRequest hotelSearchRequest) {
        Pageable pageable = PageRequest.of(hotelSearchRequest.getPage(),hotelSearchRequest.getPageSize());
        log.info("Page : {} , Size : {}",pageable.getPageNumber(),pageable.getPageSize());
        Long dateCount = ChronoUnit.DAYS.between(hotelSearchRequest.getEndDate(),hotelSearchRequest.getStartDate())+1;
        Page<HotelPriceDto> page = hotelMinPriceRepository.browseHotels(hotelSearchRequest.getCity(),hotelSearchRequest.getStartDate(),hotelSearchRequest.getEndDate(),pageable);

        return page;
    }



}
