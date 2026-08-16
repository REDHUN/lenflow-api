package com.redhun.lendflow_api.controller;
import com.redhun.lendflow_api.dto.user.CreateUserRequest;
import com.redhun.lendflow_api.dto.user.UserResponse;
import com.redhun.lendflow_api.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // Create user
    @PreAuthorize("hasAnyRole('ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createUser(
            @Valid @RequestBody CreateUserRequest request
    ) {
        return userService.createUser(request);
    }

    // Get all users
    @PreAuthorize("hasAnyRole('ADMIN', 'MEMBER')")
    @GetMapping
    public List<UserResponse> getAllUsers() {
        return userService.getAllUsers();
    }

    // Get user by ID
    @PreAuthorize("hasAnyRole('ADMIN', 'MEMBER')")
    @GetMapping("/{id}")
    public UserResponse getUserById(
            @PathVariable Long id
    ) {
        return userService.getUserById(id);
    }

    // Update user
    @PreAuthorize("hasAnyRole('ADMIN')")
    @PutMapping("/{id}")
    public UserResponse updateUser(
            @PathVariable Long id,
            @Valid @RequestBody CreateUserRequest request
    ) {
        return userService.updateUser(id, request);
    }

    // Deactivate user
    @PreAuthorize("hasAnyRole('ADMIN')")
    @PatchMapping("/{id}/deactivate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivateUser(
            @PathVariable Long id
    ) {
        userService.deactivateUser(id);
    }
}