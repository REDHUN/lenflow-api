package com.redhun.lendflow_api.controller;

import com.redhun.lendflow_api.dto.loan.CreateLoanRequest;
import com.redhun.lendflow_api.dto.loan.CreateRepaymentRequest;
import com.redhun.lendflow_api.dto.loan.LoanResponse;
import com.redhun.lendflow_api.dto.loan.RepaymentResponse;
import com.redhun.lendflow_api.service.LoanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;


    // =========================================================
    // CREATE LOAN
    // =========================================================

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LoanResponse createLoan(
            @Valid @RequestBody CreateLoanRequest request
    ) {

        return loanService.createLoan(request);
    }


    // =========================================================
    // GET ALL LOANS
    // =========================================================

    @PreAuthorize("hasAnyRole('ADMIN', 'MEMBER')")
    @GetMapping
    public List<LoanResponse> getAllLoans() {

        return loanService.getAllLoans();
    }


    // =========================================================
    // GET LOAN BY ID
    // =========================================================

    @PreAuthorize("hasAnyRole('ADMIN', 'MEMBER')")
    @GetMapping("/{loanId}")
    public LoanResponse getLoan(
            @PathVariable Long loanId
    ) {

        return loanService.getLoan(loanId);
    }


    // =========================================================
    // GET USER LOANS
    // =========================================================

    @PreAuthorize("hasAnyRole('ADMIN', 'MEMBER')")
    @GetMapping("/user/{userId}")
    public List<LoanResponse> getUserLoans(
            @PathVariable Long userId
    ) {

        return loanService.getUserLoans(userId);
    }


    // =========================================================
    // ACCRUE MONTHLY INTEREST
    // =========================================================




    // =========================================================
    // CREATE REPAYMENT
    // =========================================================

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{loanId}/repayments")
    @ResponseStatus(HttpStatus.CREATED)
    public RepaymentResponse createRepayment(
            @PathVariable Long loanId,
            @Valid @RequestBody CreateRepaymentRequest request
    ) {

        return loanService.createRepayment(
                loanId,
                request
        );
    }


    // =========================================================
    // GET LOAN REPAYMENTS
    // =========================================================

    @PreAuthorize("hasAnyRole('ADMIN', 'MEMBER')")
    @GetMapping("/{loanId}/repayments")
    public List<RepaymentResponse> getLoanRepayments(
            @PathVariable Long loanId
    ) {

        return loanService.getLoanRepayments(
                loanId
        );
    }


    // =========================================================
    // GET USER REPAYMENTS
    // =========================================================

    @PreAuthorize("hasAnyRole('ADMIN', 'MEMBER')")
    @GetMapping("/user/{userId}/repayments")
    public List<RepaymentResponse> getUserRepayments(
            @PathVariable Long userId
    ) {

        return loanService.getUserRepayments(
                userId
        );
    }
}