package com.redhun.lendflow_api.repository;

import com.redhun.lendflow_api.entity.DepositTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DepositTransactionRepository
        extends JpaRepository<DepositTransaction, Long> {

    List<DepositTransaction> findByDepositId(Long depositId);

    List<DepositTransaction> findByDepositUserId(Long userId);
}