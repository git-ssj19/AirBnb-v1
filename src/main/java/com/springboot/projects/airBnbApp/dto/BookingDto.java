package com.springboot.projects.airBnbApp.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.springboot.projects.airBnbApp.entity.Guest;
import com.springboot.projects.airBnbApp.entity.Hotel;
import com.springboot.projects.airBnbApp.entity.Room;
import com.springboot.projects.airBnbApp.entity.User;
import com.springboot.projects.airBnbApp.entity.enums.BookingStatus;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Data
public class BookingDto {

//    private Hotel hotel;
//
//    private Room room;
    private Long id;

    @JsonIgnore
    private User user;

    private Integer roomsCount;
    private LocalDate checkInDate;

    private LocalDate checkOutDate;


    private BigDecimal amount;


    private BookingStatus bookingStatus;
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;



//    private Set<GuestDto> guests;
}
