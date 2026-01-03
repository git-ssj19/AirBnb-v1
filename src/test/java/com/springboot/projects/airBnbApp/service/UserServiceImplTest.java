package com.springboot.projects.airBnbApp.service;

import com.springboot.projects.airBnbApp.TestContainerConfiguration;
import com.springboot.projects.airBnbApp.entity.User;
import com.springboot.projects.airBnbApp.entity.enums.Role;
import com.springboot.projects.airBnbApp.exception.ResourceNotFoundException;
import com.springboot.projects.airBnbApp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.testcontainers.shaded.org.apache.commons.lang3.ObjectUtils;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestContainerConfiguration.class)
//@DataJpaTest
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .id(1L)
                .email("dummy_user@gmail.com")
                .name("dummy_user")
                .password("kgjb")
                .roles(Set.of(Role.GUEST))
                .build();
    }

    @Test
    void getUserById_whenUserIsPresent() {

        Long id = mockUser.getId();
        when(userRepository.findById(id)).thenReturn(Optional.of(mockUser));

        User user = userService.getUserById(mockUser.getId());

        assertThat(user.getEmail()).isEqualTo(mockUser.getEmail());
    }

    @Test
    void getUserById_whenUserIsNotPresent() {

        Long id = 8L;
        when(userRepository.findById(id)).thenReturn(Optional.<User>empty());

//        User user = userService.getUserById(id);
        assertThrows(ResourceNotFoundException.class,()->userService.getUserById(id));

        verify(userRepository, times(1)).findById(id);
    }

    @Test
    void loadUserByUsername_whenUserIsPresent() {
        String email = mockUser.getEmail();
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockUser));

        User user = (User) userService.loadUserByUsername(email);
        assertThat(user.getUsername()).isEqualTo(mockUser.getUsername());
        verify(userRepository,times(1)).findByEmail(email);
    }
    @Test
    void loadUserByUsername_whenUserIsNotPresent() {
        String email = "emailNotpresent@gmail.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.<User>empty());

        assertThrows(ResourceNotFoundException.class,()->userService.loadUserByUsername(email));

        verify(userRepository).findByEmail(email);
    }
}