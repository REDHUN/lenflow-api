package com.redhun.lendflow_api.dto.desposit;


import com.redhun.lendflow_api.enums.DepositStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DepositResponse(

        Long id,
        String depositNumber,
        Long userId,
        String userName,

        BigDecimal totalAmount,
        BigDecimal interestRate,
        BigDecimal interestAmount,
        BigDecimal closingAmount,

        LocalDate startDate,
        LocalDate closedDate,

        DepositStatus status
) {
}