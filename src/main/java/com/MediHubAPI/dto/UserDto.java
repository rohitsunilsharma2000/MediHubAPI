package com.MediHubAPI.dto;


import com.MediHubAPI.model.ERole;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Set;

@Data
@Setter
@Getter
public class UserDto {
    private Long id;

    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private Set<ERole> roles;


//    private String fullName; // optional, can be set from first + last
//    private String mobile;
//    private String gender;
//    private LocalDate dateOfBirth;
    private String specialization; // for doctors
    private boolean enabled;
}