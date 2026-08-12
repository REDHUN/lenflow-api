package com.redhun.lendflow_api.dto.reports;

import com.redhun.lendflow_api.enums.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RepaymentReportResponse(

        Long repaymentId,

        Long loanId,
        String loanNumber,

        Long userId,
        String userName,

        BigDecimal amount,

        BigDecimal interestAmount,
        BigDecimal principalAmount,

        PaymentMethod paymentMethod,

        LocalDate repaymentDate,

        String notes
) {
}