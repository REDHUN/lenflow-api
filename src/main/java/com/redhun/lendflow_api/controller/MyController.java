package com.redhun.lendflow_api.controller;

import com.redhun.lendflow_api.dto.desposit.DepositResponse;
import com.redhun.lendflow_api.dto.loan.LoanResponse;
import com.redhun.lendflow_api.dto.loan.RepaymentResponse;
import com.redhun.lendflow_api.dto.user.UserResponse;
import com.redhun.lendflow_api.service.MyService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/my")
@RequiredArgsConstructor
public class MyController {

    private final MyService myService;


    // =====================================================
    // MY PROFILE
    // =====================================================

    @GetMapping("/profile")
    public UserResponse getMyProfile(
            Authentication authentication
    ) {

        return myService.getMyProfile(
                authentication.getName()
        );
    }


    // =====================================================
    // MY DEPOSITS
    // =====================================================

    @GetMapping("/deposits")
    public List<DepositResponse> getMyDeposits(
            Authentication authentication
    ) {

        return myService.getMyDeposits(
                authentication.getName()
        );
    }


    // =====================================================
    // MY LOANS
    // =====================================================

    @GetMapping("/loans")
    public List<LoanResponse> getMyLoans(
            Authentication authentication
    ) {

        return myService.getMyLoans(
                authentication.getName()
        );
    }


    // =====================================================
    // MY REPAYMENTS
    // =====================================================

    @GetMapping("/repayments")
    public List<RepaymentResponse> getMyRepayments(
            Authentication authentication
    ) {

        return myService.getMyRepayments(
                authentication.getName()
        );
    }
}