package com.springboot.projects.airBnbApp.service;

import com.springboot.projects.airBnbApp.dto.HotelDto;
import com.springboot.projects.airBnbApp.dto.RoomDto;
import com.springboot.projects.airBnbApp.entity.Hotel;
import com.springboot.projects.airBnbApp.entity.Room;
import com.springboot.projects.airBnbApp.entity.User;
import com.springboot.projects.airBnbApp.exception.ResourceNotFoundException;
import com.springboot.projects.airBnbApp.exception.UnauthorizedException;
import com.springboot.projects.airBnbApp.repository.HotelRepository;
import com.springboot.projects.airBnbApp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class HotelServiceImpl implements HotelService{


    private final HotelRepository hotelRepository;
    private final ModelMapper modelMapper;
    private final UserRepository userRepository;
    @Override
    public HotelDto createNewHotel(HotelDto hotelDto) {
        log.info("Hotel name is : "+hotelDto.getName());
        Hotel hotel = modelMapper.map(hotelDto,Hotel.class);
//        hotel.setActive(false);
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();


        log.info("User while creating hotel is {}",user);
        hotel.setOwner(user);
        hotel = hotelRepository.save(hotel);
        log.info("Added new Hotel : {} : {} ",hotel.getId(),hotel.getName());
        return modelMapper.map(hotel,HotelDto.class);
    }

    @Override
    public HotelDto getHotelById(Long id) {
        log.info("Finding Hotel with id {}",id);
        Hotel hotel = hotelRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Hotel not found with ID : "+id));
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.equals(hotel.getOwner())){
            throw new UnauthorizedException("This user does not own this hotel : "+user.getId());
        }
        return modelMapper.map(hotel,HotelDto.class);
    }

    @Override
    public List<RoomDto> getHotelInfoById(Long hotelId) {
        log.info("Getting Hotel info from Hotel id {}",hotelId);
        Hotel hotel = hotelRepository.findById(hotelId).orElseThrow(() -> new ResourceNotFoundException("Hotel not found with Id : "+hotelId));
        List<Room> rooms = hotel.getRooms();
        return rooms.stream().map((element)->modelMapper.map(element, RoomDto.class)).collect(Collectors.toList());
    }

}
