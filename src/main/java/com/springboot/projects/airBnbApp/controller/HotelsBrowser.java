package com.springboot.projects.airBnbApp.controller;

import com.springboot.projects.airBnbApp.dto.HotelDto;
import com.springboot.projects.airBnbApp.dto.HotelPriceDto;
import com.springboot.projects.airBnbApp.dto.HotelSearchRequest;
import com.springboot.projects.airBnbApp.dto.RoomDto;
import com.springboot.projects.airBnbApp.service.HotelServiceImpl;
import com.springboot.projects.airBnbApp.service.InventoryService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Component
@RestController
@AllArgsConstructor
@RequestMapping("/hotels")
public class HotelsBrowser {

    private final InventoryService inventoryService;

    private final HotelServiceImpl hotelService;

    @GetMapping("/browse")
    public ResponseEntity<Page<HotelPriceDto>> browseHotels(@RequestBody HotelSearchRequest hotelSearchRequest){
        Page<HotelPriceDto> page= inventoryService.browseHotels(hotelSearchRequest);

        return ResponseEntity.ok(page);
    }

    @GetMapping("{hotelId}/info")
    public ResponseEntity<List<RoomDto>> getHotelInfoById(@PathVariable Long hotelId){
        List<RoomDto> room= hotelService.getHotelInfoById(hotelId);

        return ResponseEntity.ok(room);
    }


}
