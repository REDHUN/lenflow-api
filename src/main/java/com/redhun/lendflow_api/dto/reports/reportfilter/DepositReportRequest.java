package com.redhun.lendflow_api.dto.reports.reportfilter;


import com.redhun.lendflow_api.enums.DepositStatus;

import java.time.LocalDate;

public record DepositReportRequest(

        LocalDate fromDate,

        LocalDate toDate,

        Long userId,

        DepositStatus status

) {
}