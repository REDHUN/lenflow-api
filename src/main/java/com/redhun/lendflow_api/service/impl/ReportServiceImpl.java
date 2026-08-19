package com.redhun.lendflow_api.service.impl;

import com.redhun.lendflow_api.dto.reports.*;
import com.redhun.lendflow_api.dto.reports.reportfilter.DepositReportRequest;
import com.redhun.lendflow_api.dto.reports.reportfilter.FinancialTransactionReportRequest;
import com.redhun.lendflow_api.dto.reports.reportfilter.LoanReportRequest;
import com.redhun.lendflow_api.dto.reports.reportfilter.RepaymentReportRequest;
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
import java.time.LocalDate;
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
    public List<LoanReportResponse> getLoanReport(
            LoanReportRequest request
    ) {

        return loanRepository.findAll()
                .stream()
                .filter(loan ->
                        isWithinDateRange(
                                loan.getStartDate(),
                                request.fromDate(),
                                request.toDate()
                        )
                )
                .filter(loan ->
                        matchesUser(
                                loan.getUser(),
                                request.userId()
                        )
                )
                .filter(loan ->
                        request.status() == null
                                || loan.getStatus()
                                == request.status()
                )
                .map(this::buildLoanReport)
                .toList();
    }


    // =========================================================
    // BUILD LOAN REPORT
    // =========================================================

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
                        .filter(value -> value != null)
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
                loan.getAccruedInterest() != null
                        ? loan.getAccruedInterest()
                        : BigDecimal.ZERO;


        BigDecimal principalOutstanding =
                loan.getOutstandingPrincipal() != null
                        ? loan.getOutstandingPrincipal()
                        : BigDecimal.ZERO;


        BigDecimal totalOutstanding =
                principalOutstanding
                        .add(interestOutstanding);


        BigDecimal totalInterestAccrued =
                loan.getTotalInterestAccrued() != null
                        ? loan.getTotalInterestAccrued()
                        : BigDecimal.ZERO;


        BigDecimal totalPayable =
                loan.getTotalPayable() != null
                        ? loan.getTotalPayable()
                        : totalOutstanding;


        return new LoanReportResponse(

                loan.getId(),

                loan.getLoanNumber(),

                loan.getUser().getId(),

                loan.getUser().getName(),

                loan.getPrincipalAmount(),

                loan.getInterestRate(),

                totalInterestAccrued,

                totalPayable,

                totalPaid,

                interestPaid,

                principalPaid,

                interestOutstanding,

                principalOutstanding,

                totalOutstanding,

                loan.getStartDate(),

                loan.getStatus()
        );
    }


    // =========================================================
    // DEPOSIT REPORT
    // =========================================================

    @Override
    public List<DepositReportResponse> getDepositReport(
            DepositReportRequest request
    ) {

        return depositRepository.findAll()
                .stream()
                .filter(deposit ->
                        isWithinDateRange(
                                deposit.getStartDate(),
                                request.fromDate(),
                                request.toDate()
                        )
                )
                .filter(deposit ->
                        matchesUser(
                                deposit.getUser(),
                                request.userId()
                        )
                )
                .filter(deposit ->
                        request.status() == null
                                || deposit.getStatus()
                                == request.status()
                )
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
    public List<RepaymentReportResponse> getRepaymentReport(
            RepaymentReportRequest request
    ) {

        return repaymentRepository.findAll()
                .stream()
                .filter(repayment ->
                        isWithinDateRange(
                                repayment.getRepaymentDate(),
                                request.fromDate(),
                                request.toDate()
                        )
                )
                .filter(repayment ->
                        matchesUser(
                                repayment.getLoan().getUser(),
                                request.userId()
                        )
                )
                .filter(repayment ->
                        request.loanId() == null
                                || repayment.getLoan()
                                .getId()
                                .equals(request.loanId())
                )
                .filter(repayment ->
                        request.paymentMethod() == null
                                || repayment.getPaymentMethod()
                                == request.paymentMethod()
                )
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
    getFinancialTransactionReport(
            FinancialTransactionReportRequest request
    ) {

        return financialTransactionRepository
                .findAll()
                .stream()
                .filter(transaction ->
                        isWithinDateRange(
                                transaction.getTransactionDate(),
                                request.fromDate(),
                                request.toDate()
                        )
                )
                .filter(transaction ->
                        matchesUser(
                                transaction.getUser(),
                                request.userId()
                        )
                )
                .filter(transaction ->
                        matchesLoan(
                                transaction,
                                request.loanId()
                        )
                )
                .filter(transaction ->
                        matchesDeposit(
                                transaction,
                                request.depositId()
                        )
                )
                .filter(transaction ->
                        request.transactionType() == null
                                || transaction.getType()
                                == request.transactionType()
                )
                .map(this::buildFinancialTransactionReport)
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

        List<Loan> loans =
                loanRepository.findAll();


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


        BigDecimal netInterestProfit =
                loanInterestReceived
                        .subtract(depositInterestPaid);


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
        // TOTAL DEPOSITED
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
        // TOTAL LOAN PRINCIPAL
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
        // LOAN PRINCIPAL OUTSTANDING
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
        // TOTAL LOAN INTEREST
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
        // LOAN INTEREST PAID
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
        // LOAN INTEREST OUTSTANDING
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
        // TOTAL LOAN OUTSTANDING
        // =====================================================

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


    // =========================================================
    // COMMON DATE FILTER
    // =========================================================

    private boolean isWithinDateRange(
            LocalDate date,
            LocalDate fromDate,
            LocalDate toDate
    ) {

        if (date == null) {
            return false;
        }


        // -----------------------------------------------------
        // FROM DATE
        // -----------------------------------------------------

        if (fromDate != null
                && date.isBefore(fromDate)) {

            return false;
        }


        // -----------------------------------------------------
        // TO DATE
        // -----------------------------------------------------

        if (toDate != null
                && date.isAfter(toDate)) {

            return false;
        }


        return true;
    }


    // =========================================================
    // COMMON USER FILTER
    // =========================================================

    private boolean matchesUser(
            User user,
            Long userId
    ) {

        if (userId == null) {
            return true;
        }

        return user != null
                && user.getId().equals(userId);
    }


    // =========================================================
    // LOAN FILTER
    // =========================================================

    private boolean matchesLoan(
            FinancialTransaction transaction,
            Long loanId
    ) {

        if (loanId == null) {
            return true;
        }

        return transaction.getLoan() != null
                && transaction.getLoan()
                .getId()
                .equals(loanId);
    }


    // =========================================================
    // DEPOSIT FILTER
    // =========================================================

    private boolean matchesDeposit(
            FinancialTransaction transaction,
            Long depositId
    ) {

        if (depositId == null) {
            return true;
        }

        return transaction.getDeposit() != null
                && transaction.getDeposit()
                .getId()
                .equals(depositId);
    }
}