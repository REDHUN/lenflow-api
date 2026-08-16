package com.redhun.lendflow_api.service.impl;

import com.redhun.lendflow_api.dto.reports.*;
import com.redhun.lendflow_api.entity.Deposit;
import com.redhun.lendflow_api.entity.DepositTransaction;
import com.redhun.lendflow_api.entity.FinancialTransaction;
import com.redhun.lendflow_api.entity.Loan;
import com.redhun.lendflow_api.entity.LoanRepayment;
import com.redhun.lendflow_api.entity.User;
import com.redhun.lendflow_api.enums.DepositStatus;
import com.redhun.lendflow_api.enums.LoanStatus;
import com.redhun.lendflow_api.repository.DepositRepository;
import com.redhun.lendflow_api.repository.DepositTransactionRepository;
import com.redhun.lendflow_api.repository.FinancialTransactionRepository;
import com.redhun.lendflow_api.repository.LoanRepaymentRepository;
import com.redhun.lendflow_api.repository.LoanRepository;
import com.redhun.lendflow_api.repository.UserRepository;
import com.redhun.lendflow_api.service.FinancialTransactionService;
import com.redhun.lendflow_api.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

    private final LoanRepository loanRepository;

    private final LoanRepaymentRepository repaymentRepository;

    private final DepositRepository depositRepository;

    private final DepositTransactionRepository
            depositTransactionRepository;

    private final FinancialTransactionRepository
            financialTransactionRepository;

    private final UserRepository userRepository;

    private final FinancialTransactionService
            financialTransactionService;


    // =========================================================
    // LOAN REPORT
    // =========================================================

    @Override
    public List<LoanReportResponse> getLoanReport() {

        return loanRepository.findAll()
                .stream()
                .map(this::buildLoanReport)
                .toList();
    }


    // =========================================================
    // BUILD LOAN REPORT
    // =========================================================

    private LoanReportResponse buildLoanReport(
            Loan loan
    ) {

        // -----------------------------------------------------
        // Repayments
        // -----------------------------------------------------

        List<LoanRepayment> repayments =
                repaymentRepository.findByLoanId(
                        loan.getId()
                );


        // -----------------------------------------------------
        // Total paid
        // -----------------------------------------------------

        BigDecimal totalPaid =
                repayments.stream()
                        .map(LoanRepayment::getAmount)
                        .filter(value -> value != null)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );


        // -----------------------------------------------------
        // Interest paid
        // -----------------------------------------------------

        BigDecimal interestPaid =
                repayments.stream()
                        .map(
                                LoanRepayment::getInterestAmount
                        )
                        .filter(value -> value != null)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );


        // -----------------------------------------------------
        // Principal paid
        // -----------------------------------------------------

        BigDecimal principalPaid =
                repayments.stream()
                        .map(
                                LoanRepayment::getPrincipalAmount
                        )
                        .filter(value -> value != null)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );


        // -----------------------------------------------------
        // Current outstanding interest
        //
        // accruedInterest =
        // interest generated but not yet paid
        // -----------------------------------------------------

        BigDecimal interestOutstanding =
                loan.getAccruedInterest() != null
                        ? loan.getAccruedInterest()
                        : BigDecimal.ZERO;


        // -----------------------------------------------------
        // Current outstanding principal
        // -----------------------------------------------------

        BigDecimal principalOutstanding =
                loan.getOutstandingPrincipal() != null
                        ? loan.getOutstandingPrincipal()
                        : BigDecimal.ZERO;


        // -----------------------------------------------------
        // Total outstanding
        // -----------------------------------------------------

        BigDecimal totalOutstanding =
                principalOutstanding
                        .add(interestOutstanding);


        // -----------------------------------------------------
        // Historical interest accrued
        // -----------------------------------------------------

        BigDecimal totalInterestAccrued =
                loan.getTotalInterestAccrued() != null
                        ? loan.getTotalInterestAccrued()
                        : BigDecimal.ZERO;


        // -----------------------------------------------------
        // Current total payable
        // -----------------------------------------------------

        BigDecimal totalPayable =
                loan.getTotalPayable() != null
                        ? loan.getTotalPayable()
                        : totalOutstanding;


        return new LoanReportResponse(

                loan.getId(),

                loan.getLoanNumber(),

                loan.getUser().getId(),

                loan.getUser().getName(),

                // Original principal
                loan.getPrincipalAmount(),

                // Monthly interest rate
                loan.getInterestRate(),

                // Total interest generated historically
                totalInterestAccrued,

                // Current total payable
                totalPayable,

                // Total amount received
                totalPaid,

                // Interest received
                interestPaid,

                // Principal received
                principalPaid,

                // Current interest outstanding
                interestOutstanding,

                // Current principal outstanding
                principalOutstanding,

                // Current total outstanding
                totalOutstanding,

                // Start date
                loan.getStartDate(),

                // Status
                loan.getStatus()
        );
    }


    // =========================================================
    // DEPOSIT REPORT
    // =========================================================

    @Override
    public List<DepositReportResponse> getDepositReport() {

        return depositRepository.findAll()
                .stream()
                .map(this::buildDepositReport)
                .toList();
    }


    // =========================================================
    // BUILD DEPOSIT REPORT
    // =========================================================

    private DepositReportResponse buildDepositReport(
            Deposit deposit
    ) {

        BigDecimal totalDeposited =
                depositTransactionRepository
                        .findByDepositId(
                                deposit.getId()
                        )
                        .stream()
                        .map(
                                DepositTransaction::getAmount
                        )
                        .filter(value -> value != null)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );


        BigDecimal interestAmount =
                deposit.getInterestAmount() != null
                        ? deposit.getInterestAmount()
                        : BigDecimal.ZERO;


        BigDecimal closingAmount =
                deposit.getClosingAmount() != null
                        ? deposit.getClosingAmount()
                        : BigDecimal.ZERO;


        return new DepositReportResponse(

                deposit.getId(),

                deposit.getDepositNumber(),

                deposit.getUser().getId(),

                deposit.getUser().getName(),

                totalDeposited,

                deposit.getInterestRate(),

                interestAmount,

                closingAmount,

                deposit.getStartDate(),

                deposit.getClosedDate(),

                deposit.getStatus()
        );
    }


    // =========================================================
    // REPAYMENT REPORT
    // =========================================================

    @Override
    public List<RepaymentReportResponse>
    getRepaymentReport() {

        return repaymentRepository.findAll()
                .stream()
                .map(this::buildRepaymentReport)
                .toList();
    }


    // =========================================================
    // BUILD REPAYMENT REPORT
    // =========================================================

    private RepaymentReportResponse buildRepaymentReport(
            LoanRepayment repayment
    ) {

        Loan loan =
                repayment.getLoan();

        User user =
                loan.getUser();


        return new RepaymentReportResponse(

                repayment.getId(),

                loan.getId(),

                loan.getLoanNumber(),

                user.getId(),

                user.getName(),

                repayment.getAmount(),

                repayment.getInterestAmount(),

                repayment.getPrincipalAmount(),

                repayment.getPaymentMethod(),

                repayment.getRepaymentDate(),

                repayment.getNotes()
        );
    }


    // =========================================================
    // FINANCIAL TRANSACTION REPORT
    // =========================================================

    @Override
    public List<FinancialTransactionReportResponse>
    getFinancialTransactionReport() {

        return financialTransactionRepository
                .findAll()
                .stream()
                .map(
                        this::buildFinancialTransactionReport
                )
                .toList();
    }


    // =========================================================
    // BUILD FINANCIAL TRANSACTION REPORT
    // =========================================================

    private FinancialTransactionReportResponse
    buildFinancialTransactionReport(
            FinancialTransaction transaction
    ) {

        User user =
                transaction.getUser();

        Loan loan =
                transaction.getLoan();

        Deposit deposit =
                transaction.getDeposit();


        return new FinancialTransactionReportResponse(

                transaction.getId(),

                transaction.getTransactionNumber(),

                transaction.getType(),

                transaction.getAmount(),

                transaction.getTransactionDate(),

                transaction.getDescription(),

                // User
                user != null
                        ? user.getId()
                        : null,

                user != null
                        ? user.getName()
                        : null,

                // Loan
                loan != null
                        ? loan.getId()
                        : null,

                loan != null
                        ? loan.getLoanNumber()
                        : null,

                // Deposit
                deposit != null
                        ? deposit.getId()
                        : null,

                deposit != null
                        ? deposit.getDepositNumber()
                        : null
        );
    }


    // =========================================================
    // PROFIT REPORT
    // =========================================================

    @Override
    public ProfitReportResponse getProfitReport() {

        List<Loan> loans =
                loanRepository.findAll();


        // =====================================================
        // TOTAL INTEREST ACCRUED
        //
        // Historical interest generated.
        // =====================================================

        BigDecimal loanInterestContracted =
                loans.stream()
                        .map(
                                Loan::getTotalInterestAccrued
                        )
                        .filter(value -> value != null)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );


        // =====================================================
        // INTEREST ACTUALLY RECEIVED
        // =====================================================

        BigDecimal loanInterestReceived =
                loans.stream()
                        .map(
                                Loan::getTotalInterestPaid
                        )
                        .filter(value -> value != null)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );


        // =====================================================
        // INTEREST CURRENTLY OUTSTANDING
        // =====================================================

        BigDecimal loanInterestOutstanding =
                loans.stream()
                        .map(
                                Loan::getAccruedInterest
                        )
                        .filter(value -> value != null)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );


        // =====================================================
        // DEPOSIT INTEREST PAID
        // =====================================================

        BigDecimal depositInterestPaid =
                depositRepository.findAll()
                        .stream()
                        .filter(deposit ->
                                deposit.getStatus()
                                        == DepositStatus.CLOSED
                        )
                        .map(
                                Deposit::getInterestAmount
                        )
                        .filter(value -> value != null)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );


        // =====================================================
        // NET INTEREST PROFIT
        //
        // Only RECEIVED interest is considered profit.
        //
        // Outstanding interest is NOT profit yet.
        // =====================================================

        BigDecimal netInterestProfit =
                loanInterestReceived
                        .subtract(
                                depositInterestPaid
                        );


        // =====================================================
        // AVAILABLE BALANCE
        // =====================================================

        BigDecimal availableBalance =
                financialTransactionService
                        .getAvailableBalance();


        // =====================================================
        // RESPONSE
        // =====================================================

        return new ProfitReportResponse(

                // Total interest generated
                loanInterestContracted,

                // Interest received
                loanInterestReceived,

                // Interest still outstanding
                loanInterestOutstanding,

                // Interest paid to depositors
                depositInterestPaid,

                // Net interest profit
                netInterestProfit,

                // Current cash balance
                availableBalance,

                // Net profit
                netInterestProfit
        );
    }


    // =========================================================
    // USER FINANCIAL REPORT
    // =========================================================

    @Override
    public UserFinancialReportResponse
    getUserFinancialReport(
            Long userId
    ) {

        User user =
                userRepository.findById(
                        userId
                ).orElseThrow(() ->
                        new RuntimeException(
                                "User not found with id: "
                                        + userId
                        )
                );


        List<Deposit> deposits =
                depositRepository.findByUserId(
                        userId
                );


        List<Loan> loans =
                loanRepository.findByUserId(
                        userId
                );


        // =====================================================
        // DEPOSITS
        // =====================================================

        BigDecimal totalDeposited =
                deposits.stream()
                        .map(deposit ->
                                depositTransactionRepository
                                        .findByDepositId(
                                                deposit.getId()
                                        )
                                        .stream()
                                        .map(
                                                DepositTransaction::getAmount
                                        )
                                        .filter(
                                                value -> value != null
                                        )
                                        .reduce(
                                                BigDecimal.ZERO,
                                                BigDecimal::add
                                        )
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );


        // =====================================================
        // ACTIVE DEPOSIT PRINCIPAL
        // =====================================================

        BigDecimal activeDepositPrincipal =
                deposits.stream()
                        .filter(deposit ->
                                deposit.getStatus()
                                        == DepositStatus.ACTIVE
                        )
                        .map(deposit ->
                                depositTransactionRepository
                                        .findByDepositId(
                                                deposit.getId()
                                        )
                                        .stream()
                                        .map(
                                                DepositTransaction::getAmount
                                        )
                                        .filter(
                                                value -> value != null
                                        )
                                        .reduce(
                                                BigDecimal.ZERO,
                                                BigDecimal::add
                                        )
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );


        // =====================================================
        // DEPOSIT INTEREST PAID
        // =====================================================

        BigDecimal depositInterestPaid =
                deposits.stream()
                        .filter(deposit ->
                                deposit.getStatus()
                                        == DepositStatus.CLOSED
                        )
                        .map(
                                Deposit::getInterestAmount
                        )
                        .filter(value -> value != null)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );


        // =====================================================
        // DEPOSIT COUNTS
        // =====================================================

        int activeDeposits =
                (int) deposits.stream()
                        .filter(deposit ->
                                deposit.getStatus()
                                        == DepositStatus.ACTIVE
                        )
                        .count();


        int closedDeposits =
                (int) deposits.stream()
                        .filter(deposit ->
                                deposit.getStatus()
                                        == DepositStatus.CLOSED
                        )
                        .count();


        // =====================================================
        // LOANS
        // =====================================================

        BigDecimal totalLoanPrincipal =
                loans.stream()
                        .map(
                                Loan::getPrincipalAmount
                        )
                        .filter(value -> value != null)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );


        // =====================================================
        // PRINCIPAL OUTSTANDING
        // =====================================================

        BigDecimal loanPrincipalOutstanding =
                loans.stream()
                        .map(
                                Loan::getOutstandingPrincipal
                        )
                        .filter(value -> value != null)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );


        // =====================================================
        // TOTAL INTEREST ACCRUED
        // =====================================================

        BigDecimal totalLoanInterest =
                loans.stream()
                        .map(
                                Loan::getTotalInterestAccrued
                        )
                        .filter(value -> value != null)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );


        // =====================================================
        // INTEREST PAID
        // =====================================================

        BigDecimal loanInterestPaid =
                loans.stream()
                        .map(
                                Loan::getTotalInterestPaid
                        )
                        .filter(value -> value != null)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );


        // =====================================================
        // TOTAL LOAN REPAID
        //
        // Principal paid + interest paid
        // =====================================================

        BigDecimal totalPrincipalPaid =
                loans.stream()
                        .map(
                                Loan::getTotalPrincipalPaid
                        )
                        .filter(value -> value != null)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );


        BigDecimal totalLoanRepaid =
                totalPrincipalPaid
                        .add(loanInterestPaid);


        // =====================================================
        // TOTAL LOAN OUTSTANDING
        //
        // Principal outstanding
        // +
        // Interest outstanding
        // =====================================================

        BigDecimal loanInterestOutstanding =
                loans.stream()
                        .map(
                                Loan::getAccruedInterest
                        )
                        .filter(value -> value != null)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );


        BigDecimal totalLoanOutstanding =
                loanPrincipalOutstanding
                        .add(
                                loanInterestOutstanding
                        );


        // =====================================================
        // LOAN COUNTS
        // =====================================================

        int activeLoans =
                (int) loans.stream()
                        .filter(loan ->
                                loan.getStatus()
                                        == LoanStatus.ACTIVE
                        )
                        .count();


        int partiallyPaidLoans =
                (int) loans.stream()
                        .filter(loan ->
                                loan.getStatus()
                                        == LoanStatus.PARTIALLY_PAID
                        )
                        .count();


        int closedLoans =
                (int) loans.stream()
                        .filter(loan ->
                                loan.getStatus()
                                        == LoanStatus.CLOSED
                        )
                        .count();


        // =====================================================
        // RESPONSE
        // =====================================================

        return new UserFinancialReportResponse(

                user.getId(),

                user.getName(),

                // -------------------------------
                // Deposits
                // -------------------------------

                deposits.size(),

                activeDeposits,

                closedDeposits,

                totalDeposited,

                activeDepositPrincipal,

                depositInterestPaid,

                // -------------------------------
                // Loans
                // -------------------------------

                loans.size(),

                activeLoans,

                partiallyPaidLoans,

                closedLoans,

                totalLoanPrincipal,

                loanPrincipalOutstanding,

                totalLoanInterest,

                loanInterestPaid,

                totalLoanRepaid,

                totalLoanOutstanding
        );
    }
}