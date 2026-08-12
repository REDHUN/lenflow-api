package com.redhun.lendflow_api.dto.reports;


import com.redhun.lendflow_api.enums.DepositStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DepositReportResponse(

        Long depositId,
        String depositNumber,

        Long userId,
        String userName,

        BigDecimal totalDeposited,
        BigDecimal interestRate,

        BigDecimal interestAmount,
        BigDecimal closingAmount,

        LocalDate startDate,
        LocalDate closedDate,

        DepositStatus status
) {
}