package com.redhun.lendflow_api.dto.auth;


import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @NotBlank
        String mobileNumber,

        @NotBlank
        String password
) {
}