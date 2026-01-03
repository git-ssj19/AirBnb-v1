package com.springboot.projects.airBnbApp.Strategy;

import com.springboot.projects.airBnbApp.entity.Inventory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;


@RequiredArgsConstructor
public class HolidayPricingStrategy implements PricingStrategy{
    private final PricingStrategy wrapped;
    @Override
    public BigDecimal calculatePrice(Inventory inventory) {
        BigDecimal price = wrapped.calculatePrice(inventory);
        Boolean isHolidayToday = true;
        if(isHolidayToday){
            price = price.multiply(BigDecimal.valueOf(1.25));
        }
        return price;
    }
}
