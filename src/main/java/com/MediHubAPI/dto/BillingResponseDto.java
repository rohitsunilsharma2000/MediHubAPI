package com.MediHubAPI.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BillingResponseDto {
    private Long billId;
    private String patientName;
    private String itemName;
    private Double billedAmount;
    private Double paidAmount;
    private Double discount;
    private String paymentMode;
    private String cardType;
    private String billingDate;
}
