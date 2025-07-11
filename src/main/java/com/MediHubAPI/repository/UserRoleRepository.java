package com.MediHubAPI.repository;

import com.MediHubAPI.model.User;
import com.MediHubAPI.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
    List<UserRole> findByUser(User user);
}

