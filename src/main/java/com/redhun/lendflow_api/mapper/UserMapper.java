package com.redhun.lendflow_api.mapper;

import com.redhun.lendflow_api.dto.user.CreateUserRequest;
import com.redhun.lendflow_api.dto.user.UserResponse;
import com.redhun.lendflow_api.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {

        return new UserResponse(
                user.getId(),
                user.getUserCode(),
                user.getName(),
                user.getPhone(),
                user.getEmail(),
                user.getRole(),
                user.getActive()
        );
    }

    public User toEntity(CreateUserRequest request, String userCode) {

        return User.builder()
                .userCode(userCode)
                .name(request.name())
                .phone(request.phone())
                .email(request.email())
                .password(request.password())
                .role(request.role())
                .active(true)
                .build();
    }
    public void updateEntity(User user, CreateUserRequest request) {
        user.setName(request.name());
        user.setPhone(request.phone());
        user.setEmail(request.email());
        user.setRole(request.role());
    }

}
