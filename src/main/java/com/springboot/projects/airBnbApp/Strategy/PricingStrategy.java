package com.springboot.projects.airBnbApp.Strategy;

import com.springboot.projects.airBnbApp.entity.Inventory;

import java.math.BigDecimal;

public interface PricingStrategy {
    BigDecimal calculatePrice(Inventory inventory);
}
