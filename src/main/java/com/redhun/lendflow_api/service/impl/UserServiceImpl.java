package com.redhun.lendflow_api.service.impl;

import com.redhun.lendflow_api.dto.desposit.DepositResponse;
import com.redhun.lendflow_api.dto.loan.LoanResponse;
import com.redhun.lendflow_api.dto.user.CreateUserRequest;
import com.redhun.lendflow_api.dto.user.UserResponse;
import com.redhun.lendflow_api.entity.User;
import com.redhun.lendflow_api.enums.DepositStatus;
import com.redhun.lendflow_api.enums.LoanStatus;
import com.redhun.lendflow_api.exception.BusinessException;
import com.redhun.lendflow_api.exception.ResourceNotFoundException;
import com.redhun.lendflow_api.mapper.UserMapper;
import com.redhun.lendflow_api.repository.DepositRepository;
import com.redhun.lendflow_api.repository.LoanRepository;
import com.redhun.lendflow_api.repository.UserRepository;
import com.redhun.lendflow_api.service.DepositService;
import com.redhun.lendflow_api.service.LoanService;
import com.redhun.lendflow_api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final UserMapper userMapper;

    private final LoanRepository loanRepository;

    private final DepositRepository depositRepository;

    private final LoanService loanService;

    private final DepositService depositService;


    // =========================================================
    // CREATE USER
    // =========================================================

    @Override
    public UserResponse createUser(
            CreateUserRequest request
    ) {

        if (userRepository.existsByPhone(
                request.phone()
        )) {

            throw new BusinessException(
                    "Phone number already exists"
            );
        }


        String userCode =
                generateUserCode();


        User user =
                userMapper.toEntity(
                        request,
                        userCode
                );


        User savedUser =
                userRepository.save(user);


        /*
         * Return the complete user response.
         *
         * Newly created user will have:
         *
         * loans    = []
         * deposits = []
         */
        return buildUserResponse(
                savedUser
        );
    }


    // =========================================================
    // GET USER BY ID
    // =========================================================

    @Override
    @Transactional
    public UserResponse getUserById(
            Long id
    ) {

        User user =
                userRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User not found with id: "
                                                + id
                                )
                        );


        return buildUserResponse(
                user
        );
    }


    // =========================================================
    // GET ALL USERS
    // =========================================================

    @Override
    @Transactional
    public List<UserResponse> getAllUsers() {

        return userRepository.findAll()
                .stream()
                .map(this::buildUserResponse)
                .toList();
    }


    // =========================================================
    // UPDATE USER
    // =========================================================

    @Override
    public UserResponse updateUser(
            Long id,
            CreateUserRequest request
    ) {

        User user =
                userRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User not found with id: "
                                                + id
                                )
                        );


        /*
         * Your mapper updates the existing user.
         */
        userMapper.updateEntity(
                user,
                request
        );


        User updatedUser =
                userRepository.save(user);


        return buildUserResponse(
                updatedUser
        );
    }


    // =========================================================
    // DEACTIVATE USER
    // =========================================================

    @Override
    public void deactivateUser(
            Long userId
    ) {

        User user =
                userRepository.findById(userId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User not found with id: "
                                                + userId
                                )
                        );


        if (!user.getActive()) {

            throw new BusinessException(
                    "User is already inactive"
            );
        }


        // =====================================================
        // CHECK ACTIVE LOANS
        // =====================================================

        boolean hasActiveLoan =
                loanRepository
                        .existsByUserIdAndStatusIn(
                                userId,
                                List.of(
                                        LoanStatus.ACTIVE,
                                        LoanStatus.PARTIALLY_PAID
                                )
                        );


        if (hasActiveLoan) {

            throw new BusinessException(
                    "Cannot deactivate user because the user "
                            + "has an active or outstanding loan"
            );
        }


        // =====================================================
        // CHECK ACTIVE DEPOSITS
        // =====================================================

        boolean hasActiveDeposit =
                depositRepository
                        .existsByUserIdAndStatus(
                                userId,
                                DepositStatus.ACTIVE
                        );


        if (hasActiveDeposit) {

            throw new BusinessException(
                    "Cannot deactivate user because the user "
                            + "has an active deposit"
            );
        }


        // =====================================================
        // DEACTIVATE
        // =====================================================

        user.setActive(false);

        userRepository.save(user);
    }


    // =========================================================
    // BUILD COMPLETE USER RESPONSE
    // =========================================================

    private UserResponse buildUserResponse(
            User user
    ) {

        // =====================================================
        // LOANS
        // =====================================================

        /*
         * LoanService is responsible for:
         *
         * - daily interest calculation
         * - outstanding principal
         * - outstanding interest
         * - total outstanding
         *
         * So we don't duplicate that logic here.
         */

        List<LoanResponse> loans =
                loanService.getUserLoans(
                        user.getId()
                );


        // =====================================================
        // DEPOSITS
        // =====================================================

        /*
         * DepositService is responsible for
         * building DepositResponse.
         */

        List<DepositResponse> deposits =
                depositService.getUserDeposits(
                        user.getId()
                );


        // =====================================================
        // USER RESPONSE
        // =====================================================

        return new UserResponse(

                user.getId(),

                user.getUserCode(),

                user.getName(),

                user.getPhone(),

                user.getEmail(),

                user.getRole(),

                user.getActive(),

                loans,

                deposits
        );
    }


    // =========================================================
    // GENERATE USER CODE
    // =========================================================

    private String generateUserCode() {

        long count =
                userRepository.count() + 1;


        return String.format(
                "USR%06d",
                count
        );
    }
}