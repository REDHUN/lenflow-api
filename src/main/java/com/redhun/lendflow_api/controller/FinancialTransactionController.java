package com.redhun.lendflow_api.controller;

import com.redhun.lendflow_api.service.FinancialTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class FinancialTransactionController {

    private final FinancialTransactionService transactionService;
    @PreAuthorize("hasAnyRole('ADMIN', 'MEMBER')")
    @GetMapping("/available-balance")
    public BigDecimal getAvailableBalance() {

        return transactionService.getAvailableBalance();
    }
}