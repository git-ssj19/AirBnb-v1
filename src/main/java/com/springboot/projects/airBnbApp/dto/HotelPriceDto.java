package com.springboot.projects.airBnbApp.dto;

import com.springboot.projects.airBnbApp.entity.Hotel;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class HotelPriceDto {
  private Hotel hotel;
  private BigDecimal price;
  public HotelPriceDto(Hotel h,BigDecimal price){
      this.hotel = h;
      this.price = price;
  }
}
