package com.springboot.projects.airBnbApp.Strategy;

import com.springboot.projects.airBnbApp.entity.Booking;
import com.springboot.projects.airBnbApp.entity.Inventory;
import org.springframework.stereotype.Service;

import javax.swing.plaf.basic.BasicIconFactory;
import java.math.BigDecimal;
import java.util.List;

@Service
public class PricingService {

    public BigDecimal calculateDynamicPricing(Inventory inventory){
        PricingStrategy pricingStrategy = new BasePricingStrategy();
        pricingStrategy = new SurgePricingStrategy(pricingStrategy);
        pricingStrategy = new OccupancyPricingStrategy(pricingStrategy);
        pricingStrategy = new UrgencyPricingStrategy(pricingStrategy);
        pricingStrategy = new HolidayPricingStrategy(pricingStrategy);

        return pricingStrategy.calculatePrice(inventory);


    }
    public BigDecimal calculatePricing(List<Inventory> inventories){
        BigDecimal total = BigDecimal.ZERO;
        for (Inventory i : inventories){
            total = total.add(calculateDynamicPricing(i));
        }
        return total;
    }
}
