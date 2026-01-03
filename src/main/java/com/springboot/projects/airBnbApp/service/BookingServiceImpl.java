package com.springboot.projects.airBnbApp.service;

import com.springboot.projects.airBnbApp.Strategy.PricingService;
import com.springboot.projects.airBnbApp.dto.BookingDto;
import com.springboot.projects.airBnbApp.dto.BookingRequest;
import com.springboot.projects.airBnbApp.dto.GuestDto;
import com.springboot.projects.airBnbApp.entity.*;
import com.springboot.projects.airBnbApp.entity.enums.BookingStatus;
import com.springboot.projects.airBnbApp.exception.ResourceNotFoundException;
import com.springboot.projects.airBnbApp.exception.UnauthorizedException;
import com.springboot.projects.airBnbApp.repository.*;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.Refund;
import com.stripe.model.checkout.Session;
import com.stripe.param.RefundCreateParams;
import jakarta.transaction.Transactional;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.awt.print.Book;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService{

  private final HotelRepository hotelRepository;
  private final RoomRepository roomRepository;
  private final InventoryRepository inventoryRepository;
  private final BookingRepository bookingRepository;
  private  final ModelMapper modelMapper;
  private final GuestRepository guestRepository;
  private final HotelMinPriceRepository hotelMinPriceRepository;
  private final CheckoutService checkoutService;
  private final PricingService pricingService;

  @Value("${frontend.url}")
  private String frontEndUrl;

    @Override
    @Transactional
    public BookingDto initialiseBooking(BookingRequest bookingRequest) {
        log.info("Initialising booking for HotelID {} , RoomId {}",bookingRequest.getHotelId(),bookingRequest.getRoomId());
        Hotel hotel = hotelRepository.findById(bookingRequest.getHotelId()).orElseThrow(()-> new ResourceNotFoundException("Hotel not fund with Id :"+bookingRequest.getHotelId()));

        Room room = roomRepository.findById(bookingRequest.getRoomId()).orElseThrow(()-> new ResourceNotFoundException("Room not found with Id "+bookingRequest.getRoomId()));

        List<Inventory> inventories = inventoryRepository.getAllInventoriesAndLock(bookingRequest.getRoomId(), bookingRequest.getHotelId(), bookingRequest.getStartDate(),bookingRequest.getEndDate(),bookingRequest.getRoomsCount());

        long daysCount = ChronoUnit.DAYS.between(bookingRequest.getStartDate(),bookingRequest.getEndDate())+1;

        if(daysCount != inventories.size())
        {
            throw new IllegalStateException("Rooms not available anymore");
        }

        //Reserve the room / update the booked count of inventories
        inventoryRepository.initBooking(room.getId(),bookingRequest.getStartDate(),bookingRequest.getEndDate(),bookingRequest.getRoomsCount());



        //TO DO - To add dynamic pricing
        BigDecimal priceForOneRoom = pricingService.calculatePricing(inventories);
        BigDecimal totalPrice = priceForOneRoom.multiply(BigDecimal.valueOf(bookingRequest.getRoomsCount()));


        Booking booking = Booking.builder().bookingStatus(BookingStatus.RESERVED)
                .hotel(hotel)
                .roomsCount(bookingRequest.getRoomsCount())
                .checkOutDate(bookingRequest.getEndDate())
                .checkInDate(bookingRequest.getStartDate())
                .room(room)
                .user(getCurrentUser())
                .amount(totalPrice)
                .build();


        bookingRepository.save(booking);



        return modelMapper.map(booking,BookingDto.class);
    }

    @Override
    @Transactional
    public BookingDto addGuests(Long bookingId, List<GuestDto> guestDtoList) {
        log.info("Adding guests to booking with id : {} ",bookingId);
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(()->new ResourceNotFoundException("Booking not found with id : "+ bookingId));

        User user = getCurrentUser();

        if(!user.equals(booking.getUser())){
            throw new UnauthorizedException("Booking does not belong to this user with id: " + user.getId());
        }

        if(!isBookingActive(booking)){
            throw new IllegalStateException("Booking is not active or is expired");
        }
        if(booking.getBookingStatus() != BookingStatus.RESERVED){
            throw  new IllegalStateException("Booking is not under reserved state");
        }
        for(GuestDto guestDto:guestDtoList){
            Guest guest = modelMapper.map(guestDto, Guest.class);
            guest.setUser(user);
            guest = guestRepository.save(guest);
            booking.getGuests().add(guest);
        }
        booking.setBookingStatus(BookingStatus.GUESTS_ADDED);
        bookingRepository.save(booking);


        return modelMapper.map(booking,BookingDto.class);
    }

    @Override
    @Transactional
    public String initiatePayments(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(()-> new ResourceNotFoundException("Booking not found with id "+bookingId));

        User user = getCurrentUser();
        if(!user.equals(booking.getUser())){
            throw new UnauthorizedException("Booking does not belong to this user with id: " + user.getId());
        }
        if(!isBookingActive(booking)){
            throw new IllegalStateException("Booking is not active or is expired");
        }

        String sessionUrl = checkoutService.getCheckoutSession(booking,frontEndUrl+"/payments/success",frontEndUrl+"/payments/failure");
        booking.setBookingStatus(BookingStatus.PENDING_STATUS);
        bookingRepository.save(booking);
        return sessionUrl;
    }

    @Override
    @Transactional
    public void capturePayment(Event event) {
     if("checkout.session.completed".equals(event.getType())){
         Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
         if(session == null){
             return;
         }
         String sessionId = session.getId();
         Booking booking = bookingRepository.findByPaymentSessionId(sessionId).orElseThrow(()-> new ResourceNotFoundException("Booking not found for payment session id "+sessionId));
         booking.setBookingStatus(BookingStatus.CONFIRMED);
         bookingRepository.save(booking);

        inventoryRepository.getAllReservedInventoriesAndLock(booking.getRoom().getId(),booking.getCheckInDate(),booking.getCheckOutDate(),booking.getRoomsCount());
        inventoryRepository.confirmBooking(booking.getRoom().getId(),booking.getCheckInDate(),booking.getCheckOutDate(),booking.getRoomsCount());

        log.info("Successfully confirmed the booking {}",booking.getId());

     }
     else{
         log.warn("Unhandled event type: {}",event.getType());
     }

    }

    @Override
    @Transactional
    public void cancelPayments(Long bookingId){
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(()-> new ResourceNotFoundException("Booking not found with id "+bookingId));

        log.info("Cancelling booking for booking id {} ",bookingId);
        User user = getCurrentUser();
        if(!user.equals(booking.getUser())){
            throw new UnauthorizedException("Booking does not belong to this user with id: " + user.getId());
        }
        if(booking.getBookingStatus()!= BookingStatus.CONFIRMED){
            throw new IllegalStateException("Only confirmed bookings can be cancelled");
        }

//        String sessionUrl = checkoutService.getCheckoutSession(booking,frontEndUrl+"/payments/success",frontEndUrl+"/payments/failure");
        booking.setBookingStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        inventoryRepository.getAllReservedInventoriesAndLock(booking.getRoom().getId(),booking.getCheckInDate(),booking.getCheckOutDate(),booking.getRoomsCount());
        inventoryRepository.cancelBooking(booking.getRoom().getId(),booking.getCheckInDate(),booking.getCheckOutDate(),booking.getRoomsCount());

        //handle refund
        try {
            Session session = Session.retrieve(booking.getPaymentSessionId());
            RefundCreateParams refundCreateParams = RefundCreateParams.builder()
                    .setPaymentIntent(session.getPaymentIntent())
                    .build();
            Refund.create(refundCreateParams);
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }

        log.info("Booking cancelled for booking id {} and Refund processed",bookingId);

    }

    @Override
    public String getBookingStatus(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(()-> new ResourceNotFoundException("Booking not found with id "+bookingId));

        User user = getCurrentUser();
        if(!user.equals(booking.getUser())){
            throw new UnauthorizedException("Booking does not belong to this user with id: " + user.getId());
        }
        return booking.getBookingStatus().toString();
    }

    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public Boolean isBookingActive(Booking booking){
        return booking.getCreatedAt().plusMinutes(10).isAfter(LocalDateTime.now());
    }
}
