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
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

    private final LoanRepository loanRepository;
    private final LoanRepaymentRepository repaymentRepository;

    private final DepositRepository depositRepository;
    private final DepositTransactionRepository depositTransactionRepository;

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


    private LoanReportResponse buildLoanReport(
            Loan loan
    ) {

        List<LoanRepayment> repayments =
                repaymentRepository.findByLoanId(
                        loan.getId()
                );

        BigDecimal totalPaid =
                repayments.stream()
                        .map(LoanRepayment::getAmount)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        BigDecimal interestPaid =
                repayments.stream()
                        .map(LoanRepayment::getInterestAmount)
                        .filter(value -> value != null)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        BigDecimal principalPaid =
                repayments.stream()
                        .map(LoanRepayment::getPrincipalAmount)
                        .filter(value -> value != null)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        BigDecimal interestOutstanding =
                loan.getTotalInterest()
                        .subtract(interestPaid);

        BigDecimal principalOutstanding =
                loan.getPrincipalAmount()
                        .subtract(principalPaid);

        if (interestOutstanding.compareTo(
                BigDecimal.ZERO
        ) < 0) {
            interestOutstanding = BigDecimal.ZERO;
        }

        if (principalOutstanding.compareTo(
                BigDecimal.ZERO
        ) < 0) {
            principalOutstanding = BigDecimal.ZERO;
        }

        BigDecimal totalOutstanding =
                interestOutstanding
                        .add(principalOutstanding);

        return new LoanReportResponse(

                loan.getId(),
                loan.getLoanNumber(),

                loan.getUser().getId(),
                loan.getUser().getName(),

                loan.getPrincipalAmount(),
                loan.getInterestRate(),
                loan.getTotalInterest(),
                loan.getTotalPayable(),

                totalPaid,
                interestPaid,
                principalPaid,

                interestOutstanding,
                principalOutstanding,
                totalOutstanding,

                loan.getStartDate(),
                loan.getDueDate(),

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


    private DepositReportResponse buildDepositReport(
            Deposit deposit
    ) {

        BigDecimal totalDeposited =
                depositTransactionRepository
                        .findByDepositId(deposit.getId())
                        .stream()
                        .map(DepositTransaction::getAmount)
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


    private RepaymentReportResponse buildRepaymentReport(
            LoanRepayment repayment
    ) {

        Loan loan = repayment.getLoan();
        User user = loan.getUser();

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
                .map(this::buildFinancialTransactionReport)
                .toList();
    }


    private FinancialTransactionReportResponse
    buildFinancialTransactionReport(
            FinancialTransaction transaction
    ) {

        User user = transaction.getUser();

        Loan loan = transaction.getLoan();

        Deposit deposit = transaction.getDeposit();

        return new FinancialTransactionReportResponse(

                transaction.getId(),

                transaction.getTransactionNumber(),

                transaction.getType(),

                transaction.getAmount(),

                transaction.getTransactionDate(),

                transaction.getDescription(),

                user != null
                        ? user.getId()
                        : null,

                user != null
                        ? user.getName()
                        : null,

                loan != null
                        ? loan.getId()
                        : null,

                loan != null
                        ? loan.getLoanNumber()
                        : null,

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

        BigDecimal loanInterestContracted =
                loanRepository.findAll()
                        .stream()
                        .map(Loan::getTotalInterest)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );


        BigDecimal loanInterestReceived =
                repaymentRepository.findAll()
                        .stream()
                        .map(LoanRepayment::getInterestAmount)
                        .filter(value -> value != null)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );


        BigDecimal loanInterestOutstanding =
                loanInterestContracted
                        .subtract(
                                loanInterestReceived
                        );


        BigDecimal depositInterestPaid =
                depositRepository.findAll()
                        .stream()
                        .filter(deposit ->
                                deposit.getStatus()
                                        == DepositStatus.CLOSED
                        )
                        .map(Deposit::getInterestAmount)
                        .filter(value -> value != null)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );


        BigDecimal netInterestProfit =
                loanInterestReceived
                        .subtract(
                                depositInterestPaid
                        );


        BigDecimal availableBalance =
                financialTransactionService
                        .getAvailableBalance();


        return new ProfitReportResponse(

                loanInterestContracted,

                loanInterestReceived,

                loanInterestOutstanding,

                depositInterestPaid,

                netInterestProfit,

                availableBalance,

                netInterestProfit
        );
    }


    // =========================================================
    // USER FINANCIAL REPORT
    // =========================================================

    @Override
    public UserFinancialReportResponse
    getUserFinancialReport(Long userId) {

        User user =
                userRepository.findById(userId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "User not found with id: "
                                                + userId
                                )
                        );


        List<Deposit> deposits =
                depositRepository.findByUserId(userId);

        List<Loan> loans =
                loanRepository.findByUserId(userId);


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
                                        .reduce(
                                                BigDecimal.ZERO,
                                                BigDecimal::add
                                        )
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );


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
                                        .reduce(
                                                BigDecimal.ZERO,
                                                BigDecimal::add
                                        )
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );


        BigDecimal depositInterestPaid =
                deposits.stream()
                        .filter(deposit ->
                                deposit.getStatus()
                                        == DepositStatus.CLOSED
                        )
                        .map(Deposit::getInterestAmount)
                        .filter(value -> value != null)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );


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
                        .map(Loan::getPrincipalAmount)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );


        BigDecimal loanPrincipalOutstanding =
                BigDecimal.ZERO;


        BigDecimal totalLoanInterest =
                loans.stream()
                        .map(Loan::getTotalInterest)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );


        BigDecimal loanInterestPaid =
                BigDecimal.ZERO;


        BigDecimal totalLoanRepaid =
                BigDecimal.ZERO;


        BigDecimal totalLoanOutstanding =
                BigDecimal.ZERO;


        for (Loan loan : loans) {

            List<LoanRepayment> repayments =
                    repaymentRepository.findByLoanId(
                            loan.getId()
                    );


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


            BigDecimal totalPaid =
                    repayments.stream()
                            .map(LoanRepayment::getAmount)
                            .reduce(
                                    BigDecimal.ZERO,
                                    BigDecimal::add
                            );


            loanPrincipalOutstanding =
                    loanPrincipalOutstanding.add(
                            loan.getPrincipalAmount()
                                    .subtract(
                                            principalPaid
                                    )
                    );


            loanInterestPaid =
                    loanInterestPaid.add(
                            interestPaid
                    );


            totalLoanRepaid =
                    totalLoanRepaid.add(
                            totalPaid
                    );


            totalLoanOutstanding =
                    totalLoanOutstanding.add(
                            loan.getTotalPayable()
                                    .subtract(
                                            totalPaid
                                    )
                    );
        }


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


        return new UserFinancialReportResponse(

                user.getId(),
                user.getName(),

                deposits.size(),
                activeDeposits,
                closedDeposits,

                totalDeposited,
                activeDepositPrincipal,
                depositInterestPaid,

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