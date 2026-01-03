package com.springboot.projects.airBnbApp.repository;

import com.springboot.projects.airBnbApp.dto.HotelDto;
import com.springboot.projects.airBnbApp.dto.HotelSearchRequest;
import com.springboot.projects.airBnbApp.entity.Hotel;
import com.springboot.projects.airBnbApp.entity.Inventory;
import jakarta.persistence.LockModeType;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface InventoryRepository extends JpaRepository<Inventory,Long> {
    boolean existsByRoomId(Long roomId);

    void deleteByRoomId(Long roomId);


    @Query("""
            SELECT DISTINCT(i.hotel) FROM Inventory i
            WHERE i.city = :city
            AND i.date BETWEEN :startDate AND :endDate
            AND (i.totalCount - i.bookedCount - i.reservedCount) >= :roomsCount
            AND i.closed = false
            GROUP BY i.hotel,i.room
            HAVING COUNT(i.date) = :dateCount
            """)
    Page<Hotel> browseHotels(
            @Param("city") String city,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("roomsCount") Integer roomsCount,
            @Param("dateCount") Long dateCount,
            Pageable pageable
           );

    @Modifying
    @Query("""
            UPDATE Inventory i
            SET i.reservedCount = i.reservedCount + :numberOfRooms
            WHERE i.room.id = :roomId
             AND i.date BETWEEN :startDate AND :endDate
             AND (i.totalCount - i.bookedCount - i.reservedCount) >= :numberOfRooms
             AND i.closed = false
            """)
    void initBooking(@Param("roomId") Long roomId,
                     @Param("startDate") LocalDate startDate,
                     @Param("endDate") LocalDate endDate,
                     @Param("numberOfRooms") int numberOfRooms);

    @Query("""
            SELECT i FROM Inventory i
            where i.hotel.id = :hotelId
            AND i.room.id = :roomId
            AND i.date BETWEEN :startDate AND :endDate
            AND (i.totalCount - i.bookedCount - i.reservedCount) >= :roomsCount
            """)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Inventory> getAllInventoriesAndLock(
            @Param("roomId") Long roomId,
            @Param("hotelId") Long hotelId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("roomsCount") Integer roomsCount
    );
    @Query("""
            SELECT i FROM Inventory i
            WHERE i.room.id = :roomId
            AND i.date BETWEEN :startDate AND :endDate
            AND (i.totalCount - i.bookedCount) >= :numberOfRooms
            """)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Inventory> getAllReservedInventoriesAndLock(
            @Param("roomId") Long roomId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("numberOfRooms") int numberOfRooms);

    @Modifying
    @Query("""
           UPDATE Inventory i
           SET i.reservedCount = i.reservedCount - :numberOfRooms,
               i.bookedCount = i.bookedCount + :numberOfRooms
           WHERE i.room.id = :roomId
            AND i.date BETWEEN :startDate AND :endDate
            AND (i.totalCount - i.bookedCount) >= :numberOfRooms
            AND i.reservedCount >= :numberOfRooms
            AND i.closed = false
           """)
    void confirmBooking(@Param("roomId") Long roomId,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate,
                        @Param("numberOfRooms") int numberOfRooms );

    @Modifying
    @Query("""
           UPDATE Inventory i
           SET i.bookedCount = i.bookedCount - :numberOfRooms
           WHERE i.room.id = :roomId
            AND i.date BETWEEN :startDate AND :endDate
            AND (i.totalCount - i.bookedCount) >= :numberOfRooms
            AND i.closed = false
           """)
    void cancelBooking(@Param("roomId") Long roomId,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate,
                        @Param("numberOfRooms") int numberOfRooms );

    List<Inventory> findAllByHotelAndDateBetween(Hotel hotel, LocalDate startDate, LocalDate endDate);
}
