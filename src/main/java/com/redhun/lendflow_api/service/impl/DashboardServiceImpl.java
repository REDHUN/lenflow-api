package com.redhun.lendflow_api.service.impl;

import com.redhun.lendflow_api.dto.dashboard.DashboardResponse;
import com.redhun.lendflow_api.entity.Deposit;
import com.redhun.lendflow_api.entity.DepositTransaction;
import com.redhun.lendflow_api.entity.FinancialTransaction;
import com.redhun.lendflow_api.entity.Loan;
import com.redhun.lendflow_api.enums.DepositStatus;
import com.redhun.lendflow_api.enums.LoanStatus;
import com.redhun.lendflow_api.enums.TransactionType;
import com.redhun.lendflow_api.repository.DepositRepository;
import com.redhun.lendflow_api.repository.DepositTransactionRepository;
import com.redhun.lendflow_api.repository.FinancialTransactionRepository;
import com.redhun.lendflow_api.repository.LoanRepository;
import com.redhun.lendflow_api.repository.UserRepository;
import com.redhun.lendflow_api.service.DashboardService;
import com.redhun.lendflow_api.service.FinancialTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl
        implements DashboardService {

    private final UserRepository userRepository;

    private final DepositRepository depositRepository;

    private final DepositTransactionRepository
            depositTransactionRepository;

    private final LoanRepository loanRepository;

    private final FinancialTransactionRepository
            financialTransactionRepository;

    private final FinancialTransactionService
            financialTransactionService;


    // =====================================================
    // ADMIN DASHBOARD
    // =====================================================

    @Override
    public DashboardResponse getAdminDashboard() {

        // =================================================
        // USERS
        // =================================================

        long totalUsers =
                userRepository.count();

        long activeUsers =
                userRepository.countByActiveTrue();


        // =================================================
        // DEPOSITS
        // =================================================

        long totalDeposits =
                depositRepository.count();

        long activeDeposits =
                depositRepository.countByStatus(
                        DepositStatus.ACTIVE
                );

        long closedDeposits =
                depositRepository.countByStatus(
                        DepositStatus.CLOSED
                );

        List<Deposit> deposits =
                depositRepository.findAll();


        BigDecimal totalDepositedHistorically =
                calculateTotalDepositedHistorically(
                        deposits
                );


        BigDecimal activeDepositPrincipal =
                calculateActiveDepositPrincipal(
                        deposits
                );


        BigDecimal depositPrincipalReturned =
                calculateDepositPrincipalReturned(
                        deposits
                );


        BigDecimal depositInterestPaid =
                calculateDepositInterestPaid(
                        deposits
                );


        // =================================================
        // LOANS
        // =================================================

        long totalLoans =
                loanRepository.count();

        long activeLoans =
                loanRepository.countByStatus(
                        LoanStatus.ACTIVE
                );

        long partiallyPaidLoans =
                loanRepository.countByStatus(
                        LoanStatus.PARTIALLY_PAID
                );

        long closedLoans =
                loanRepository.countByStatus(
                        LoanStatus.CLOSED
                );


        List<Loan> loans =
                loanRepository.findAll();


        // =================================================
        // TOTAL ORIGINAL LOAN PRINCIPAL
        // =================================================

        BigDecimal totalLoanPrincipal =
                loans.stream()
                        .map(Loan::getPrincipalAmount)
                        .filter(amount ->
                                amount != null
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );


        // =================================================
        // TOTAL INTEREST ACCRUED
        // =================================================

        BigDecimal totalLoanInterest =
                loans.stream()
                        .map(Loan::getTotalInterestAccrued)
                        .filter(amount ->
                                amount != null
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );


        // =================================================
        // INTEREST ACTUALLY RECEIVED
        // =================================================

        BigDecimal loanInterestReceived =
                loans.stream()
                        .map(Loan::getTotalInterestPaid)
                        .filter(amount ->
                                amount != null
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );


        // =================================================
        // PRINCIPAL OUTSTANDING
        // =================================================

        BigDecimal loanPrincipalOutstanding =
                loans.stream()
                        .map(
                                Loan::getOutstandingPrincipal
                        )
                        .filter(amount ->
                                amount != null
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );


        // =================================================
        // TOTAL LOAN REPAID
        //
        // Principal paid + interest paid
        // =================================================

        BigDecimal totalLoanRepaid =
                loans.stream()
                        .map(loan -> {

                            BigDecimal principalPaid =
                                    loan.getTotalPrincipalPaid();

                            BigDecimal interestPaid =
                                    loan.getTotalInterestPaid();

                            if (principalPaid == null) {
                                principalPaid =
                                        BigDecimal.ZERO;
                            }

                            if (interestPaid == null) {
                                interestPaid =
                                        BigDecimal.ZERO;
                            }

                            return principalPaid
                                    .add(interestPaid);
                        })
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );


        // =================================================
        // TOTAL LOAN OUTSTANDING
        //
        // Principal outstanding
        // +
        // Interest outstanding
        // =================================================

        BigDecimal totalLoanOutstanding =
                loans.stream()
                        .map(loan -> {

                            BigDecimal principal =
                                    loan.getOutstandingPrincipal();

                            BigDecimal interest =
                                    loan.getAccruedInterest();

                            if (principal == null) {
                                principal =
                                        BigDecimal.ZERO;
                            }

                            if (interest == null) {
                                interest =
                                        BigDecimal.ZERO;
                            }

                            return principal.add(
                                    interest
                            );
                        })
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );


        // =================================================
        // AVAILABLE BALANCE
        // =================================================

        BigDecimal availableBalance =
                financialTransactionService
                        .getAvailableBalance();


        // =================================================
        // EXPENSES
        // =================================================

        BigDecimal totalExpenses =
                financialTransactionRepository
                        .findByType(
                                TransactionType.EXPENSE
                        )
                        .stream()
                        .map(
                                FinancialTransaction::getAmount
                        )
                        .filter(amount ->
                                amount != null
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );


        // =================================================
        // NET PROFIT
        //
        // Loan interest received
        // - Deposit interest paid
        // - Expenses
        // =================================================

        BigDecimal netProfit =
                loanInterestReceived
                        .subtract(
                                depositInterestPaid
                        )
                        .subtract(
                                totalExpenses
                        );


        // =================================================
        // RESPONSE
        // =================================================

        return new DashboardResponse(

                // Users
                totalUsers,
                activeUsers,

                // Deposits
                totalDeposits,
                activeDeposits,
                closedDeposits,
                activeDepositPrincipal,
                totalDepositedHistorically,
                depositPrincipalReturned,
                depositInterestPaid,

                // Loans
                totalLoans,
                activeLoans,
                partiallyPaidLoans,
                closedLoans,
                totalLoanPrincipal,
                loanPrincipalOutstanding,
                totalLoanInterest,
                loanInterestReceived,
                totalLoanRepaid,
                totalLoanOutstanding,

                // Cash
                availableBalance,

                // Expenses / Profit
                totalExpenses,
                netProfit
        );
    }


    // =====================================================
    // TOTAL HISTORICAL DEPOSITS
    // =====================================================

    private BigDecimal calculateTotalDepositedHistorically(
            List<Deposit> deposits
    ) {

        return deposits.stream()
                .map(
                        this::calculateTotalDeposit
                )
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );
    }


    // =====================================================
    // ACTIVE DEPOSIT PRINCIPAL
    // =====================================================

    private BigDecimal calculateActiveDepositPrincipal(
            List<Deposit> deposits
    ) {

        return deposits.stream()
                .filter(deposit ->
                        deposit.getStatus()
                                == DepositStatus.ACTIVE
                )
                .map(
                        this::calculateTotalDeposit
                )
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );
    }


    // =====================================================
    // DEPOSIT PRINCIPAL RETURNED
    // =====================================================

    private BigDecimal calculateDepositPrincipalReturned(
            List<Deposit> deposits
    ) {

        return deposits.stream()
                .filter(deposit ->
                        deposit.getStatus()
                                == DepositStatus.CLOSED
                )
                .map(
                        this::calculateTotalDeposit
                )
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );
    }


    // =====================================================
    // DEPOSIT INTEREST PAID
    // =====================================================

    private BigDecimal calculateDepositInterestPaid(
            List<Deposit> deposits
    ) {

        return deposits.stream()
                .filter(deposit ->
                        deposit.getStatus()
                                == DepositStatus.CLOSED
                )
                .map(deposit -> {

                    BigDecimal interest =
                            deposit.getInterestAmount();

                    return interest != null
                            ? interest
                            : BigDecimal.ZERO;
                })
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );
    }


    // =====================================================
    // TOTAL DEPOSIT AMOUNT
    // =====================================================

    private BigDecimal calculateTotalDeposit(
            Deposit deposit
    ) {

        return depositTransactionRepository
                .findByDepositId(
                        deposit.getId()
                )
                .stream()
                .map(
                        DepositTransaction::getAmount
                )
                .filter(amount ->
                        amount != null
                )
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );
    }
}