package com.redhun.lendflow_api.service;
import com.redhun.lendflow_api.dto.desposit.DepositResponse;
import com.redhun.lendflow_api.dto.loan.LoanResponse;
import com.redhun.lendflow_api.dto.loan.RepaymentResponse;
import com.redhun.lendflow_api.dto.user.UserResponse;

import java.util.List;

public interface MyService {

    UserResponse getMyProfile(
            String phone
    );

    List<DepositResponse> getMyDeposits(
            String phone
    );

    List<LoanResponse> getMyLoans(
            String phone
    );

    List<RepaymentResponse> getMyRepayments(
            String phone
    );
}
