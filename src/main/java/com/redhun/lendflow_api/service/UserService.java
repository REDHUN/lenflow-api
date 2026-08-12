package com.redhun.lendflow_api.service;

import com.redhun.lendflow_api.dto.user.CreateUserRequest;
import com.redhun.lendflow_api.dto.user.UserResponse;

import java.util.List;

public interface UserService {

    UserResponse createUser(CreateUserRequest request);

    UserResponse getUserById(Long id);

    List<UserResponse> getAllUsers();

    UserResponse updateUser(Long id, CreateUserRequest request);

    void deactivateUser(Long id);
}
