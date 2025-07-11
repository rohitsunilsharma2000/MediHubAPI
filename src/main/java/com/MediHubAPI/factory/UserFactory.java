package com.MediHubAPI.factory;

import com.MediHubAPI.dto.UserCreateDto;
import com.MediHubAPI.exception.HospitalAPIException;
import com.MediHubAPI.model.ERole;
import com.MediHubAPI.model.Role;
import com.MediHubAPI.model.User;
import com.MediHubAPI.repository.RoleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class UserFactory {

    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserFactory(RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User createUser(UserCreateDto dto) {
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword())); // if applicable
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());

        Set<Role> roles = new HashSet<>();
        for (ERole roleEnum : dto.getRoles()) {
            Role role = roleRepository.findByName(roleEnum)
                    .orElseThrow(() -> new HospitalAPIException(HttpStatus.BAD_REQUEST, "Role not found: " + roleEnum));
            roles.add(role);
        }

        user.setRoles(roles);
        return user;
    }

}
