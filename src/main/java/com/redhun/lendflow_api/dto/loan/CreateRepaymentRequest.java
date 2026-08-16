package com.redhun.lendflow_api.dto.loan;

import com.redhun.lendflow_api.enums.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateRepaymentRequest(

        @NotNull
        @DecimalMin(
                value = "0.00",
                inclusive = true
        )
        BigDecimal interestAmount,

        @NotNull
        @DecimalMin(
                value = "0.00",
                inclusive = true
        )
        BigDecimal principalAmount,

        @NotNull
        PaymentMethod paymentMethod,

        @NotNull
        LocalDate repaymentDate,

        String notes
) {
}