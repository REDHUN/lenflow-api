package com.redhun.lendflow_api.service;

import com.redhun.lendflow_api.dto.loan.CreateLoanRequest;
import com.redhun.lendflow_api.dto.loan.CreateRepaymentRequest;
import com.redhun.lendflow_api.dto.loan.LoanResponse;
import com.redhun.lendflow_api.dto.loan.RepaymentResponse;

import java.util.List;

public interface LoanService {

    LoanResponse createLoan(CreateLoanRequest request);

    LoanResponse getLoan(Long loanId);

    List<LoanResponse> getAllLoans();

    List<LoanResponse> getUserLoans(Long userId);

    LoanResponse accrueInterest(Long loanId);

    RepaymentResponse createRepayment(
            Long loanId,
            CreateRepaymentRequest request
    );

    List<RepaymentResponse> getLoanRepayments(Long loanId);

    List<RepaymentResponse> getUserRepayments(Long userId);
}