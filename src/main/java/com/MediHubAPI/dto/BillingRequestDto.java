package com.MediHubAPI.dto;

import lombok.Data;

@Data
public class BillingRequestDto {
    private Long patientId;
    private Long consultationItemId;
    private String paymentMode;      // e.g. CASH, CREDIT, UPI
    private String cardType;         // Optional, e.g., VISA, MasterCard
    private Double amountPaid;
    private String comments;
}
