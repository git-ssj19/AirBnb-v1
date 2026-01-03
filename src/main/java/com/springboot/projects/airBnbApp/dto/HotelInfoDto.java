package com.springboot.projects.airBnbApp.dto;

import lombok.Data;

import java.util.List;

@Data
public class HotelInfoDto {
    private HotelDto hotel;
    private List<RoomDto> room;
}
