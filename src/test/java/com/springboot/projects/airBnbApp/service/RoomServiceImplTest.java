package com.springboot.projects.airBnbApp.service;

import com.springboot.projects.airBnbApp.TestContainerConfiguration;
import com.springboot.projects.airBnbApp.dto.RoomDto;
import com.springboot.projects.airBnbApp.entity.Hotel;
import com.springboot.projects.airBnbApp.entity.Room;
import com.springboot.projects.airBnbApp.entity.User;
import com.springboot.projects.airBnbApp.exception.ResourceNotFoundException;
import com.springboot.projects.airBnbApp.exception.UnauthorizedException;
import com.springboot.projects.airBnbApp.repository.HotelRepository;
import com.springboot.projects.airBnbApp.repository.RoomRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.module.ResolutionException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
//@Import(TestContainerConfiguration.class)
//@DataJpaTest
@ExtendWith(MockitoExtension.class)
class RoomServiceImplTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private HotelRepository hotelRepository;

    @Mock private InventoryService inventoryService;
    @InjectMocks
    private RoomServiceImpl roomService;


    @Mock
    private ModelMapper modelMapper;

   private RoomDto mockRoomDto;


    private Room mockRoom;

   private Hotel mockHotel;

   private User hotelOwner;
    private User user;

    @BeforeEach
    void setUp() {
         hotelOwner = User.builder().id(10L).build();
         user = User.builder().id(20L).build();
        mockRoom = Room.builder()
                .id(1L)
                .basePrice(BigDecimal.ONE)
                .type("Executive")
                .build();
         mockHotel = Hotel.builder()
                    .id(1L)
                    .name("Taj Palace")
                    .city("Mumbai")
                    .photos(new String[]{"https://example.com/images/hotel-front.jpg", "https://example.com/images/hotel-room.jpg"})
                    .amenities(new String[]{"Free WiFi",
                            "Breakfast included",
                            "Swimming pool",
                            "Gym"})
                    .owner(hotelOwner)
                    .rooms(List.of(mockRoom))
                    .build();

    }

    @Test
    void addRoomToHotel_whenHotelIdPresent() {
        mockRoomDto = getMockRoomDto();

        mockSecurityContext(hotelOwner);
        when(hotelRepository.findById(mockHotel.getId())).thenReturn(Optional.of(mockHotel));
        when(modelMapper.map(mockRoomDto, Room.class)).thenReturn(mockRoom);
        when(roomRepository.save(mockRoom)).thenReturn(mockRoom);
        when(modelMapper.map(mockRoom, RoomDto.class)).thenReturn(mockRoomDto);

        RoomDto roomDto = roomService.addRoomToHotel(mockHotel.getId(),mockRoomDto);

        assertThat(roomDto.getType()).isEqualTo("Executive");

        verify(inventoryService).addInventory(mockRoom);

    }
    @Test
    void addRoomToHotel_whenHotelIdNotPresent() {
        mockRoomDto = getMockRoomDto();

        when(hotelRepository.findById(3L)).thenReturn(Optional.<Hotel>empty());

        assertThrows(ResourceNotFoundException.class,()->roomService.addRoomToHotel(3L,mockRoomDto));


    }

    @Test
    void addRoomToHotel_whenOwnerNotPresent() {

        mockRoomDto = getMockRoomDto();
        mockRoom.setHotel(mockHotel);
        mockSecurityContext(user);
        when(hotelRepository.findById(mockHotel.getId())).thenReturn(Optional.of(mockHotel));
        when(modelMapper.map(mockRoomDto, Room.class)).thenReturn(mockRoom);


        assertThrows(UnauthorizedException.class,() -> roomService.addRoomToHotel(mockHotel.getId(),mockRoomDto));


        verify(hotelRepository).findById(mockHotel.getId());

    }



    @Test
    void getAllRoomsByHotelId_Success() {
        mockRoomDto = getMockRoomDto();
        List<RoomDto> roomListDto = List.of(mockRoomDto);
        when(hotelRepository.findById(mockHotel.getId())).thenReturn(Optional.of(mockHotel));
        when(modelMapper.map(mockRoom,RoomDto.class)).thenReturn(mockRoomDto);
        List<RoomDto> roomListDtoResult = roomService.getAllRoomsByHotelId(mockHotel.getId());

        assertThat(roomListDtoResult).hasSize(1);
        assertThat(roomListDtoResult.get(0).getType()).isEqualTo(roomListDto.get(0).getType());
    }

    @Test
    void getAllRoomsByHotelId_Failure() {
    }

    @Test
    void getRoomById() {
        mockRoomDto = getMockRoomDto();
        when(roomRepository.findById(mockRoom.getId())).thenReturn(Optional.of(mockRoom));
        when(modelMapper.map(mockRoom,RoomDto.class)).thenReturn(mockRoomDto);
        RoomDto roomDtoResult = roomService.getRoomById(mockRoom.getId());
        assertThat(mockRoom.getType()).isEqualTo(roomDtoResult.getType());
    }

    @Test
    void deleteRoomById_WhenRoomExists() {
        mockSecurityContext(hotelOwner);
        mockRoom.setHotel(mockHotel);
        when(roomRepository.existsById(mockRoom.getId())).thenReturn(Boolean.TRUE);
        when(roomRepository.findById(mockRoom.getId())).thenReturn(Optional.of(mockRoom));
        roomService.deleteRoomById(mockRoom.getId());
        verify(inventoryService).deleteRoomsByRoomId(mockRoom.getId());
        verify(roomRepository).deleteById(mockRoom.getId());
    }

    @Test
    void deleteRoomById_WhenRoomNotExists(){
        when(roomRepository.existsById(9L)).thenReturn(Boolean.FALSE);
        assertThrows(ResourceNotFoundException.class,()->roomService.deleteRoomById(9L));

    }
    @Test
    void deleteRoomById_WhenRoomExistsOwnerNotAuthorized(){
        mockSecurityContext(user);
        mockRoom.setHotel(mockHotel);
        when(roomRepository.existsById(mockRoom.getId())).thenReturn(Boolean.TRUE);
        when(roomRepository.findById(mockRoom.getId())).thenReturn(Optional.of(mockRoom));

        assertThrows(UnauthorizedException.class,()->roomService.deleteRoomById(mockRoom.getId()));
    }


    private void mockSecurityContext(User currentUser) {
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(currentUser);

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);

        SecurityContextHolder.setContext(context);
    }
    private RoomDto getMockRoomDto() {
        mockRoomDto = new RoomDto();
        mockRoomDto.setType(mockRoom.getType());
        mockRoomDto.setBasePrice(mockRoom.getBasePrice());
        return mockRoomDto;
    }

}


