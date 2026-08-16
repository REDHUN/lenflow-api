package com.redhun.lendflow_api.dto.auth;

public record LoginResponse(

        String accessToken,

        String refreshToken,

        String tokenType,

        Long userId,

        String name,

        String username,

        String role

) {
}