package com.redhun.lendflow_api.controller;

import com.redhun.lendflow_api.dto.reports.DepositReportResponse;
import com.redhun.lendflow_api.dto.reports.FinancialTransactionReportResponse;
import com.redhun.lendflow_api.dto.reports.LoanReportResponse;
import com.redhun.lendflow_api.dto.reports.ProfitReportResponse;
import com.redhun.lendflow_api.dto.reports.RepaymentReportResponse;
import com.redhun.lendflow_api.dto.reports.UserFinancialReportResponse;
import com.redhun.lendflow_api.dto.reports.reportfilter.DepositReportRequest;
import com.redhun.lendflow_api.dto.reports.reportfilter.FinancialTransactionReportRequest;
import com.redhun.lendflow_api.dto.reports.reportfilter.LoanReportRequest;
import com.redhun.lendflow_api.dto.reports.reportfilter.RepaymentReportRequest;
import com.redhun.lendflow_api.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;


    // =========================================================
    // LOAN REPORT
    // =========================================================

    @PostMapping("/loans")
    public List<LoanReportResponse> getLoanReport(
            @RequestBody LoanReportRequest request
    ) {

        return reportService.getLoanReport(request);
    }


    // =========================================================
    // DEPOSIT REPORT
    // =========================================================

    @PostMapping("/deposits")
    public List<DepositReportResponse> getDepositReport(
            @RequestBody DepositReportRequest request
    ) {

        return reportService.getDepositReport(request);
    }


    // =========================================================
    // REPAYMENT REPORT
    // =========================================================

    @PostMapping("/repayments")
    public List<RepaymentReportResponse> getRepaymentReport(
            @RequestBody RepaymentReportRequest request
    ) {

        return reportService.getRepaymentReport(request);
    }


    // =========================================================
    // FINANCIAL TRANSACTION / LEDGER REPORT
    // =========================================================

    @PostMapping("/transactions")
    public List<FinancialTransactionReportResponse>
    getFinancialTransactionReport(
            @RequestBody FinancialTransactionReportRequest request
    ) {

        return reportService
                .getFinancialTransactionReport(request);
    }


    // =========================================================
    // PROFIT REPORT
    // =========================================================

    @GetMapping("/profit")
    public ProfitReportResponse getProfitReport() {

        return reportService.getProfitReport();
    }


    // =========================================================
    // USER FINANCIAL REPORT
    // =========================================================

    @GetMapping("/users/{userId}")
    public UserFinancialReportResponse
    getUserFinancialReport(
            @PathVariable Long userId
    ) {

        return reportService
                .getUserFinancialReport(userId);
    }
}