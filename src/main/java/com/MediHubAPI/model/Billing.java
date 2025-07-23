package com.MediHubAPI.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "billing")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Billing {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User patient;

    private String itemName;

    private Double unitPrice;

    private Integer quantity;

    private Double discount;

    private Double totalAmount;

    private String paymentMode;

    private String cardType;

    private Double amountPaid;

    private String comments;

    private LocalDate billingDate = LocalDate.now();
}
