package com.springboot.projects.airBnbApp.dto;

import lombok.Data;

@Data
public class SignUPRequsetDto {
    private String name;
    private String email;
    private String password;
}
