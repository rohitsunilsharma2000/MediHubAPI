package com.MediHubAPI.dto;

import lombok.Data;

@Data
public class DiscountRequestDto {
    private Double discountPercentage;
    private String reason;
    private Long approverId;
}

