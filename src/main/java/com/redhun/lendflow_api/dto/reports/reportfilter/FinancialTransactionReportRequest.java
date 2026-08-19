package com.redhun.lendflow_api.dto.reports.reportfilter;


import com.redhun.lendflow_api.enums.TransactionType;

import java.time.LocalDate;

public record FinancialTransactionReportRequest(

        LocalDate fromDate,

        LocalDate toDate,

        Long userId,

        Long loanId,

        Long depositId,

        TransactionType transactionType

) {
}
