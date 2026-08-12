package com.redhun.lendflow_api.dto.user;


import com.redhun.lendflow_api.enums.Role;

public record UserResponse(

        Long id,
        String userCode,
        String name,
        String phone,
        String email,
        Role role,
        Boolean active
) {
}