package com.redhun.lendflow_api.dto.reports;


import com.redhun.lendflow_api.enums.LoanStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LoanReportResponse(

        Long loanId,

        String loanNumber,

        Long userId,

        String userName,

        BigDecimal principalAmount,

        BigDecimal interestRate,

        BigDecimal totalInterest,

        BigDecimal totalPayable,

        BigDecimal totalPaid,

        BigDecimal interestPaid,

        BigDecimal principalPaid,

        BigDecimal interestOutstanding,

        BigDecimal principalOutstanding,

        BigDecimal totalOutstanding,

        LocalDate startDate,

        LocalDate dueDate,

        LoanStatus status
) {
}