package com.redhun.lendflow_api.dto.ledger;


import com.redhun.lendflow_api.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record LedgerTransactionResponse(

        Long id,

        String transactionNumber,

        TransactionType type,

        BigDecimal amount,

        LocalDate transactionDate,

        String description,

        Long userId,

        String userName,

        Long loanId,

        String loanNumber,

        Long depositId,

        String depositNumber,

        LocalDateTime createdAt

) {
}