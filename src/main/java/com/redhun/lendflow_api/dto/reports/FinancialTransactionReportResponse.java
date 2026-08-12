package com.redhun.lendflow_api.dto.reports;

import com.redhun.lendflow_api.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FinancialTransactionReportResponse(

        Long transactionId,

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
        String depositNumber
) {
}