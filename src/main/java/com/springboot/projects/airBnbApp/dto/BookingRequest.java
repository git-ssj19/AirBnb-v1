package com.springboot.projects.airBnbApp.dto;

import com.springboot.projects.airBnbApp.entity.Hotel;
import com.springboot.projects.airBnbApp.entity.Room;
import lombok.Data;

import java.time.LocalDate;

@Data
public class BookingRequest {

    private Long hotelId;
    private Long roomId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer roomsCount;
}
