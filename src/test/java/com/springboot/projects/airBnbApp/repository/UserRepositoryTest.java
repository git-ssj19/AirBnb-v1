package com.springboot.projects.airBnbApp.repository;

import com.springboot.projects.airBnbApp.TestContainerConfiguration;
import com.springboot.projects.airBnbApp.entity.User;
import com.springboot.projects.airBnbApp.entity.enums.Role;
import com.springboot.projects.airBnbApp.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;


import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;


import static org.assertj.core.api.Assertions.*;


@Import(TestContainerConfiguration.class)
@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setUp(){
        user = User.builder()
                .email("dummy@gmail.com")
                .roles(Set.of(Role.GUEST))
                .password("akjbr")
                .build();

    }

    @Test
    @Transactional
    void findByEmail_when_EmailIsPresent(){
        userRepository.save(user);
        Optional<User> user1 = userRepository.findByEmail(user.getEmail());
        List<User> userList = user1.stream().toList();
        assertThat(userList.size()).isEqualTo(1);
        assertThat(userList.get(0).getEmail()).isEqualTo(user.getEmail()).isNotEmpty();
        System.out.println("VM TimeZone: " + TimeZone.getDefault().getID());
    }

    @Test
    void findByEmail_when_EmailIsNotPresent(){
//        .orElseThrow(()-> new ResourceNotFoundException("resource not found junit"))
        Optional<User> user1 = userRepository.findByEmail("asdhffhkj@gmail.com");
        List<User> userList = user1.stream().toList();
        assertThat(userList.size()).isEqualTo(0);
    }
}