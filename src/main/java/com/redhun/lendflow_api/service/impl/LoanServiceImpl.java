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
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;
    private final LoanRepaymentRepository repaymentRepository;
    private final UserRepository userRepository;

    private final FinancialTransactionRepository financialTransactionRepository;

    private final FinancialTransactionService financialTransactionService;


    // =========================================================
    // CREATE LOAN
    // =========================================================

    @Override
    public LoanResponse createLoan(CreateLoanRequest request) {

        // -----------------------------------------------------
        // Find user
        // -----------------------------------------------------

        User user = userRepository.findById(request.userId()).orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.userId()));


        // -----------------------------------------------------
        // Check available balance BEFORE giving loan
        // -----------------------------------------------------

        BigDecimal availableBalance = financialTransactionService.getAvailableBalance();

        if (availableBalance.compareTo(request.principalAmount()) < 0) {

            throw new BusinessException("Insufficient available balance to create loan. " + "Required: ₹" + request.principalAmount() + ", Available: ₹" + availableBalance);
        }


        // -----------------------------------------------------
        // Calculate fixed loan interest
        //
        // Example:
        //
        // Principal = ₹100,000
        // Interest  = 10%
        //
        // Interest = ₹10,000
        // -----------------------------------------------------

        BigDecimal interestAmount = request.principalAmount().multiply(request.interestRate()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);


        // -----------------------------------------------------
        // Calculate total payable
        // -----------------------------------------------------

        BigDecimal totalPayable = request.principalAmount().add(interestAmount);


        // -----------------------------------------------------
        // Create loan
        // -----------------------------------------------------

        Loan loan = Loan.builder().loanNumber(generateLoanNumber()).user(user).principalAmount(request.principalAmount()).interestRate(request.interestRate()).startDate(request.startDate()).dueDate(request.dueDate()).totalInterest(interestAmount).totalPayable(totalPayable).status(LoanStatus.ACTIVE).build();

        Loan savedLoan = loanRepository.save(loan);


        // -----------------------------------------------------
        // Record money going OUT of the business
        // -----------------------------------------------------

        FinancialTransaction financialTransaction = FinancialTransaction.builder().transactionNumber(generateTransactionNumber()).type(TransactionType.LOAN_DISBURSEMENT).amount(request.principalAmount()).transactionDate(request.startDate()).description("Loan disbursement - " + savedLoan.getLoanNumber()).user(user).loan(savedLoan).createdAt(LocalDateTime.now()).build();

        financialTransactionRepository.save(financialTransaction);


        return buildLoanResponse(savedLoan);
    }


    // =========================================================
    // GET LOAN
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public LoanResponse getLoan(Long loanId) {

        Loan loan = loanRepository.findById(loanId).orElseThrow(() -> new ResourceNotFoundException("Loan not found with id: " + loanId));

        return buildLoanResponse(loan);
    }


    // =========================================================
    // GET ALL LOANS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<LoanResponse> getAllLoans() {

        return loanRepository.findAll().stream().map(this::buildLoanResponse).toList();
    }


    // =========================================================
    // GET USER LOANS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<LoanResponse> getUserLoans(Long userId) {

        if (!userRepository.existsById(userId)) {

            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        return loanRepository.findByUserId(userId).stream().map(this::buildLoanResponse).toList();
    }


    // =========================================================
    // CREATE REPAYMENT
    // =========================================================

    @Override
    public RepaymentResponse createRepayment(Long loanId, CreateRepaymentRequest request) {

        Loan loan = loanRepository.findById(loanId).orElseThrow(() -> new ResourceNotFoundException("Loan not found with id: " + loanId));

        // -----------------------------------------------------
        // Cannot repay closed loan
        // -----------------------------------------------------

        if (loan.getStatus() == LoanStatus.CLOSED) {
            throw new BusinessException("Cannot make repayment for a closed loan");
        }

        // -----------------------------------------------------
        // Calculate current total paid
        // -----------------------------------------------------

        BigDecimal totalPaid = calculateTotalPaid(loanId);

        // -----------------------------------------------------
        // Calculate current outstanding
        // -----------------------------------------------------

        BigDecimal outstanding = loan.getTotalPayable().subtract(totalPaid);

        // -----------------------------------------------------
        // Prevent overpayment
        // -----------------------------------------------------

        if (request.amount().compareTo(outstanding) > 0) {
            throw new BusinessException("Repayment amount cannot exceed outstanding amount. " + "Outstanding: ₹" + outstanding + ", Requested: ₹" + request.amount());
        }

        // -----------------------------------------------------
        // Calculate interest already paid
        // -----------------------------------------------------

        BigDecimal interestAlreadyPaid = repaymentRepository.findByLoanId(loanId).stream().map(LoanRepayment::getInterestAmount).filter(amount -> amount != null).reduce(BigDecimal.ZERO, BigDecimal::add);

        // -----------------------------------------------------
        // Calculate remaining interest
        // -----------------------------------------------------

        BigDecimal remainingInterest = loan.getTotalInterest().subtract(interestAlreadyPaid);

        if (remainingInterest.compareTo(BigDecimal.ZERO) < 0) {

            remainingInterest = BigDecimal.ZERO;
        }

        // -----------------------------------------------------
        // Allocate repayment
        //
        // Interest is paid first.
        // Remaining amount goes to principal.
        // -----------------------------------------------------

        BigDecimal interestAmount = request.amount().min(remainingInterest);

        BigDecimal principalAmount = request.amount().subtract(interestAmount);

        // -----------------------------------------------------
        // Create repayment
        // -----------------------------------------------------

        LoanRepayment repayment = LoanRepayment.builder().loan(loan).amount(request.amount()).interestAmount(interestAmount).principalAmount(principalAmount).paymentMethod(request.paymentMethod()).repaymentDate(request.repaymentDate()).notes(request.notes()).build();

        LoanRepayment savedRepayment = repaymentRepository.save(repayment);

        // -----------------------------------------------------
        // Financial ledger
        // -----------------------------------------------------

        FinancialTransaction financialTransaction = FinancialTransaction.builder().transactionNumber(generateTransactionNumber()).type(TransactionType.LOAN_REPAYMENT).amount(request.amount()).transactionDate(request.repaymentDate()).description("Loan repayment - " + loan.getLoanNumber()).user(loan.getUser()).loan(loan).createdAt(LocalDateTime.now()).build();

        financialTransactionRepository.save(financialTransaction);

        // -----------------------------------------------------
        // Calculate new outstanding
        // -----------------------------------------------------

        BigDecimal newTotalPaid = totalPaid.add(request.amount());

        BigDecimal newOutstanding = loan.getTotalPayable().subtract(newTotalPaid);

        // -----------------------------------------------------
        // Update loan status
        // -----------------------------------------------------

        if (newOutstanding.compareTo(BigDecimal.ZERO) == 0) {

            loan.setStatus(LoanStatus.CLOSED);

        } else {

            loan.setStatus(LoanStatus.PARTIALLY_PAID);
        }

        return buildRepaymentResponse(savedRepayment);
    }

    // =========================================================
    // GET LOAN REPAYMENTS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<RepaymentResponse> getLoanRepayments(Long loanId) {

        if (!loanRepository.existsById(loanId)) {

            throw new ResourceNotFoundException("Loan not found with id: " + loanId);
        }

        return repaymentRepository.findByLoanId(loanId).stream().map(this::buildRepaymentResponse).toList();
    }


    // =========================================================
    // CALCULATE TOTAL PAID
    // =========================================================

    private BigDecimal calculateTotalPaid(Long loanId) {

        return repaymentRepository.findByLoanId(loanId).stream().map(LoanRepayment::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }


    // =========================================================
    // BUILD LOAN RESPONSE
    // =========================================================

    private LoanResponse buildLoanResponse(Loan loan) {

        BigDecimal totalPaid = calculateTotalPaid(loan.getId());

        BigDecimal outstanding = loan.getTotalPayable().subtract(totalPaid);


        return new LoanResponse(loan.getId(), loan.getLoanNumber(), loan.getUser().getId(), loan.getUser().getName(), loan.getPrincipalAmount(), loan.getInterestRate(), loan.getTotalInterest(), loan.getTotalPayable(), totalPaid, outstanding, loan.getStartDate(), loan.getDueDate(), loan.getStatus());
    }


    // =========================================================
    // BUILD REPAYMENT RESPONSE
    // =========================================================

    private RepaymentResponse buildRepaymentResponse(LoanRepayment repayment) {

        Loan loan = repayment.getLoan();

        User user = loan.getUser();


        return new RepaymentResponse(repayment.getId(), loan.getId(), loan.getLoanNumber(), user.getId(), user.getName(), repayment.getAmount(), repayment.getPrincipalAmount(), repayment.getInterestAmount(), repayment.getPaymentMethod(), repayment.getRepaymentDate(), repayment.getNotes());
    }


    // =========================================================
    // GENERATE LOAN NUMBER
    // =========================================================

    private String generateLoanNumber() {

        long count = loanRepository.count() + 1;

        return String.format("LN%06d", count);
    }


    // =========================================================
    // GENERATE TRANSACTION NUMBER
    // =========================================================

    private String generateTransactionNumber() {

        long count = financialTransactionRepository.count() + 1;

        return String.format("TXN%08d", count);
    }
}