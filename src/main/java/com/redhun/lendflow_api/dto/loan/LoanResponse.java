package com.redhun.lendflow_api.dto.loan;

import com.redhun.lendflow_api.enums.LoanStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LoanResponse(

        Long id,

        String loanNumber,

        Long userId,

        String userName,

        BigDecimal principalAmount,

        BigDecimal interestRate,

        BigDecimal accruedInterest,

        BigDecimal totalPayable,

        BigDecimal totalPaid,

        BigDecimal outstandingPrincipal,

        BigDecimal outstandingInterest,

        BigDecimal outstandingAmount,

        LocalDate startDate,

        LocalDate lastInterestAccruedDate,

        LoanStatus status
) {
}