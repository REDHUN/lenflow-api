package com.redhun.lendflow_api.service.impl;

import com.redhun.lendflow_api.dto.desposit.AddDepositMoneyRequest;
import com.redhun.lendflow_api.dto.desposit.CreateDepositRequest;
import com.redhun.lendflow_api.dto.desposit.DepositResponse;
import com.redhun.lendflow_api.entity.Deposit;
import com.redhun.lendflow_api.entity.DepositTransaction;
import com.redhun.lendflow_api.entity.FinancialTransaction;
import com.redhun.lendflow_api.entity.User;
import com.redhun.lendflow_api.enums.DepositStatus;
import com.redhun.lendflow_api.enums.TransactionType;
import com.redhun.lendflow_api.exception.BusinessException;
import com.redhun.lendflow_api.exception.ResourceNotFoundException;
import com.redhun.lendflow_api.mapper.DepositMapper;
import com.redhun.lendflow_api.mapper.DepositTransactionMapper;
import com.redhun.lendflow_api.repository.DepositRepository;
import com.redhun.lendflow_api.repository.DepositTransactionRepository;
import com.redhun.lendflow_api.repository.FinancialTransactionRepository;
import com.redhun.lendflow_api.repository.UserRepository;
import com.redhun.lendflow_api.service.DepositService;
import com.redhun.lendflow_api.service.FinancialTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DepositServiceImpl implements DepositService {

    private final DepositRepository depositRepository;
    private final DepositTransactionRepository transactionRepository;
    private final UserRepository userRepository;

    private final DepositMapper depositMapper;
    private final DepositTransactionMapper depositTransactionMapper;

    private final FinancialTransactionRepository
            financialTransactionRepository;

    private final FinancialTransactionService
            financialTransactionService;


    // =========================================================
    // CREATE DEPOSIT
    // =========================================================

    @Override
    public DepositResponse createDeposit(
            CreateDepositRequest request
    ) {

        User user = userRepository.findById(request.userId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with id: "
                                        + request.userId()
                        )
                );

        Deposit deposit = depositMapper.toEntity(
                request,
                user,
                generateDepositNumber()
        );

        Deposit savedDeposit =
                depositRepository.save(deposit);


        // -----------------------------------------------------
        // Create deposit transaction
        // -----------------------------------------------------

        DepositTransaction depositTransaction =
                depositTransactionMapper.toInitialTransaction(
                        request,
                        savedDeposit
                );

        transactionRepository.save(depositTransaction);


        // -----------------------------------------------------
        // Create financial ledger transaction
        // -----------------------------------------------------

        FinancialTransaction financialTransaction =
                FinancialTransaction.builder()
                        .transactionNumber(
                                generateTransactionNumber()
                        )
                        .type(
                                TransactionType.DEPOSIT_RECEIVED
                        )
                        .amount(
                                request.initialAmount()
                        )
                        .transactionDate(
                                request.startDate()
                        )
                        .description(
                                "Initial deposit - "
                                        + savedDeposit
                                        .getDepositNumber()
                        )
                        .user(user)
                        .deposit(savedDeposit)
                        .createdAt(LocalDateTime.now())
                        .build();

        financialTransactionRepository.save(
                financialTransaction
        );

        return buildResponse(savedDeposit);
    }


    // =========================================================
    // GET DEPOSIT
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public DepositResponse getDeposit(Long depositId) {

        Deposit deposit =
                depositRepository.findById(depositId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Deposit not found with id: "
                                                + depositId
                                )
                        );

        return buildResponse(deposit);
    }


    // =========================================================
    // GET USER DEPOSITS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<DepositResponse> getUserDeposits(
            Long userId
    ) {

        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException(
                    "User not found with id: " + userId
            );
        }

        return depositRepository.findByUserId(userId)
                .stream()
                .map(this::buildResponse)
                .toList();
    }


    // =========================================================
    // ADD MONEY TO DEPOSIT
    // =========================================================

    @Override
    public DepositResponse addMoney(
            Long depositId,
            AddDepositMoneyRequest request
    ) {

        Deposit deposit =
                depositRepository.findById(depositId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Deposit not found with id: "
                                                + depositId
                                )
                        );


        // -----------------------------------------------------
        // Cannot add money to closed deposit
        // -----------------------------------------------------

        if (deposit.getStatus() == DepositStatus.CLOSED) {

            throw new BusinessException(
                    "Cannot add money to a closed deposit"
            );
        }


        // -----------------------------------------------------
        // Create deposit transaction
        // -----------------------------------------------------

        DepositTransaction depositTransaction =
                depositTransactionMapper.toEntity(
                        request,
                        deposit
                );

        transactionRepository.save(
                depositTransaction
        );


        // -----------------------------------------------------
        // Create financial ledger transaction
        // -----------------------------------------------------

        FinancialTransaction financialTransaction =
                FinancialTransaction.builder()
                        .transactionNumber(
                                generateTransactionNumber()
                        )
                        .type(
                                TransactionType.DEPOSIT_RECEIVED
                        )
                        .amount(
                                request.amount()
                        )
                        .transactionDate(
                                request.transactionDate()
                        )
                        .description(
                                "Additional deposit - "
                                        + deposit
                                        .getDepositNumber()
                        )
                        .user(deposit.getUser())
                        .deposit(deposit)
                        .createdAt(LocalDateTime.now())
                        .build();

        financialTransactionRepository.save(
                financialTransaction
        );

        return buildResponse(deposit);
    }


    // =========================================================
    // CLOSE DEPOSIT
    // =========================================================

    @Override
    public DepositResponse closeDeposit(
            Long depositId
    ) {

        Deposit deposit =
                depositRepository.findById(depositId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Deposit not found with id: "
                                                + depositId
                                )
                        );


        // -----------------------------------------------------
        // Already closed
        // -----------------------------------------------------

        if (deposit.getStatus() == DepositStatus.CLOSED) {

            throw new BusinessException(
                    "Deposit is already closed"
            );
        }


        // -----------------------------------------------------
        // Calculate total amount deposited
        // -----------------------------------------------------

        BigDecimal totalAmount =
                calculateTotalDeposit(deposit);


        // -----------------------------------------------------
        // Calculate fixed deposit interest
        //
        // Example:
        //
        // ₹120,000 × 5%
        // = ₹6,000
        // -----------------------------------------------------

        BigDecimal interest =
                totalAmount
                        .multiply(
                                deposit.getInterestRate()
                        )
                        .divide(
                                BigDecimal.valueOf(100),
                                2,
                                RoundingMode.HALF_UP
                        );


        // -----------------------------------------------------
        // Calculate amount that must be paid to customer
        // -----------------------------------------------------

        BigDecimal closingAmount =
                totalAmount.add(interest);


        // -----------------------------------------------------
        // CHECK GLOBAL AVAILABLE BALANCE
        // -----------------------------------------------------

        BigDecimal availableBalance =
                financialTransactionService
                        .getAvailableBalance();


        if (availableBalance.compareTo(
                closingAmount
        ) < 0) {

            throw new BusinessException(
                    "Insufficient available balance to close "
                            + "deposit. Required: ₹"
                            + closingAmount
                            + ", Available: ₹"
                            + availableBalance
            );
        }


        // -----------------------------------------------------
        // Update deposit
        // -----------------------------------------------------

        deposit.setInterestAmount(interest);

        deposit.setClosingAmount(closingAmount);

        deposit.setClosedDate(
                LocalDate.now()
        );

        deposit.setStatus(
                DepositStatus.CLOSED
        );


        // -----------------------------------------------------
        // Create financial transaction for money paid
        // to customer.
        // -----------------------------------------------------

        FinancialTransaction financialTransaction =
                FinancialTransaction.builder()
                        .transactionNumber(
                                generateTransactionNumber()
                        )
                        .type(
                                TransactionType.DEPOSIT_CLOSURE
                        )
                        .amount(closingAmount)
                        .transactionDate(
                                LocalDate.now()
                        )
                        .description(
                                "Deposit closure - "
                                        + deposit
                                        .getDepositNumber()
                        )
                        .user(deposit.getUser())
                        .deposit(deposit)
                        .createdAt(LocalDateTime.now())
                        .build();

        financialTransactionRepository.save(
                financialTransaction
        );


        return buildResponse(deposit);
    }


    // =========================================================
    // CALCULATE TOTAL DEPOSIT
    // =========================================================

    private BigDecimal calculateTotalDeposit(
            Deposit deposit
    ) {

        return transactionRepository
                .findByDepositId(deposit.getId())
                .stream()
                .map(DepositTransaction::getAmount)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );
    }


    // =========================================================
    // BUILD RESPONSE
    // =========================================================

    private DepositResponse buildResponse(
            Deposit deposit
    ) {

        BigDecimal totalAmount =
                calculateTotalDeposit(deposit);

        return depositMapper.toResponse(
                deposit,
                totalAmount
        );
    }


    // =========================================================
    // GENERATE DEPOSIT NUMBER
    // =========================================================

    private String generateDepositNumber() {

        long count =
                depositRepository.count() + 1;

        return String.format(
                "DEP%06d",
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
}