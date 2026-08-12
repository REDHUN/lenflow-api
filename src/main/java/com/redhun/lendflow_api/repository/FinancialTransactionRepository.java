package com.redhun.lendflow_api.repository;

import com.redhun.lendflow_api.entity.FinancialTransaction;
import com.redhun.lendflow_api.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FinancialTransactionRepository
        extends JpaRepository<FinancialTransaction, Long> {

    List<FinancialTransaction> findByType(
            TransactionType type
    );

    List<FinancialTransaction> findByLoanId(
            Long loanId
    );

    List<FinancialTransaction> findByDepositId(
            Long depositId
    );

    List<FinancialTransaction> findByUserId(
            Long userId
    );

}