package com.springboot.projects.airBnbApp.Strategy;

import com.springboot.projects.airBnbApp.entity.Inventory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;


@RequiredArgsConstructor
public class UrgencyPricingStrategy implements PricingStrategy {

    private final PricingStrategy wrapped;
    @Override
    public BigDecimal calculatePrice(Inventory inventory) {
        LocalDate today = LocalDate.now();
        BigDecimal price = wrapped.calculatePrice(inventory);
        if(!inventory.getDate().isBefore(today) && inventory.getDate().isBefore(today.plusDays(7))){
            price =  price.multiply(BigDecimal.valueOf(1.5));
        }
        return price;
    }
}
