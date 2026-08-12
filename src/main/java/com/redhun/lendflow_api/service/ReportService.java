package com.redhun.lendflow_api.service;
import com.redhun.lendflow_api.dto.reports.*;

import java.util.List;

public interface ReportService {

    List<LoanReportResponse> getLoanReport();

    List<DepositReportResponse> getDepositReport();

    List<RepaymentReportResponse> getRepaymentReport();

    List<FinancialTransactionReportResponse>
    getFinancialTransactionReport();
    ProfitReportResponse getProfitReport();
    UserFinancialReportResponse
    getUserFinancialReport(Long userId);
}