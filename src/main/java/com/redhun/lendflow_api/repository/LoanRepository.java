package com.redhun.lendflow_api.repository;

import com.redhun.lendflow_api.entity.Loan;
import com.redhun.lendflow_api.enums.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    List<Loan> findByUserId(Long userId);

    List<Loan> findByStatus(LoanStatus status);

    List<Loan> findByUserIdAndStatus(
            Long userId,
            LoanStatus status
    );
    boolean existsByUserIdAndStatusIn(
            Long userId,
            List<LoanStatus> statuses
    );

    long countByStatus(LoanStatus status);
}