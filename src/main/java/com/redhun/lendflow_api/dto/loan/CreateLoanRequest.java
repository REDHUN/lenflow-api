package com.redhun.lendflow_api.dto.loan;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateLoanRequest(

        @NotNull
        Long userId,

        @NotNull
        @DecimalMin(value = "0.0", inclusive = false)
        BigDecimal principalAmount,

        @NotNull
        @DecimalMin(value = "0.0", inclusive = false)
        BigDecimal interestRate,

        @NotNull
        LocalDate startDate

) {
}