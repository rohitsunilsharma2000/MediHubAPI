package com.MediHubAPI.service;



import com.MediHubAPI.dto.UserCreateDto;
import com.MediHubAPI.dto.UserDto;
import com.MediHubAPI.model.ERole;

import java.util.List;
import java.util.Set;

public interface UserService {
    UserDto createUser(UserCreateDto userCreateDto);
    List<UserDto> getAllUsers();
    UserDto getUserById(Long id);
    void deleteUser(Long id);

    void updateUserStatus(Long userId, boolean enabled);
    UserDto updateUserRolesByUsername(String username, Set<ERole> roles);
}
