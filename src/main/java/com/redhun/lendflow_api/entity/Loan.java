package com.redhun.lendflow_api.entity;

import com.redhun.lendflow_api.enums.LoanStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "loans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // =====================================================
    // LOAN NUMBER
    // =====================================================

    @Column(
            nullable = false,
            unique = true
    )
    private String loanNumber;


    // =====================================================
    // BORROWER
    // =====================================================

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private User user;


    // =====================================================
    // ORIGINAL PRINCIPAL
    // =====================================================

    @Column(
            nullable = false,
            precision = 15,
            scale = 2
    )
    private BigDecimal principalAmount;


    // =====================================================
    // OUTSTANDING PRINCIPAL
    // =====================================================

    @Column(
            nullable = false,
            precision = 15,
            scale = 2
    )
    private BigDecimal outstandingPrincipal;


    // =====================================================
    // MONTHLY INTEREST RATE
    //
    // Example:
    // 5.00 = 5% per month
    // =====================================================

    @Column(
            nullable = false,
            precision = 5,
            scale = 2
    )
    private BigDecimal interestRate;


    // =====================================================
    // CURRENT OUTSTANDING INTEREST
    //
    // Interest generated but not yet paid.
    // =====================================================

    @Column(
            nullable = false,
            precision = 15,
            scale = 2
    )
    private BigDecimal accruedInterest;


    // =====================================================
    // TOTAL INTEREST ACCRUED
    //
    // Historical total interest generated during
    // the lifetime of the loan.
    //
    // Example:
    //
    // Month 1 = ₹50
    // Month 2 = ₹50
    // Month 3 = ₹25
    //
    // totalInterestAccrued = ₹125
    // =====================================================

    @Column(
            nullable = false,
            precision = 15,
            scale = 2
    )
    private BigDecimal totalInterestAccrued;


    // =====================================================
    // TOTAL INTEREST PAID
    //
    // Historical interest actually received.
    // =====================================================

    @Column(
            nullable = false,
            precision = 15,
            scale = 2
    )
    private BigDecimal totalInterestPaid;


    // =====================================================
    // TOTAL PRINCIPAL PAID
    //
    // Historical principal actually received.
    // =====================================================

    @Column(
            nullable = false,
            precision = 15,
            scale = 2
    )
    private BigDecimal totalPrincipalPaid;


    // =====================================================
    // TOTAL PAYABLE
    //
    // Current outstanding balance:
    //
    // outstandingPrincipal
    // +
    // accruedInterest
    // =====================================================

    @Column(
            nullable = false,
            precision = 15,
            scale = 2
    )
    private BigDecimal totalPayable;


    // =====================================================
    // START DATE
    // =====================================================

    @Column(
            nullable = false
    )
    private LocalDate startDate;


    // =====================================================
    // LAST INTEREST ACCRUED DATE
    //
    // Used to determine when the next monthly interest
    // calculation is allowed.
    // =====================================================

    private LocalDate lastInterestAccruedDate;


    // =====================================================
    // STATUS
    // =====================================================

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false
    )
    private LoanStatus status;


    // =====================================================
    // AUDIT
    // =====================================================

    private LocalDateTime createdAt;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;


    // =====================================================
    // PRE PERSIST
    // =====================================================

    @PrePersist
    protected void onCreate() {

        createdAt = LocalDateTime.now();
    }
}