package com.redhun.lendflow_api.repository;

import com.redhun.lendflow_api.entity.LoanRepayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanRepaymentRepository
        extends JpaRepository<LoanRepayment, Long> {

    List<LoanRepayment> findByLoanId(Long loanId);

    List<LoanRepayment> findByLoanUserId(Long userId);
}