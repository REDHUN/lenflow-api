package com.redhun.lendflow_api.dto.dashboard;


import java.math.BigDecimal;

public record DashboardResponse(

        // =========================
        // USERS
        // =========================

        long totalUsers,
        long activeUsers,


        // =========================
        // DEPOSITS
        // =========================

        long totalDeposits,
        long activeDeposits,
        long closedDeposits,

        // Current amount owed to active depositors
        BigDecimal activeDepositPrincipal,

        // Historical amount received through deposits
        BigDecimal totalDepositedHistorically,

        // Principal already returned to depositors
        BigDecimal depositPrincipalReturned,

        // Interest actually paid to depositors
        BigDecimal depositInterestPaid,


        // =========================
        // LOANS
        // =========================

        long totalLoans,
        long activeLoans,
        long partiallyPaidLoans,
        long closedLoans,

        // Original loan principal
        BigDecimal totalLoanPrincipal,

        // Current principal still owed by borrowers
        BigDecimal loanPrincipalOutstanding,

        // Total fixed interest contracted on all loans
        BigDecimal totalLoanInterest,

        // Interest actually received from repayments
        BigDecimal loanInterestReceived,

        // Total amount actually received from borrowers
        BigDecimal totalLoanRepaid,

        // Principal + remaining interest
        BigDecimal totalLoanOutstanding,


        // =========================
        // CASH
        // =========================

        BigDecimal availableBalance,


        // =========================
        // EXPENSE / PROFIT
        // =========================

        BigDecimal totalExpenses,

        BigDecimal netProfit

) {
}
