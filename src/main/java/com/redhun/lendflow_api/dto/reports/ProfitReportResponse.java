package com.redhun.lendflow_api.dto.reports;


import java.math.BigDecimal;

public record ProfitReportResponse(

        BigDecimal loanInterestContracted,

        BigDecimal loanInterestReceived,

        BigDecimal loanInterestOutstanding,

        BigDecimal depositInterestPaid,

        BigDecimal netInterestProfit,

        BigDecimal availableBalance,

        BigDecimal netProfit
) {
}
