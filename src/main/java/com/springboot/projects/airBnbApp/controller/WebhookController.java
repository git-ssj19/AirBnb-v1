package com.springboot.projects.airBnbApp.controller;

import com.springboot.projects.airBnbApp.service.BookingService;
import com.springboot.projects.airBnbApp.service.BookingServiceImpl;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

@Component
@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
public class WebhookController {

    private final BookingService bookingService;
    @Value("${stripe.webhook.secret}")
    private String endpointSecret;
    @PostMapping("/payment")
    public ResponseEntity<Void> capturePayments(@RequestBody String payload, @RequestHeader("Stripe-Signature") String sigHeader){
        try{
            Event event = Webhook.constructEvent(payload,sigHeader,endpointSecret);
            bookingService.capturePayment(event);
            return ResponseEntity.noContent().build();

        }
        catch(SignatureVerificationException e){
            throw new RuntimeException(e);
        }
    }
}
