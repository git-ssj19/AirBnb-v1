package com.springboot.projects.airBnbApp.service;

import com.springboot.projects.airBnbApp.entity.Booking;

public interface CheckoutService {
    String getCheckoutSession(Booking booking,String successUrl,String failureUrl);
}
