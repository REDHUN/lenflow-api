package com.redhun.lendflow_api.service.impl;

import com.redhun.lendflow_api.dto.loan.CreateLoanRequest;
import com.redhun.lendflow_api.dto.loan.CreateRepaymentRequest;
import com.redhun.lendflow_api.dto.loan.LoanResponse;
import com.redhun.lendflow_api.dto.loan.RepaymentResponse;
import com.redhun.lendflow_api.entity.FinancialTransaction;
import com.redhun.lendflow_api.entity.Loan;
import com.redhun.lendflow_api.entity.LoanRepayment;
import com.redhun.lendflow_api.entity.User;
import com.redhun.lendflow_api.enums.LoanStatus;
import com.redhun.lendflow_api.enums.TransactionType;
import com.redhun.lendflow_api.exception.BusinessException;
import com.redhun.lendflow_api.exception.ResourceNotFoundException;
import com.redhun.lendflow_api.repository.FinancialTransactionRepository;
import com.redhun.lendflow_api.repository.LoanRepaymentRepository;
import com.redhun.lendflow_api.repository.LoanRepository;
import com.redhun.lendflow_api.repository.UserRepository;
import com.redhun.lendflow_api.service.FinancialTransactionService;
import com.redhun.lendflow_api.service.LoanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;

    private final LoanRepaymentRepository repaymentRepository;

    private final UserRepository userRepository;

    private final FinancialTransactionRepository
            financialTransactionRepository;
    private final FinancialTransactionService financialTransactionService;


    // =========================================================
    // CREATE LOAN
    // =========================================================

    @Override
    public LoanResponse createLoan(
            CreateLoanRequest request
    ) {

        // =====================================================
        // FIND USER
        // =====================================================

        User user =
                userRepository.findById(
                        request.userId()
                ).orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with id: "
                                        + request.userId()
                        )
                );


        // =====================================================
        // PRINCIPAL
        // =====================================================

        BigDecimal principal =
                request.principalAmount()
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );


        if (principal.compareTo(
                BigDecimal.ZERO
        ) <= 0) {

            throw new BusinessException(
                    "Loan principal must be greater than zero"
            );
        }


        // =====================================================
        // CHECK AVAILABLE BALANCE
        // =====================================================

        BigDecimal availableBalance =
                financialTransactionService
                        .getAvailableBalance();


        if (availableBalance.compareTo(
                principal
        ) < 0) {

            throw new BusinessException(
                    "Insufficient available balance. "
                            + "Available balance: ₹"
                            + availableBalance
                            + ", Requested loan: ₹"
                            + principal
            );
        }


        // =====================================================
        // VALIDATE INTEREST RATE
        // =====================================================

        if (request.interestRate()
                .compareTo(BigDecimal.ZERO) < 0) {

            throw new BusinessException(
                    "Interest rate cannot be negative"
            );
        }


        BigDecimal interestRate =
                request.interestRate()
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );


        // =====================================================
        // CREATE LOAN
        // =====================================================

        Loan loan =
                Loan.builder()

                        .loanNumber(
                                generateLoanNumber()
                        )

                        .user(user)

                        // Original principal
                        .principalAmount(
                                principal
                        )

                        // Current principal outstanding
                        .outstandingPrincipal(
                                principal
                        )

                        // Monthly interest rate
                        .interestRate(
                                interestRate
                        )

                        // Current unpaid interest
                        .accruedInterest(
                                BigDecimal.ZERO
                        )

                        // Historical interest generated
                        .totalInterestAccrued(
                                BigDecimal.ZERO
                        )

                        // Historical interest received
                        .totalInterestPaid(
                                BigDecimal.ZERO
                        )

                        // Historical principal received
                        .totalPrincipalPaid(
                                BigDecimal.ZERO
                        )

                        // Current total outstanding
                        .totalPayable(
                                principal
                        )

                        .startDate(
                                request.startDate()
                        )

                        /*
                         * Interest calculation starts from
                         * the loan start date.
                         */
                        .lastInterestAccruedDate(
                                request.startDate()
                        )

                        .status(
                                LoanStatus.ACTIVE
                        )

                        .build();


        // =====================================================
        // SAVE LOAN
        // =====================================================

        Loan savedLoan =
                loanRepository.save(loan);


        // =====================================================
        // CREATE LOAN DISBURSEMENT TRANSACTION
        //
        // This is MONEY OUT.
        //
        // Example:
        //
        // Available balance = ₹1,000
        // Loan              = ₹1,000
        //
        // Available balance becomes ₹0
        // =====================================================

        FinancialTransaction transaction =
                FinancialTransaction.builder()

                        .transactionNumber(
                                generateTransactionNumber()
                        )

                        .type(
                                TransactionType.LOAN_DISBURSEMENT
                        )

                        .amount(
                                principal
                        )

                        .transactionDate(
                                request.startDate()
                        )

                        .description(
                                "Loan disbursement - "
                                        + savedLoan.getLoanNumber()
                        )

                        .user(
                                user
                        )

                        .loan(
                                savedLoan
                        )

                        .createdAt(
                                LocalDateTime.now()
                        )

                        .build();


        financialTransactionRepository.save(
                transaction
        );


        // =====================================================
        // RESPONSE
        // =====================================================

        return buildLoanResponse(
                savedLoan
        );
    }


    // =========================================================
    // GET LOAN
    // =========================================================

    @Override
    public LoanResponse getLoan(
            Long loanId
    ) {

        Loan loan =
                loanRepository.findById(
                        loanId
                ).orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Loan not found with id: "
                                        + loanId
                        )
                );

        /*
         * Automatically calculate all
         * interest up to today.
         */
        accrueInterestUpTo(
                loan,
                LocalDate.now()
        );

        return buildLoanResponse(
                loan
        );
    }


    // =========================================================
    // GET ALL LOANS
    // =========================================================

    @Override
    public List<LoanResponse> getAllLoans() {

        return loanRepository.findAll()
                .stream()
                .map(loan -> {

                    accrueInterestUpTo(
                            loan,
                            LocalDate.now()
                    );

                    return buildLoanResponse(
                            loan
                    );
                })
                .toList();
    }


    // =========================================================
    // GET USER LOANS
    // =========================================================

    @Override
    public List<LoanResponse> getUserLoans(
            Long userId
    ) {

        if (!userRepository.existsById(userId)) {

            throw new ResourceNotFoundException(
                    "User not found with id: "
                            + userId
            );
        }

        return loanRepository
                .findByUserId(userId)
                .stream()
                .map(loan -> {

                    accrueInterestUpTo(
                            loan,
                            LocalDate.now()
                    );

                    return buildLoanResponse(
                            loan
                    );
                })
                .toList();
    }


    // =========================================================
    // ACCRUE MONTHLY INTEREST
    // =========================================================

    @Override
    public LoanResponse accrueInterest(Long loanId) {

        Loan loan =
                loanRepository.findById(loanId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Loan not found with id: "
                                                + loanId
                                )
                        );

        if (loan.getStatus() == LoanStatus.CLOSED) {
            throw new BusinessException(
                    "Cannot accrue interest on a closed loan"
            );
        }

        accrueInterestUpTo(
                loan,
                LocalDate.now()
        );

        return buildLoanResponse(loan);
    }


    // =========================================================
    // CREATE REPAYMENT
    // =========================================================

    @Override
    public RepaymentResponse createRepayment(
            Long loanId,
            CreateRepaymentRequest request
    ) {

        Loan loan =
                loanRepository.findById(
                        loanId
                ).orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Loan not found with id: "
                                        + loanId
                        )
                );


        // =====================================================
        // CLOSED LOAN
        // =====================================================

        if (loan.getStatus()
                == LoanStatus.CLOSED) {

            throw new BusinessException(
                    "Cannot make repayment for a closed loan"
            );
        }


        // =====================================================
        // AUTOMATIC INTEREST ACCRUAL
        //
        // Calculate interest up to repayment date
        // BEFORE processing repayment.
        // =====================================================

        accrueInterestUpTo(
                loan,
                request.repaymentDate()
        );


        // =====================================================
        // CURRENT OUTSTANDING
        // =====================================================

        BigDecimal outstandingInterest =
                loan.getAccruedInterest() == null
                        ? BigDecimal.ZERO
                        : loan.getAccruedInterest();


        BigDecimal outstandingPrincipal =
                loan.getOutstandingPrincipal() == null
                        ? BigDecimal.ZERO
                        : loan.getOutstandingPrincipal();


        // =====================================================
        // PAYMENT VALUES
        // =====================================================

        BigDecimal interestPayment =
                request.interestAmount()
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );


        BigDecimal principalPayment =
                request.principalAmount()
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );


        // =====================================================
        // VALIDATE NEGATIVE VALUES
        // =====================================================

        if (interestPayment.compareTo(
                BigDecimal.ZERO
        ) < 0) {

            throw new BusinessException(
                    "Interest payment cannot be negative"
            );
        }


        if (principalPayment.compareTo(
                BigDecimal.ZERO
        ) < 0) {

            throw new BusinessException(
                    "Principal payment cannot be negative"
            );
        }


        // =====================================================
        // TOTAL PAYMENT
        // =====================================================

        BigDecimal totalPayment =
                interestPayment
                        .add(principalPayment)
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );


        if (totalPayment.compareTo(
                BigDecimal.ZERO
        ) <= 0) {

            throw new BusinessException(
                    "Repayment amount must be greater than zero"
            );
        }


        // =====================================================
        // VALIDATE INTEREST
        // =====================================================

        if (interestPayment.compareTo(
                outstandingInterest
        ) > 0) {

            throw new BusinessException(
                    "Interest payment exceeds outstanding "
                            + "interest. Outstanding: ₹"
                            + outstandingInterest
            );
        }


        // =====================================================
        // VALIDATE PRINCIPAL
        // =====================================================

        if (principalPayment.compareTo(
                outstandingPrincipal
        ) > 0) {

            throw new BusinessException(
                    "Principal payment exceeds outstanding "
                            + "principal. Outstanding: ₹"
                            + outstandingPrincipal
            );
        }


        // =====================================================
        // CREATE REPAYMENT
        // =====================================================

        LoanRepayment repayment =
                LoanRepayment.builder()
                        .loan(loan)

                        .amount(
                                totalPayment
                        )

                        .interestAmount(
                                interestPayment
                        )

                        .principalAmount(
                                principalPayment
                        )

                        .paymentMethod(
                                request.paymentMethod()
                        )

                        .repaymentDate(
                                request.repaymentDate()
                        )

                        .notes(
                                request.notes()
                        )

                        .build();


        LoanRepayment savedRepayment =
                repaymentRepository.save(
                        repayment
                );


        // =====================================================
        // NEW INTEREST BALANCE
        // =====================================================

        BigDecimal newInterestOutstanding =
                outstandingInterest
                        .subtract(
                                interestPayment
                        )
                        .max(BigDecimal.ZERO)
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );


        // =====================================================
        // NEW PRINCIPAL BALANCE
        // =====================================================

        BigDecimal newPrincipalOutstanding =
                outstandingPrincipal
                        .subtract(
                                principalPayment
                        )
                        .max(BigDecimal.ZERO)
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );


        // =====================================================
        // TOTAL INTEREST PAID
        // =====================================================

        BigDecimal totalInterestPaid =
                loan.getTotalInterestPaid() == null
                        ? BigDecimal.ZERO
                        : loan.getTotalInterestPaid();


        loan.setTotalInterestPaid(
                totalInterestPaid
                        .add(interestPayment)
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        )
        );


        // =====================================================
        // TOTAL PRINCIPAL PAID
        // =====================================================

        BigDecimal totalPrincipalPaid =
                loan.getTotalPrincipalPaid() == null
                        ? BigDecimal.ZERO
                        : loan.getTotalPrincipalPaid();


        loan.setTotalPrincipalPaid(
                totalPrincipalPaid
                        .add(principalPayment)
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        )
        );


        // =====================================================
        // UPDATE CURRENT BALANCES
        // =====================================================

        loan.setAccruedInterest(
                newInterestOutstanding
        );


        loan.setOutstandingPrincipal(
                newPrincipalOutstanding
        );


        // =====================================================
        // TOTAL OUTSTANDING
        // =====================================================

        BigDecimal newTotalOutstanding =
                newPrincipalOutstanding
                        .add(
                                newInterestOutstanding
                        )
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );


        loan.setTotalPayable(
                newTotalOutstanding
        );


        // =====================================================
        // LOAN STATUS
        // =====================================================

        if (
                newPrincipalOutstanding.compareTo(
                        BigDecimal.ZERO
                ) == 0
                        &&
                        newInterestOutstanding.compareTo(
                                BigDecimal.ZERO
                        ) == 0
        ) {

            loan.setStatus(
                    LoanStatus.CLOSED
            );

        } else {

            loan.setStatus(
                    LoanStatus.PARTIALLY_PAID
            );
        }


        // =====================================================
        // FINANCIAL TRANSACTION
        // =====================================================

        FinancialTransaction transaction =
                FinancialTransaction.builder()
                        .transactionNumber(
                                generateTransactionNumber()
                        )

                        .type(
                                TransactionType.LOAN_REPAYMENT
                        )

                        .amount(
                                totalPayment
                        )

                        .transactionDate(
                                request.repaymentDate()
                        )

                        .description(
                                "Loan repayment - "
                                        + loan.getLoanNumber()
                        )

                        .user(
                                loan.getUser()
                        )

                        .loan(
                                loan
                        )

                        .createdAt(
                                LocalDateTime.now()
                        )

                        .build();


        financialTransactionRepository.save(
                transaction
        );


        return buildRepaymentResponse(
                savedRepayment
        );
    }


    // =========================================================
    // GET LOAN REPAYMENTS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<RepaymentResponse> getLoanRepayments(
            Long loanId
    ) {

        if (!loanRepository.existsById(
                loanId
        )) {

            throw new ResourceNotFoundException(
                    "Loan not found with id: "
                            + loanId
            );
        }


        return repaymentRepository
                .findByLoanId(loanId)
                .stream()
                .map(this::buildRepaymentResponse)
                .toList();
    }


    // =========================================================
    // GET USER REPAYMENTS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<RepaymentResponse> getUserRepayments(
            Long userId
    ) {

        if (!userRepository.existsById(
                userId
        )) {

            throw new ResourceNotFoundException(
                    "User not found with id: "
                            + userId
            );
        }


        return repaymentRepository
                .findByLoanUserId(userId)
                .stream()
                .map(this::buildRepaymentResponse)
                .toList();
    }


    // =========================================================
    // CALCULATE TOTAL PAID
    // =========================================================

    private BigDecimal calculateTotalPaid(
            Long loanId
    ) {

        return repaymentRepository
                .findByLoanId(loanId)
                .stream()
                .map(LoanRepayment::getAmount)
                .filter(amount ->
                        amount != null
                )
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                )
                .setScale(
                        2,
                        RoundingMode.HALF_UP
                );
    }


    // =========================================================
    // GET OUTSTANDING PRINCIPAL
    // =========================================================

    private BigDecimal getOutstandingPrincipal(
            Loan loan
    ) {

        if (loan.getOutstandingPrincipal()
                != null) {

            return loan.getOutstandingPrincipal()
                    .setScale(
                            2,
                            RoundingMode.HALF_UP
                    );
        }


        // -----------------------------------------------------
        // Fallback for old database records
        // -----------------------------------------------------

        BigDecimal principalPaid =
                repaymentRepository
                        .findByLoanId(
                                loan.getId()
                        )
                        .stream()
                        .map(
                                LoanRepayment::getPrincipalAmount
                        )
                        .filter(
                                amount -> amount != null
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );


        return loan.getPrincipalAmount()
                .subtract(principalPaid)
                .max(BigDecimal.ZERO)
                .setScale(
                        2,
                        RoundingMode.HALF_UP
                );
    }


    // =========================================================
    // GET CURRENT ACCRUED INTEREST
    // =========================================================

    private BigDecimal getAccruedInterest(
            Loan loan
    ) {

        BigDecimal accruedInterest =
                loan.getAccruedInterest();

        if (accruedInterest == null) {
            return BigDecimal.ZERO;
        }

        return accruedInterest
                .max(BigDecimal.ZERO)
                .setScale(
                        2,
                        RoundingMode.HALF_UP
                );
    }


    // =========================================================
    // CALCULATE MONTHLY INTEREST
    // =========================================================

    private BigDecimal calculateMonthlyInterest(
            Loan loan
    ) {

        BigDecimal principal =
                getOutstandingPrincipal(
                        loan
                );


        if (principal.compareTo(
                BigDecimal.ZERO
        ) <= 0) {

            return BigDecimal.ZERO;
        }


        BigDecimal rate =
                loan.getInterestRate();


        if (rate == null ||
                rate.compareTo(
                        BigDecimal.ZERO
                ) <= 0) {

            return BigDecimal.ZERO;
        }


        return principal
                .multiply(rate)
                .divide(
                        BigDecimal.valueOf(100),
                        2,
                        RoundingMode.HALF_UP
                );
    }


    // =========================================================
    // BUILD LOAN RESPONSE
    // =========================================================

    private LoanResponse buildLoanResponse(
            Loan loan
    ) {

        BigDecimal totalPaid =
                calculateTotalPaid(
                        loan.getId()
                );


        BigDecimal outstandingPrincipal =
                getOutstandingPrincipal(
                        loan
                );


        BigDecimal outstandingInterest =
                getAccruedInterest(
                        loan
                );


        BigDecimal outstandingAmount =
                outstandingPrincipal
                        .add(
                                outstandingInterest
                        )
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );


        return new LoanResponse(

                loan.getId(),

                loan.getLoanNumber(),

                loan.getUser().getId(),

                loan.getUser().getName(),

                // Original principal
                loan.getPrincipalAmount(),

                // Monthly interest rate
                loan.getInterestRate(),

                // Current outstanding interest
                outstandingInterest,

                // Current total outstanding
                outstandingAmount,

                // Total repayments
                totalPaid,

                // Principal outstanding
                outstandingPrincipal,

                // Interest outstanding
                outstandingInterest,

                // Principal + interest
                outstandingAmount,

                // Start date
                loan.getStartDate(),

                // Last interest calculation date
                loan.getLastInterestAccruedDate(),

                // Status
                loan.getStatus()
        );
    }


    // =========================================================
    // BUILD REPAYMENT RESPONSE
    // =========================================================

    private RepaymentResponse buildRepaymentResponse(
            LoanRepayment repayment
    ) {

        Loan loan =
                repayment.getLoan();


        User user =
                loan.getUser();


        return new RepaymentResponse(

                repayment.getId(),

                loan.getId(),

                loan.getLoanNumber(),

                user.getId(),

                user.getName(),

                repayment.getAmount(),

                repayment.getPrincipalAmount(),

                repayment.getInterestAmount(),

                repayment.getPaymentMethod(),

                repayment.getRepaymentDate(),

                repayment.getNotes()
        );
    }


    // =========================================================
    // GENERATE LOAN NUMBER
    // =========================================================

    private String generateLoanNumber() {

        long count =
                loanRepository.count() + 1;


        return String.format(
                "LN%06d",
                count
        );
    }


    // =========================================================
    // GENERATE TRANSACTION NUMBER
    // =========================================================

    private String generateTransactionNumber() {

        long count =
                financialTransactionRepository.count() + 1;


        return String.format(
                "TXN%08d",
                count
        );
    }
    private void accrueInterestUpTo(
            Loan loan,
            LocalDate calculationDate
    ) {

        if (loan.getStatus() == LoanStatus.CLOSED) {
            return;
        }

        BigDecimal outstandingPrincipal =
                loan.getOutstandingPrincipal();

        if (outstandingPrincipal == null ||
                outstandingPrincipal.compareTo(
                        BigDecimal.ZERO
                ) <= 0) {

            return;
        }

        LocalDate lastAccruedDate =
                loan.getLastInterestAccruedDate();

        if (lastAccruedDate == null) {
            lastAccruedDate =
                    loan.getStartDate();
        }

        /*
         * Don't calculate backwards.
         */
        if (calculationDate.isBefore(
                lastAccruedDate
        )) {

            throw new BusinessException(
                    "Calculation date cannot be before "
                            + "last interest accrued date"
            );
        }

        /*
         * Number of days since last calculation.
         */
        long days =
                ChronoUnit.DAYS.between(
                        lastAccruedDate,
                        calculationDate
                );

        /*
         * Nothing to calculate.
         */
        if (days <= 0) {
            return;
        }

        BigDecimal interestRate =
                loan.getInterestRate();

        if (interestRate == null ||
                interestRate.compareTo(
                        BigDecimal.ZERO
                ) <= 0) {

            loan.setLastInterestAccruedDate(
                    calculationDate
            );

            return;
        }

        /*
         * Daily interest:
         *
         * Principal × Monthly Rate ÷ 100 ÷ 30
         *
         * Example:
         *
         * ₹1,000 × 5 ÷ 100 ÷ 30
         * = ₹1.666666...
         */
        BigDecimal dailyInterest =
                outstandingPrincipal
                        .multiply(interestRate)
                        .divide(
                                BigDecimal.valueOf(100),
                                10,
                                RoundingMode.HALF_UP
                        )
                        .divide(
                                BigDecimal.valueOf(30),
                                10,
                                RoundingMode.HALF_UP
                        );

        /*
         * Interest for elapsed days.
         */
        BigDecimal interestForPeriod =
                dailyInterest
                        .multiply(
                                BigDecimal.valueOf(days)
                        )
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );

        /*
         * Current unpaid interest.
         */
        BigDecimal currentAccruedInterest =
                loan.getAccruedInterest() == null
                        ? BigDecimal.ZERO
                        : loan.getAccruedInterest();


        /*
         * Historical interest generated.
         */
        BigDecimal currentTotalInterestAccrued =
                loan.getTotalInterestAccrued() == null
                        ? BigDecimal.ZERO
                        : loan.getTotalInterestAccrued();


        /*
         * Update current unpaid interest.
         */
        BigDecimal newAccruedInterest =
                currentAccruedInterest
                        .add(interestForPeriod)
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );


        /*
         * Update lifetime interest generated.
         */
        BigDecimal newTotalInterestAccrued =
                currentTotalInterestAccrued
                        .add(interestForPeriod)
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );


        loan.setAccruedInterest(
                newAccruedInterest
        );

        loan.setTotalInterestAccrued(
                newTotalInterestAccrued
        );


        /*
         * Current total outstanding.
         */
        loan.setTotalPayable(
                outstandingPrincipal
                        .add(newAccruedInterest)
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        )
        );


        /*
         * Move the calculation date forward.
         */
        loan.setLastInterestAccruedDate(
                calculationDate
        );
    }
}