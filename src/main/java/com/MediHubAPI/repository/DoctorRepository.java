package com.MediHubAPI.repository;

import com.MediHubAPI.model.User;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

@Repository
public interface DoctorRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
}
