package com.MediHubAPI.controller;

import com.MediHubAPI.dto.*;
import com.MediHubAPI.service.BillingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/billing")
@RequiredArgsConstructor
@Slf4j
public class BillingController {

    private final BillingService billingService;

    @PostMapping("/opd")
    public ResponseEntity<ApiResponse<BillingResponseDto>> createOpdBill(@Valid @RequestBody BillingRequestDto dto) {
        log.info("Creating OPD bill for patientId={}", dto.getPatientId());
        BillingResponseDto response = billingService.createBill(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response, "/billing/opd", "OPD bill created"));
    }

    @GetMapping("/items/consultation")
    public ResponseEntity<ApiResponse<List<String>>> getBillableItems() {
        List<String> items = billingService.getConsultationItems();
        return ResponseEntity.ok(ApiResponse.success(items, "/billing/items/consultation", "Fetched items"));
    }

    @PutMapping("/{id}/discount")
    public ResponseEntity<ApiResponse<BillingResponseDto>> applyDiscount(@PathVariable Long id,
                                                                         @Valid @RequestBody DiscountRequestDto dto) {
        BillingResponseDto updated = billingService.applyDiscount(id, dto);
        return ResponseEntity.ok(ApiResponse.success(updated, "/billing/" + id + "/discount", "Discount applied"));
    }
}
