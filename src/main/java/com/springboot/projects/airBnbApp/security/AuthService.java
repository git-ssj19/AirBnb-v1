package com.springboot.projects.airBnbApp.security;

import com.springboot.projects.airBnbApp.dto.LoginDto;
import com.springboot.projects.airBnbApp.dto.SignUPRequsetDto;
import com.springboot.projects.airBnbApp.dto.UserDto;
import com.springboot.projects.airBnbApp.entity.User;
import com.springboot.projects.airBnbApp.entity.enums.Role;
import com.springboot.projects.airBnbApp.exception.ResourceNotFoundException;
import com.springboot.projects.airBnbApp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService;
    public UserDto signup(SignUPRequsetDto signUPRequsetDto){
        User user =  userRepository.findByEmail(signUPRequsetDto.getEmail()).orElse(null);

        if(user != null){
            throw new RuntimeException("User is already present with same email id");
        }

        User newUser  = modelMapper.map(signUPRequsetDto,User.class);
        newUser.setRoles(Set.of(Role.HOTEL_MANAGER));
        newUser.setPassword(passwordEncoder.encode(signUPRequsetDto.getPassword()));
        newUser = userRepository.save(newUser);
        return modelMapper.map(newUser, UserDto.class);

    }

    public String[] login(LoginDto logindto){
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                logindto.getEmail(),logindto.getPassword()
        ));

        User user = (User) authentication.getPrincipal();

        String[] arr  = new String[2];
        arr[0] = jwtService.generateAccessToken(user);
        arr[1] = jwtService.generateRefreshToken(user);

        return arr;
     }


    public String refreshToken(String refreshToken) {
        Long id = jwtService.getUserIdFromToken(refreshToken);
        User user = userRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("No user found with id "+id));
        return jwtService.generateAccessToken(user);
    }
}
