package com.redhun.lendflow_api.controller;

import com.redhun.lendflow_api.dto.auth.LoginRequest;
import com.redhun.lendflow_api.dto.auth.LoginResponse;
import com.redhun.lendflow_api.dto.auth.RefreshTokenRequest;
import com.redhun.lendflow_api.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;


    // =====================================================
    // LOGIN
    // =====================================================

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {

        return ResponseEntity.ok(
                authService.login(request)
        );
    }


    // =====================================================
    // REFRESH
    // =====================================================

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(
            @Valid @RequestBody
            RefreshTokenRequest request
    ) {

        return ResponseEntity.ok(
                authService.refreshToken(
                        request
                )
        );
    }


    // =====================================================
    // LOGOUT
    // =====================================================

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @Valid @RequestBody
            RefreshTokenRequest request
    ) {

        authService.logout(
                request.refreshToken()
        );

        return ResponseEntity.noContent()
                .build();
    }
}