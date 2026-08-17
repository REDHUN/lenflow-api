package com.redhun.lendflow_api.dto.desposit;


import java.math.BigDecimal;
import java.time.LocalDate;

public record DepositTransactionResponse(

        Long id,

        Long depositId,

        String depositNumber,

        BigDecimal amount,

        LocalDate transactionDate


) {
}