package com.redhun.lendflow_api.dto.loan;


import com.redhun.lendflow_api.enums.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RepaymentResponse(

        Long id,

        Long loanId,

        String loanNumber,

        Long userId,

        String userName,

        BigDecimal amount,

        BigDecimal principalAmount,

        BigDecimal interestAmount,

        PaymentMethod paymentMethod,

        LocalDate repaymentDate,

        String notes
) {
}