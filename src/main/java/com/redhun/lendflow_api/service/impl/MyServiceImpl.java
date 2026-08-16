package com.redhun.lendflow_api.service.impl;

import com.redhun.lendflow_api.dto.desposit.DepositResponse;
import com.redhun.lendflow_api.dto.loan.LoanResponse;
import com.redhun.lendflow_api.dto.loan.RepaymentResponse;
import com.redhun.lendflow_api.dto.user.UserResponse;
import com.redhun.lendflow_api.entity.User;
import com.redhun.lendflow_api.exception.ResourceNotFoundException;
import com.redhun.lendflow_api.repository.UserRepository;
import com.redhun.lendflow_api.service.DepositService;
import com.redhun.lendflow_api.service.LoanService;
import com.redhun.lendflow_api.service.MyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MyServiceImpl implements MyService {

    private final UserRepository userRepository;

    private final DepositService depositService;

    private final LoanService loanService;


    // =====================================================
    // MY PROFILE
    // =====================================================

    @Override
    @Transactional
    public UserResponse getMyProfile(
            String phone
    ) {

        User user =
                getUserByPhone(phone);

        return toUserResponse(user);
    }


    // =====================================================
    // MY DEPOSITS
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public List<DepositResponse> getMyDeposits(
            String phone
    ) {

        User user =
                getUserByPhone(phone);

        return depositService.getUserDeposits(
                user.getId()
        );
    }


    // =====================================================
    // MY LOANS
    // =====================================================

    @Override
    @Transactional
    public List<LoanResponse> getMyLoans(
            String phone
    ) {

        User user =
                getUserByPhone(phone);

        /*
         * LoanService will automatically calculate
         * daily interest up to today.
         */
        return loanService.getUserLoans(
                user.getId()
        );
    }


    // =====================================================
    // MY REPAYMENTS
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public List<RepaymentResponse> getMyRepayments(
            String phone
    ) {

        User user =
                getUserByPhone(phone);

        return loanService.getUserRepayments(
                user.getId()
        );
    }


    // =====================================================
    // FIND AUTHENTICATED USER
    // =====================================================

    private User getUserByPhone(
            String phone
    ) {

        return userRepository
                .findByPhone(phone)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Authenticated user not found"
                        )
                );
    }


    // =====================================================
    // BUILD USER RESPONSE
    // =====================================================

    private UserResponse toUserResponse(
            User user
    ) {

        // =================================================
        // LOANS
        // =================================================

        List<LoanResponse> loans =
                loanService.getUserLoans(
                        user.getId()
                );


        // =================================================
        // DEPOSITS
        // =================================================

        List<DepositResponse> deposits =
                depositService.getUserDeposits(
                        user.getId()
                );


        // =================================================
        // USER RESPONSE
        // =================================================

        return new UserResponse(

                user.getId(),

                user.getUserCode(),

                user.getName(),

                user.getPhone(),

                user.getEmail(),

                user.getRole(),

                user.getActive(),

                loans,

                deposits
        );
    }
}