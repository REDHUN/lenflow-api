package com.redhun.lendflow_api.service.impl;
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

    @Override
    public UserResponse createUser(CreateUserRequest request) {

        if (userRepository.existsByPhone(request.phone())) {
            throw new BusinessException(
                    "Phone number already exists"
            );
        }

        String userCode = generateUserCode();

        User user = userMapper.toEntity(request, userCode);

        User savedUser = userRepository.save(user);

        return userMapper.toResponse(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        return userMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {

        return userRepository.findAll()
                .stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Override
    public UserResponse updateUser(
            Long id,
            CreateUserRequest request
    ) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));


        userMapper.updateEntity(user, request);

        return userMapper.toResponse(user);
    }

    @Override

    public void deactivateUser(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with id: " + userId
                        )
                );

        if (!user.getActive()) {
            throw new BusinessException(
                    "User is already inactive"
            );
        }

        // Check active loans
        boolean hasActiveLoan =
                loanRepository.existsByUserIdAndStatusIn(
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

        // Check active deposits
        boolean hasActiveDeposit =
                depositRepository.existsByUserIdAndStatus(
                        userId,
                        DepositStatus.ACTIVE
                );

        if (hasActiveDeposit) {
            throw new BusinessException(
                    "Cannot deactivate user because the user "
                            + "has an active deposit"
            );
        }

        user.setActive(false);

        userRepository.save(user);
    }

    private String generateUserCode() {

        long count = userRepository.count() + 1;

        return String.format("USR%06d", count);
    }


}