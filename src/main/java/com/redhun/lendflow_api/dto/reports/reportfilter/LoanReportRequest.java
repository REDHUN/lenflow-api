package com.redhun.lendflow_api.dto.reports.reportfilter;


import com.redhun.lendflow_api.enums.LoanStatus;

import java.time.LocalDate;

public record LoanReportRequest(

        LocalDate fromDate,

        LocalDate toDate,

        Long userId,

        LoanStatus status

) {
}