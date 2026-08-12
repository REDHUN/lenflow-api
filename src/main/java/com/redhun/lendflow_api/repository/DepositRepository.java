package com.redhun.lendflow_api.repository;
import com.redhun.lendflow_api.entity.Deposit;
import com.redhun.lendflow_api.enums.DepositStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DepositRepository extends JpaRepository<Deposit, Long> {

    List<Deposit> findByUserId(Long userId);

    List<Deposit> findByStatus(DepositStatus status);

    List<Deposit> findByUserIdAndStatus(
            Long userId,
            DepositStatus status
    );
    boolean existsByUserIdAndStatus(
            Long userId,
            DepositStatus status
    );
    long countByStatus(DepositStatus status);
}