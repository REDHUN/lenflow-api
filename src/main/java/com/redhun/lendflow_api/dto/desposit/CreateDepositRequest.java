package com.redhun.lendflow_api.dto.desposit;


import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateDepositRequest(

        @NotNull
        Long userId,

        @NotNull
        @DecimalMin(value = "0.0", inclusive = false)
        BigDecimal initialAmount,

        @NotNull
        @DecimalMin(value = "0.0", inclusive = false)
        BigDecimal interestRate,

        @NotNull
        LocalDate startDate
) {
}
