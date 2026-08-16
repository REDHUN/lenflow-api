package com.redhun.lendflow_api.service.impl;

import com.redhun.lendflow_api.entity.RefreshToken;
import com.redhun.lendflow_api.entity.User;
import com.redhun.lendflow_api.exception.BusinessException;
import com.redhun.lendflow_api.repository.RefreshTokenRepository;
import com.redhun.lendflow_api.service.security.JwtService;
import com.redhun.lendflow_api.service.security.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class RefreshTokenServiceImpl
        implements RefreshTokenService {

    private final RefreshTokenRepository
            refreshTokenRepository;

    private final JwtService jwtService;


    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;


    // =====================================================
    // CREATE
    // =====================================================

    @Override
    public RefreshToken create(
            User user
    ) {

        String token =
                jwtService.generateRefreshToken();


        LocalDateTime expiresAt =
                LocalDateTime.now()
                        .plusNanos(
                                refreshExpiration * 1_000_000L
                        );

        RefreshToken refreshToken =
                RefreshToken.builder()

                        .token(token)

                        .user(user)

                        .expiresAt(
                                expiresAt
                        )

                        .revoked(false)

                        .build();


        return refreshTokenRepository.save(
                refreshToken
        );
    }


    // =====================================================
    // VALIDATE
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public RefreshToken validate(
            String token
    ) {

        RefreshToken refreshToken =
                refreshTokenRepository
                        .findByToken(token)
                        .orElseThrow(() ->
                                new BusinessException(
                                        "Invalid refresh token"
                                )
                        );


        if (refreshToken.isRevoked()) {

            throw new BusinessException(
                    "Refresh token has been revoked"
            );
        }


        if (refreshToken.getExpiresAt()
                .isBefore(
                        LocalDateTime.now()
                )) {

            throw new BusinessException(
                    "Refresh token has expired"
            );
        }


        if (!refreshToken.getUser()
                .getActive()) {

            throw new BusinessException(
                    "User account is inactive"
            );
        }


        return refreshToken;
    }


    // =====================================================
    // REVOKE
    // =====================================================

    @Override
    public void revoke(
            RefreshToken token
    ) {

        token.setRevoked(true);

        refreshTokenRepository.save(
                token
        );
    }


    // =====================================================
    // REVOKE ALL
    // =====================================================

    @Override
    public void revokeAll(
            Long userId
    ) {

        refreshTokenRepository
                .deleteAllByUserId(userId);
    }
}