package com.redhun.lendflow_api.service.impl;

import com.redhun.lendflow_api.dto.auth.LoginRequest;
import com.redhun.lendflow_api.dto.auth.LoginResponse;
import com.redhun.lendflow_api.dto.auth.RefreshTokenRequest;
import com.redhun.lendflow_api.entity.RefreshToken;
import com.redhun.lendflow_api.entity.User;
import com.redhun.lendflow_api.exception.BusinessException;
import com.redhun.lendflow_api.repository.UserRepository;
import com.redhun.lendflow_api.service.AuthService;
import com.redhun.lendflow_api.service.security.JwtService;
import com.redhun.lendflow_api.service.security.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl
        implements AuthService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;

    private final RefreshTokenService
            refreshTokenService;


    // =====================================================
    // LOGIN
    // =====================================================

    @Override
    public LoginResponse login(
            LoginRequest request
    ) {

        User user =
                userRepository
                        .findByPhone(
                                request.mobileNumber()
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        "Invalid mobile number or password"
                                )
                        );


        if (!user.getActive()) {

            throw new BusinessException(
                    "User account is inactive"
            );
        }


        if (!passwordEncoder.matches(
                request.password(),
                user.getPassword()
        )) {

            throw new BusinessException(
                    "Invalid mobile number or password"
            );
        }


        // =================================================
        // ACCESS TOKEN
        // =================================================

        String accessToken =
                jwtService.generateToken(
                        user.getId(),
                        user.getPhone(),
                        user.getRole().name()
                );


        // =================================================
        // REFRESH TOKEN
        // =================================================

        RefreshToken refreshToken =
                refreshTokenService.create(
                        user
                );


        // =================================================
        // RESPONSE
        // =================================================

        return new LoginResponse(

                accessToken,

                refreshToken.getToken(),

                "Bearer",

                user.getId(),

                user.getName(),

                user.getPhone(),

                user.getRole().name()
        );
    }


    // =====================================================
    // REFRESH TOKEN
    // =====================================================

    @Override
    public LoginResponse refreshToken(
            RefreshTokenRequest request
    ) {

        // =================================================
        // VALIDATE OLD REFRESH TOKEN
        // =================================================

        RefreshToken oldToken =
                refreshTokenService.validate(
                        request.refreshToken()
                );


        User user =
                oldToken.getUser();


        // =================================================
        // ROTATE REFRESH TOKEN
        // =================================================

        refreshTokenService.revoke(
                oldToken
        );


        RefreshToken newRefreshToken =
                refreshTokenService.create(
                        user
                );


        // =================================================
        // NEW ACCESS TOKEN
        // =================================================

        String newAccessToken =
                jwtService.generateToken(
                        user.getId(),
                        user.getPhone(),
                        user.getRole().name()
                );


        // =================================================
        // RESPONSE
        // =================================================

        return new LoginResponse(

                newAccessToken,

                newRefreshToken.getToken(),

                "Bearer",

                user.getId(),

                user.getName(),

                user.getPhone(),

                user.getRole().name()
        );
    }


    // =====================================================
    // LOGOUT
    // =====================================================

    @Override
    public void logout(
            String refreshToken
    ) {

        RefreshToken token =
                refreshTokenService.validate(
                        refreshToken
                );


        refreshTokenService.revoke(
                token
        );
    }
}