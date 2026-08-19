package com.redhun.lendflow_api.dto.reports.reportfilter;


import com.redhun.lendflow_api.enums.PaymentMethod;

import java.time.LocalDate;

public record RepaymentReportRequest(

        LocalDate fromDate,

        LocalDate toDate,

        Long userId,

        Long loanId,

        PaymentMethod paymentMethod

) {
}