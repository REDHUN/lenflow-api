package com.redhun.lendflow_api.service.impl;

import com.redhun.lendflow_api.dto.dashboard.DashboardResponse;
import com.redhun.lendflow_api.entity.Deposit;
import com.redhun.lendflow_api.entity.DepositTransaction;
import com.redhun.lendflow_api.entity.FinancialTransaction;
import com.redhun.lendflow_api.entity.Loan;
import com.redhun.lendflow_api.entity.LoanRepayment;
import com.redhun.lendflow_api.enums.DepositStatus;
import com.redhun.lendflow_api.enums.LoanStatus;
import com.redhun.lendflow_api.enums.TransactionType;
import com.redhun.lendflow_api.repository.DepositRepository;
import com.redhun.lendflow_api.repository.DepositTransactionRepository;
import com.redhun.lendflow_api.repository.FinancialTransactionRepository;
import com.redhun.lendflow_api.repository.LoanRepaymentRepository;
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
public class DashboardServiceImpl implements DashboardService {

    private final UserRepository userRepository;
    private final DepositRepository depositRepository;
    private final DepositTransactionRepository depositTransactionRepository;
    private final LoanRepository loanRepository;
    private final LoanRepaymentRepository loanRepaymentRepository;
    private final FinancialTransactionRepository financialTransactionRepository;
    private final FinancialTransactionService financialTransactionService;

    @Override
    public DashboardResponse getAdminDashboard() {

        // =====================================================
        // USERS
        // =====================================================

        long totalUsers =
                userRepository.count();

        long activeUsers =
                userRepository.countByActiveTrue();


        // =====================================================
        // DEPOSITS
        // =====================================================

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


        // =====================================================
        // LOANS
        // =====================================================

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


        BigDecimal totalLoanPrincipal =
                loans.stream()
                        .map(Loan::getPrincipalAmount)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );


        BigDecimal totalLoanInterest =
                loans.stream()
                        .map(Loan::getTotalInterest)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );


        BigDecimal totalLoanRepaid =
                loanRepaymentRepository
                        .findAll()
                        .stream()
                        .map(LoanRepayment::getAmount)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );


        // =====================================================
        // ACTUAL INTEREST RECEIVED
        // =====================================================

        BigDecimal loanInterestReceived =
                loanRepaymentRepository
                        .findAll()
                        .stream()
                        .map(LoanRepayment::getInterestAmount)
                        .filter(amount -> amount != null)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );


        // =====================================================
        // PRINCIPAL OUTSTANDING
        // =====================================================

        BigDecimal loanPrincipalOutstanding =
                calculateLoanPrincipalOutstanding(
                        loans
                );


        // =====================================================
        // TOTAL LOAN OUTSTANDING
        // =====================================================

        BigDecimal totalLoanOutstanding =
                loans.stream()
                        .map(this::calculateLoanOutstanding)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );


        // =====================================================
        // AVAILABLE BALANCE
        // =====================================================

        BigDecimal availableBalance =
                financialTransactionService
                        .getAvailableBalance();


        // =====================================================
        // EXPENSES
        // =====================================================

        BigDecimal totalExpenses =
                financialTransactionRepository
                        .findByType(
                                TransactionType.EXPENSE
                        )
                        .stream()
                        .map(FinancialTransaction::getAmount)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );


        // =====================================================
        // NET PROFIT
        // =====================================================

        BigDecimal netProfit =
                loanInterestReceived
                        .subtract(
                                depositInterestPaid
                        )
                        .subtract(
                                totalExpenses
                        );


        // =====================================================
        // RESPONSE
        // =====================================================

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


    // =========================================================
    // TOTAL HISTORICAL DEPOSITS
    // =========================================================

    private BigDecimal calculateTotalDepositedHistorically(
            List<Deposit> deposits
    ) {

        BigDecimal total =
                BigDecimal.ZERO;

        for (Deposit deposit : deposits) {

            BigDecimal depositAmount =
                    calculateTotalDeposit(
                            deposit
                    );

            total =
                    total.add(
                            depositAmount
                    );
        }

        return total;
    }


    // =========================================================
    // ACTIVE DEPOSIT PRINCIPAL
    // =========================================================

    private BigDecimal calculateActiveDepositPrincipal(
            List<Deposit> deposits
    ) {

        BigDecimal total =
                BigDecimal.ZERO;

        for (Deposit deposit : deposits) {

            if (deposit.getStatus()
                    != DepositStatus.ACTIVE) {

                continue;
            }

            BigDecimal amount =
                    calculateTotalDeposit(
                            deposit
                    );

            total =
                    total.add(
                            amount
                    );
        }

        return total;
    }


    // =========================================================
    // DEPOSIT PRINCIPAL RETURNED
    // =========================================================

    private BigDecimal calculateDepositPrincipalReturned(
            List<Deposit> deposits
    ) {

        return deposits.stream()
                .filter(deposit ->
                        deposit.getStatus()
                                == DepositStatus.CLOSED
                )
                .map(this::calculateTotalDeposit)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );
    }


    // =========================================================
    // DEPOSIT INTEREST PAID
    // =========================================================

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


    // =========================================================
    // TOTAL DEPOSIT AMOUNT
    // =========================================================

    private BigDecimal calculateTotalDeposit(
            Deposit deposit
    ) {

        return depositTransactionRepository
                .findByDepositId(
                        deposit.getId()
                )
                .stream()
                .map(DepositTransaction::getAmount)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );
    }


    // =========================================================
    // LOAN PRINCIPAL OUTSTANDING
    // =========================================================

    private BigDecimal calculateLoanPrincipalOutstanding(
            List<Loan> loans
    ) {

        BigDecimal total =
                BigDecimal.ZERO;

        for (Loan loan : loans) {

            BigDecimal totalPaid =
                    calculateTotalPaid(
                            loan.getId()
                    );

            BigDecimal interestPaid =
                    loanRepaymentRepository
                            .findByLoanId(
                                    loan.getId()
                            )
                            .stream()
                            .map(
                                    LoanRepayment::getInterestAmount
                            )
                            .filter(
                                    amount -> amount != null
                            )
                            .reduce(
                                    BigDecimal.ZERO,
                                    BigDecimal::add
                            );

            BigDecimal principalPaid =
                    totalPaid.subtract(
                            interestPaid
                    );

            BigDecimal principalOutstanding =
                    loan.getPrincipalAmount()
                            .subtract(
                                    principalPaid
                            );

            if (principalOutstanding.compareTo(
                    BigDecimal.ZERO
            ) < 0) {

                principalOutstanding =
                        BigDecimal.ZERO;
            }

            total =
                    total.add(
                            principalOutstanding
                    );
        }

        return total;
    }


    // =========================================================
    // TOTAL LOAN OUTSTANDING
    // =========================================================

    private BigDecimal calculateLoanOutstanding(
            Loan loan
    ) {

        BigDecimal totalPaid =
                calculateTotalPaid(
                        loan.getId()
                );

        BigDecimal outstanding =
                loan.getTotalPayable()
                        .subtract(
                                totalPaid
                        );

        if (outstanding.compareTo(
                BigDecimal.ZERO
        ) < 0) {

            return BigDecimal.ZERO;
        }

        return outstanding;
    }


    // =========================================================
    // TOTAL LOAN PAID
    // =========================================================

    private BigDecimal calculateTotalPaid(
            Long loanId
    ) {

        return loanRepaymentRepository
                .findByLoanId(
                        loanId
                )
                .stream()
                .map(LoanRepayment::getAmount)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );
    }
}