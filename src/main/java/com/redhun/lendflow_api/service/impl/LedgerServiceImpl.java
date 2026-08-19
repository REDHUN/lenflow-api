package com.redhun.lendflow_api.service.impl;


import com.redhun.lendflow_api.dto.ledger.LedgerTransactionResponse;
import com.redhun.lendflow_api.entity.FinancialTransaction;
import com.redhun.lendflow_api.exception.ResourceNotFoundException;
import com.redhun.lendflow_api.repository.FinancialTransactionRepository;
import com.redhun.lendflow_api.service.LedgerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LedgerServiceImpl implements LedgerService {

    private final FinancialTransactionRepository
            financialTransactionRepository;


    @Override
    public List<LedgerTransactionResponse> getAllTransactions() {

        return financialTransactionRepository
                .findAll()
                .stream()
                .map(this::buildResponse)
                .toList();
    }


    @Override
    public List<LedgerTransactionResponse> getUserTransactions(
            Long userId
    ) {

        return financialTransactionRepository
                .findByUserId(userId)
                .stream()
                .map(this::buildResponse)
                .toList();
    }


    @Override
    public LedgerTransactionResponse getTransaction(
            Long transactionId
    ) {

        FinancialTransaction transaction =
                financialTransactionRepository
                        .findById(transactionId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Ledger transaction not found with id: "
                                                + transactionId
                                )
                        );

        return buildResponse(transaction);
    }


    private LedgerTransactionResponse buildResponse(
            FinancialTransaction transaction
    ) {

        return new LedgerTransactionResponse(

                transaction.getId(),

                transaction.getTransactionNumber(),

                transaction.getType(),

                transaction.getAmount(),

                transaction.getTransactionDate(),

                transaction.getDescription(),

                transaction.getUser() != null
                        ? transaction.getUser().getId()
                        : null,

                transaction.getUser() != null
                        ? transaction.getUser().getName()
                        : null,

                transaction.getLoan() != null
                        ? transaction.getLoan().getId()
                        : null,

                transaction.getLoan() != null
                        ? transaction.getLoan().getLoanNumber()
                        : null,

                transaction.getDeposit() != null
                        ? transaction.getDeposit().getId()
                        : null,

                transaction.getDeposit() != null
                        ? transaction.getDeposit().getDepositNumber()
                        : null,

                transaction.getCreatedAt()
        );
    }
}
