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

        BigDecimal totalInterest,

        BigDecimal totalPayable,

        BigDecimal totalPaid,

        BigDecimal outstandingAmount,

        LocalDate startDate,

        LocalDate dueDate,

        LoanStatus status
) {
}