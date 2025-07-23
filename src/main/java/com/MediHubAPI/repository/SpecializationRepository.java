package com.MediHubAPI.repository;

import com.MediHubAPI.model.Specialization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpecializationRepository extends JpaRepository<Specialization, Long> {
    // Optional: Add a method to find by name if needed
    // Optional<Specialization> findByName(String name);
}
