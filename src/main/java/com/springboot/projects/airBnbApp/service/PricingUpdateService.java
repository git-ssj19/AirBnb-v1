package com.springboot.projects.airBnbApp.service;

import com.springboot.projects.airBnbApp.Strategy.PricingService;
import com.springboot.projects.airBnbApp.entity.Hotel;
import com.springboot.projects.airBnbApp.entity.HotelMinPrice;
import com.springboot.projects.airBnbApp.entity.Inventory;
import com.springboot.projects.airBnbApp.repository.HotelMinPriceRepository;
import com.springboot.projects.airBnbApp.repository.HotelRepository;
import com.springboot.projects.airBnbApp.repository.InventoryRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.Schedules;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PricingUpdateService {

    private final HotelRepository  hotelRepository;
    private final PricingService pricingService;
    private final InventoryRepository inventoryRepository;
    private final HotelMinPriceRepository hotelMinPriceRepository;

    @Scheduled(cron = "0 0 * * * *")
//    @Scheduled(cron="*/5 * * * * *")
    public void updatePrices(){

        int page =0 ;
        int batchSize = 100;
        while(true){
            Page<Hotel> hotelPage = hotelRepository.findAll(PageRequest.of(page,batchSize));
            if(hotelPage.isEmpty()){
                break;
            }
            hotelPage.getContent().forEach(hotel -> updateHotelPrices(hotel));
            page++;
        }

    }

    private void updateHotelPrices(Hotel hotel) {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusYears(1);
        List<Inventory> inventoryList = inventoryRepository.findAllByHotelAndDateBetween(hotel,startDate,endDate);

        for (Inventory inventory:inventoryList) {
            log.info("Original price of Hotel {} is {}",inventory.getHotel(),inventory.getPrice());
            BigDecimal price =  pricingService.calculateDynamicPricing(inventory);
            log.info("Price after surge for Hotel {} is {}",inventory.getHotel(),inventory.getPrice());

            inventory.setPrice(price);
        }
        inventoryRepository.saveAll(inventoryList);
        updateHotelMinPrices(hotel,inventoryList,startDate,endDate);
    }

    private void updateHotelMinPrices(Hotel hotel, List<Inventory> inventoryList, LocalDate startDate, LocalDate endDate) {
        Map<LocalDate,BigDecimal> dailyMinPrices = new HashMap<>();

        while(true){
            BigDecimal minPrice = null;
            for(Inventory inventory : inventoryList){
                if(inventory.getDate().isEqual(startDate) && (minPrice == null || inventory.getPrice().compareTo(minPrice) < 0)){
                    minPrice = inventory.getPrice();
                }
            }
            if(minPrice!=null)
            dailyMinPrices.put(startDate,minPrice);
            startDate = startDate.plusDays(1);
            if(startDate.isAfter(endDate))
                break;
        }

        List<HotelMinPrice> hotelMinPrices = new ArrayList<>();
//        System.out.println(dailyMinPrices);
        dailyMinPrices.forEach((date,price) -> {
            HotelMinPrice hotelMinPrice = hotelMinPriceRepository.findByHotelAndDate(hotel,date)
                    .orElse(new HotelMinPrice(hotel,date));
            price = price.setScale(2,RoundingMode.HALF_UP);
            BigDecimal max = new BigDecimal("99999999.99");
            if (price.abs().compareTo(max) > 0) {
                throw new IllegalArgumentException("Price too large for NUMERIC(10,2): " + price);
            }
            log.info("Price of hotel with id {} after surge calculation is {}",hotel.getId(),price);
            hotelMinPrice.setPrice(price);
            hotelMinPrices.add(hotelMinPrice);
        });
        hotelMinPriceRepository.saveAll(hotelMinPrices);

    }
}
