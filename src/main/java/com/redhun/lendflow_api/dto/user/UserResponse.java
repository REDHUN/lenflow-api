package com.redhun.lendflow_api.dto.user;

import com.redhun.lendflow_api.dto.desposit.DepositResponse;
import com.redhun.lendflow_api.dto.loan.LoanResponse;
import com.redhun.lendflow_api.enums.Role;

import java.util.List;

public record UserResponse(

        Long id,

        String userCode,

        String name,

        String phone,

        String email,

        Role role,

        Boolean active,

        List<LoanResponse> loans,

        List<DepositResponse> deposits

) {
}