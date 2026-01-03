package com.springboot.projects.airBnbApp.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RoomDto {
    private Long id;

//    private Hotel hotel;
//    When we have Hotel then Room info also will be present - **to be clarified week3

    private String type;

    private BigDecimal basePrice;

    private String[] photos;

    private String[] amenities;

    private Integer totalCount;

    private Integer capacity;

}
