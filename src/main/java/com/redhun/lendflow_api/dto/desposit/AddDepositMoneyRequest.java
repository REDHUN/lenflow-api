package com.redhun.lendflow_api.dto.desposit;

import com.redhun.lendflow_api.enums.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AddDepositMoneyRequest(

        @NotNull
        @DecimalMin(value = "0.0", inclusive = false)
        BigDecimal amount,

        @NotNull
        PaymentMethod paymentMethod,

        @NotNull
        LocalDate transactionDate,

        String notes
) {
}