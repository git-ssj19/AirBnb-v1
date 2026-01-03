package com.springboot.projects.airBnbApp.controller;

import com.springboot.projects.airBnbApp.dto.HotelDto;
import com.springboot.projects.airBnbApp.service.HotelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

@Component
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/hotels")
public class HotelController {

    private final HotelService hotelService;
    @PostMapping("/create")
    public ResponseEntity<HotelDto> postHotel(@RequestBody HotelDto hotelDto){
        HotelDto hotelDto1 = hotelService.createNewHotel(hotelDto);
        return new ResponseEntity<>(hotelDto1, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<HotelDto> getHotelById(@PathVariable Long id){
        HotelDto hotelDto =  hotelService.getHotelById(id);
        return ResponseEntity.ok(hotelDto);
    }


}
