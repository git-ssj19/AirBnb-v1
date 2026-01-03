package com.springboot.projects.airBnbApp.controller;

import com.springboot.projects.airBnbApp.dto.BookingDto;
import com.springboot.projects.airBnbApp.dto.BookingRequest;
import com.springboot.projects.airBnbApp.dto.GuestDto;
import com.springboot.projects.airBnbApp.entity.Hotel;
import com.springboot.projects.airBnbApp.service.BookingServiceImpl;
import com.springboot.projects.airBnbApp.service.HotelServiceImpl;
import com.springboot.projects.airBnbApp.service.RoomServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/bookings")
public class HotelBookingController {

    private final BookingServiceImpl bookingService;

    @PostMapping("/init")
    public ResponseEntity<BookingDto> initialiseBooking(@RequestBody BookingRequest bookingRequest){
         BookingDto booking = bookingService.initialiseBooking(bookingRequest);
         return ResponseEntity.ok(booking);
    }

    @PostMapping("/{bookingId}/addGuests")
    public ResponseEntity<BookingDto> addGuests(@PathVariable Long bookingId, @RequestBody List<GuestDto> guestDtoList){
        return ResponseEntity.ok(bookingService.addGuests(bookingId,guestDtoList));
    }

    @PostMapping("/{bookingId}/payments")
    public ResponseEntity<Map<String,String>> initiatePayments(@PathVariable Long bookingId){
        String sessionUrl = bookingService.initiatePayments(bookingId);

        return ResponseEntity.ok(Map.of("SessionUrl",sessionUrl));
    }
    @PostMapping("/{bookingId}/cancel")
    public ResponseEntity<Void> cancelPayments(@PathVariable Long bookingId){
        bookingService.cancelPayments(bookingId);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{bookingId}/status")
    public ResponseEntity<Map<String,String>> getBookingStatus(@PathVariable Long bookingId){
        return ResponseEntity.ok(Map.of("Status", bookingService.getBookingStatus(bookingId)));
    }
}
