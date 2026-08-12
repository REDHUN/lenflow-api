package com.redhun.lendflow_api.dto.user;

import com.redhun.lendflow_api.enums.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateUserRequest(

        @NotBlank
        String name,

        @NotBlank
        String phone,

        String email,

        @NotBlank
        String password,

        @NotNull
        Role role
) {
}