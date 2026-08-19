package com.redhun.lendflow_api.service;


import com.redhun.lendflow_api.dto.ledger.LedgerTransactionResponse;

import java.util.List;

public interface LedgerService {

    List<LedgerTransactionResponse> getAllTransactions();

    List<LedgerTransactionResponse> getUserTransactions(
            Long userId
    );

    LedgerTransactionResponse getTransaction(
            Long transactionId
    );
}