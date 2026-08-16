package com.redhun.lendflow_api.service;


import com.redhun.lendflow_api.dto.desposit.AddDepositMoneyRequest;
import com.redhun.lendflow_api.dto.desposit.CreateDepositRequest;
import com.redhun.lendflow_api.dto.desposit.DepositResponse;

import java.util.List;

public interface DepositService {

    DepositResponse createDeposit(CreateDepositRequest request);

    DepositResponse getDeposit(Long id);
    List<DepositResponse>getAllDeposits();

    List<DepositResponse> getUserDeposits(Long userId);

    DepositResponse addMoney(
            Long depositId,
            AddDepositMoneyRequest request
    );

    DepositResponse closeDeposit(Long depositId);
}
