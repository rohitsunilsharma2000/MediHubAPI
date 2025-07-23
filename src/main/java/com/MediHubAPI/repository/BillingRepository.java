package com.MediHubAPI.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BillingRepository<Billing> extends JpaRepository<Billing, Long> {
}
