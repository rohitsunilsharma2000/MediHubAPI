package com.MediHubAPI.repository;


import com.MediHubAPI.model.ERole;
import com.MediHubAPI.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByName(ERole name);
    boolean existsByName(ERole name);
}
