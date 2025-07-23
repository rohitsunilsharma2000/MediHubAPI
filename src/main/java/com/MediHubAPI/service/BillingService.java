package com.MediHubAPI.service;

import com.MediHubAPI.dto.BillingRequestDto;
import com.MediHubAPI.dto.BillingResponseDto;
import com.MediHubAPI.dto.DiscountRequestDto;

import java.util.List;

public interface BillingService {
    BillingResponseDto createBill(BillingRequestDto dto);
    List<String> getConsultationItems();
    BillingResponseDto applyDiscount(Long billId, DiscountRequestDto dto);
}

