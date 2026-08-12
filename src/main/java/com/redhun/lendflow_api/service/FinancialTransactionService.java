package com.redhun.lendflow_api.service;



import java.math.BigDecimal;

public interface FinancialTransactionService {

    BigDecimal getAvailableBalance();
}