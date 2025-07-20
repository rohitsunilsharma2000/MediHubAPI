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


    private String fullName; // optional, can be set from first + last
    private String mobile;
    private String gender;
    private LocalDate dateOfBirth;
    private String specialization; // for doctors
    private boolean enabled;
//
//    public Long getId() {
//        return id;
//    }
//
//    public void setId(Long id) {
//        this.id = id;
//    }
//
//    public String getEmail() {
//        return email;
//    }
//
//    public void setEmail(String email) {
//        this.email = email;
//    }
//
//    public String getFirstName() {
//        return firstName;
//    }
//
//    public void setFirstName(String firstName) {
//        this.firstName = firstName;
//    }
//
//    public Set<ERole> getRoles() {
//        return roles;
//    }
//
//    public void setRoles(Set<ERole> roles) {
//        this.roles = roles;
//    }
//
//    public String getLastName() {
//        return lastName;
//    }
//
//    public void setLastName(String lastName) {
//        this.lastName = lastName;
//    }
}