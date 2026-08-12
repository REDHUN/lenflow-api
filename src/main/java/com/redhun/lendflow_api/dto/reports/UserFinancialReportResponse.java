package com.redhun.lendflow_api.dto.reports;


import java.math.BigDecimal;

public record UserFinancialReportResponse(

        Long userId,
        String userName,

        int totalDeposits,
        int activeDeposits,
        int closedDeposits,

        BigDecimal totalDeposited,
        BigDecimal activeDepositPrincipal,
        BigDecimal depositInterestPaid,

        int totalLoans,
        int activeLoans,
        int partiallyPaidLoans,
        int closedLoans,

        BigDecimal totalLoanPrincipal,
        BigDecimal loanPrincipalOutstanding,

        BigDecimal totalLoanInterest,
        BigDecimal loanInterestPaid,

        BigDecimal totalLoanRepaid,
        BigDecimal totalLoanOutstanding
) {
}
