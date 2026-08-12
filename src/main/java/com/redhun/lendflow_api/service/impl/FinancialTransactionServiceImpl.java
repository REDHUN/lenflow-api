package com.redhun.lendflow_api.service.impl;

import com.redhun.lendflow_api.entity.FinancialTransaction;
import com.redhun.lendflow_api.enums.TransactionType;
import com.redhun.lendflow_api.repository.FinancialTransactionRepository;
import com.redhun.lendflow_api.service.FinancialTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FinancialTransactionServiceImpl
        implements FinancialTransactionService {

    private final FinancialTransactionRepository transactionRepository;

    @Override
    public BigDecimal getAvailableBalance() {

        List<FinancialTransaction> transactions =
                transactionRepository.findAll();

        BigDecimal moneyIn = BigDecimal.ZERO;
        BigDecimal moneyOut = BigDecimal.ZERO;

        for (FinancialTransaction transaction : transactions) {

            if (isMoneyIn(transaction.getType())) {

                moneyIn = moneyIn.add(
                        transaction.getAmount()
                );

            } else if (isMoneyOut(transaction.getType())) {

                moneyOut = moneyOut.add(
                        transaction.getAmount()
                );
            }
        }

        return moneyIn.subtract(moneyOut);
    }

    private boolean isMoneyIn(
            TransactionType type
    ) {

        return type == TransactionType.DEPOSIT_RECEIVED
                || type == TransactionType.LOAN_REPAYMENT;
    }

    private boolean isMoneyOut(
            TransactionType type
    ) {

        return type == TransactionType.LOAN_DISBURSEMENT
                || type == TransactionType.DEPOSIT_CLOSURE
                || type == TransactionType.EXPENSE;
    }
}