package com.springboot.projects.airBnbApp.repository;

import com.springboot.projects.airBnbApp.dto.HotelPriceDto;
import com.springboot.projects.airBnbApp.entity.Hotel;
import com.springboot.projects.airBnbApp.entity.HotelMinPrice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;


public interface HotelMinPriceRepository extends JpaRepository<HotelMinPrice,Long> {

    Optional<HotelMinPrice> findByHotelAndDate(Hotel hotel, LocalDate date);

    @Query("""
            SELECT new com.springboot.projects.airBnbApp.dto.HotelPriceDto(i.hotel,CAST(AVG(i.price) AS big_decimal))
            FROM HotelMinPrice i
            where i.hotel.city = :city
            AND i.date BETWEEN :startDate AND :endDate
            AND i.hotel.active = true
            GROUP BY i.hotel
            """)
    Page<HotelPriceDto> browseHotels(
            @Param("city") String city,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );
}
