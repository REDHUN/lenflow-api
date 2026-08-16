package com.redhun.lendflow_api.service;

import com.redhun.lendflow_api.dto.auth.LoginRequest;
import com.redhun.lendflow_api.dto.auth.LoginResponse;
import com.redhun.lendflow_api.dto.auth.RefreshTokenRequest;

public interface AuthService {

    LoginResponse login(
            LoginRequest request
    );

    LoginResponse refreshToken(
            RefreshTokenRequest request
    );

    void logout(
            String refreshToken
    );
}