package com.redhun.lendflow_api.mapper;

import com.redhun.lendflow_api.dto.desposit.AddDepositMoneyRequest;
import com.redhun.lendflow_api.entity.Deposit;
import com.redhun.lendflow_api.entity.DepositTransaction;
import com.redhun.lendflow_api.dto.desposit.CreateDepositRequest;
import com.redhun.lendflow_api.enums.PaymentMethod;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DepositTransactionMapper {

    public DepositTransaction toInitialTransaction(
            CreateDepositRequest request,
            Deposit deposit
    ) {

        return DepositTransaction.builder()
                .deposit(deposit)
                .amount(request.initialAmount())
                .transactionDate(request.startDate())
                .paymentMethod(PaymentMethod.CASH)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public DepositTransaction toEntity(
            AddDepositMoneyRequest request,
            Deposit deposit
    ) {

        return DepositTransaction.builder()
                .deposit(deposit)
                .amount(request.amount())
                .paymentMethod(request.paymentMethod())
                .transactionDate(request.transactionDate())
                .notes(request.notes())
                .createdAt(LocalDateTime.now())
                .build();
    }
}
