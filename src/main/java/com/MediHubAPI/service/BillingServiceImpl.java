package com.MediHubAPI.service;

import com.MediHubAPI.dto.BillingRequestDto;
import com.MediHubAPI.dto.BillingResponseDto;
import com.MediHubAPI.dto.DiscountRequestDto;
import com.MediHubAPI.exception.HospitalAPIException;
import com.MediHubAPI.model.Billing;
import com.MediHubAPI.model.User;
import com.MediHubAPI.repository.BillingRepository;
import com.MediHubAPI.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BillingServiceImpl implements BillingService {

    private final BillingRepository billingRepository;
    private final UserRepository userRepository;


//    @Override
//    public BillingResponseDto createBill(BillingRequestDto dto) {
//        return null;
//    }
    @Override
    public BillingResponseDto createBill(BillingRequestDto dto) {
        User patient = userRepository.findById(dto.getPatientId())
                .orElseThrow(() -> new HospitalAPIException(HttpStatus.NOT_FOUND, "Patient not found"));

        double unitPrice = 150; // Assume fetched from predefined list
        int quantity = 1;
        double discount = 0;
        double total = unitPrice * quantity;

        Billing bill = Billing.builder()
                .patient(patient)
                .itemName("Consultation")
                .unitPrice(unitPrice)
                .quantity(quantity)
                .discount(discount)
                .totalAmount(total)
                .paymentMode(dto.getPaymentMode())
                .cardType(dto.getCardType())
                .amountPaid(dto.getAmountPaid())
                .comments(dto.getComments())
                .build();

        billingRepository.save(bill);

        return BillingResponseDto.builder()
                .billId(bill.getId())
                .patientName(patient.getFirstName() + " " + patient.getLastName())
                .itemName(bill.getItemName())
                .billedAmount(bill.getTotalAmount())
                .paidAmount(bill.getAmountPaid())
                .discount(bill.getDiscount())
                .paymentMode(bill.getPaymentMode())
                .cardType(bill.getCardType())
                .billingDate(bill.getBillingDate().toString())
                .build();
    }


    @Override
    public List<String> getConsultationItems() {
        return List.of("Consultation - Dr. Kailash", "Specialist - Dr. Mehta");
    }

    @Override
    public BillingResponseDto applyDiscount(Long billId, DiscountRequestDto dto) {
        Billing bill = billingRepository.findById(billId)
                .orElseThrow(() -> new HospitalAPIException(HttpStatus.NOT_FOUND, "Bill not found"));

        double discountAmount = (dto.getDiscountPercentage() / 100.0) * bill.getTotalAmount();
        bill.setDiscount(discountAmount);
        bill.setTotalAmount(bill.getTotalAmount() - discountAmount);
        billingRepository.save(bill);

        return BillingResponseDto.builder()
                .billId(bill.getId())
                .patientName(bill.getPatient().getFirstName() + " " + bill.getPatient().getLastName())
                .itemName(bill.getItemName())
                .billedAmount(bill.getUnitPrice())
                .paidAmount(bill.getAmountPaid())
                .discount(bill.getDiscount())
                .paymentMode(bill.getPaymentMode())
                .cardType(bill.getCardType())
                .billingDate(bill.getBillingDate().toString())
                .build();
    }


}
