package com.redhun.lendflow_api.service;

import com.redhun.lendflow_api.dto.reports.*;
import com.redhun.lendflow_api.dto.reports.reportfilter.DepositReportRequest;
import com.redhun.lendflow_api.dto.reports.reportfilter.FinancialTransactionReportRequest;
import com.redhun.lendflow_api.dto.reports.reportfilter.LoanReportRequest;
import com.redhun.lendflow_api.dto.reports.reportfilter.RepaymentReportRequest;

import java.util.List;

public interface ReportService {

    List<LoanReportResponse> getLoanReport(
            LoanReportRequest request
    );

    List<DepositReportResponse> getDepositReport(
            DepositReportRequest request
    );

    List<RepaymentReportResponse> getRepaymentReport(
            RepaymentReportRequest request
    );

    List<FinancialTransactionReportResponse>
    getFinancialTransactionReport(
            FinancialTransactionReportRequest request
    );

    ProfitReportResponse getProfitReport();

    UserFinancialReportResponse getUserFinancialReport(
            Long userId
    );
}