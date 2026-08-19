package com.redhun.lendflow_api.controller;


import com.redhun.lendflow_api.dto.ledger.LedgerTransactionResponse;
import com.redhun.lendflow_api.service.LedgerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ledger")
@RequiredArgsConstructor
public class LedgerController {

    private final LedgerService ledgerService;


    // =========================================================
    // GET ALL TRANSACTIONS
    // =========================================================

    @GetMapping("/transactions")
    public List<LedgerTransactionResponse> getAllTransactions() {

        return ledgerService.getAllTransactions();
    }


    // =========================================================
    // GET USER TRANSACTIONS
    // =========================================================

    @GetMapping("/transactions/user/{userId}")
    public List<LedgerTransactionResponse> getUserTransactions(
            @PathVariable Long userId
    ) {

        return ledgerService.getUserTransactions(userId);
    }


    // =========================================================
    // GET TRANSACTION BY ID
    // =========================================================

    @GetMapping("/transactions/{transactionId}")
    public LedgerTransactionResponse getTransaction(
            @PathVariable Long transactionId
    ) {

        return ledgerService.getTransaction(transactionId);
    }
}