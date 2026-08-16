package com.redhun.lendflow_api.service;


import com.redhun.lendflow_api.entity.User;
import com.redhun.lendflow_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.lang.NonNull;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService
        implements UserDetailsService {

    private final UserRepository userRepository;


    @Override
    public UserDetails loadUserByUsername(
            @NonNull String mobileNumber
    ) throws UsernameNotFoundException {

        User user =
                userRepository
                        .findByPhone(mobileNumber)
                        .orElseThrow(() ->
                                new UsernameNotFoundException(
                                        "User not found"
                                )
                        );

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getPhone())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .disabled(!user.getActive())
                .build();
    }
}