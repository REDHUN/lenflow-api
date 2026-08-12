package com.redhun.lendflow_api.controller;

import com.redhun.lendflow_api.dto.reports.*;
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

    @GetMapping("/loans")
    public List<LoanReportResponse> getLoanReport() {

        return reportService.getLoanReport();
    }


    // =========================================================
    // DEPOSIT REPORT
    // =========================================================

    @GetMapping("/deposits")
    public List<DepositReportResponse> getDepositReport() {

        return reportService.getDepositReport();
    }


    // =========================================================
    // REPAYMENT REPORT
    // =========================================================

    @GetMapping("/repayments")
    public List<RepaymentReportResponse>
    getRepaymentReport() {

        return reportService.getRepaymentReport();
    }


    // =========================================================
    // FINANCIAL TRANSACTION REPORT
    // =========================================================

    @GetMapping("/transactions")
    public List<FinancialTransactionReportResponse>
    getFinancialTransactionReport() {

        return reportService
                .getFinancialTransactionReport();
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