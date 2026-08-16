package com.redhun.lendflow_api.service.security;



import com.redhun.lendflow_api.entity.RefreshToken;
import com.redhun.lendflow_api.entity.User;

public interface RefreshTokenService {

    RefreshToken create(
            User user
    );

    RefreshToken validate(
            String token
    );

    void revoke(
            RefreshToken token
    );

    void revokeAll(
            Long userId
    );
}