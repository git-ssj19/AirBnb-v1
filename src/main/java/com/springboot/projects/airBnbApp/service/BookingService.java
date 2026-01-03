package com.springboot.projects.airBnbApp.service;

import com.springboot.projects.airBnbApp.dto.BookingDto;
import com.springboot.projects.airBnbApp.dto.BookingRequest;
import com.springboot.projects.airBnbApp.dto.GuestDto;
import com.stripe.model.Event;

import java.util.List;
import java.util.Map;

public interface BookingService {
    BookingDto initialiseBooking(BookingRequest bookingRequest);

    BookingDto addGuests(Long bookingId, List<GuestDto> guestDtoList);

    String initiatePayments(Long bookingId);
    void capturePayment(Event event);
    void cancelPayments(Long bookingId);

    String getBookingStatus(Long bookingId);


}
