package com.springboot.projects.airBnbApp.dto;

import com.springboot.projects.airBnbApp.entity.User;
import com.springboot.projects.airBnbApp.entity.enums.Gender;
import jakarta.persistence.*;
import lombok.Data;

@Data
public class GuestDto {

    private User user;

    private String name;

    private Gender gender;

    private Integer age;
}
