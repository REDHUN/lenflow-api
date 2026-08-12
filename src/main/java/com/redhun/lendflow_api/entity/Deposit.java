package com.redhun.lendflow_api.entity;
import com.redhun.lendflow_api.enums.DepositStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "deposits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Deposit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String depositNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal interestRate;

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate closedDate;

    @Column(precision = 15, scale = 2)
    private BigDecimal interestAmount;

    @Column(precision = 15, scale = 2)
    private BigDecimal closingAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DepositStatus status;

    private LocalDateTime createdAt;
}
