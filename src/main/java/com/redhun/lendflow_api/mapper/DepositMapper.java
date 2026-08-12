package com.redhun.lendflow_api.mapper;
import com.redhun.lendflow_api.dto.desposit.CreateDepositRequest;
import com.redhun.lendflow_api.dto.desposit.DepositResponse;
import com.redhun.lendflow_api.entity.Deposit;
import com.redhun.lendflow_api.entity.User;
import com.redhun.lendflow_api.enums.DepositStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DepositMapper {

    public Deposit toEntity(
            CreateDepositRequest request,
            User user,
            String depositNumber
    ) {

        return Deposit.builder()
                .depositNumber(depositNumber)
                .user(user)
                .interestRate(request.interestRate())
                .startDate(request.startDate())
                .status(DepositStatus.ACTIVE)
                .build();
    }

    public DepositResponse toResponse(
            Deposit deposit,
            BigDecimal totalAmount
    ) {

        return new DepositResponse(
                deposit.getId(),
                deposit.getDepositNumber(),
                deposit.getUser().getId(),
                deposit.getUser().getName(),
                totalAmount,
                deposit.getInterestRate(),
                deposit.getInterestAmount(),
                deposit.getClosingAmount(),
                deposit.getStartDate(),
                deposit.getClosedDate(),
                deposit.getStatus()
        );
    }
}