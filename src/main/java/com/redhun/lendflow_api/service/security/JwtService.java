package com.redhun.lendflow_api.service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    private final SecretKey secretKey;

    private final long expiration;


    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expiration
    ) {

        this.secretKey =
                Keys.hmacShaKeyFor(
                        secret.getBytes(
                                StandardCharsets.UTF_8
                        )
                );

        this.expiration = expiration;
    }


    // =====================================================
    // ACCESS TOKEN
    // =====================================================

    public String generateToken(
            Long userId,
            String phone,
            String role
    ) {

        Date now =
                new Date();

        Date expiry =
                new Date(
                        now.getTime()
                                + expiration
                );


        return Jwts.builder()

                .subject(phone)

                .claim(
                        "userId",
                        userId
                )

                .claim(
                        "role",
                        role
                )

                .issuedAt(now)

                .expiration(expiry)

                .signWith(secretKey)

                .compact();
    }


    // =====================================================
    // EXTRACT PHONE
    // =====================================================

    public String extractPhone(
            String token
    ) {

        return getClaims(token)
                .getSubject();
    }


    // =====================================================
    // EXTRACT USER ID
    // =====================================================

    public Long extractUserId(
            String token
    ) {

        return getClaims(token)
                .get(
                        "userId",
                        Long.class
                );
    }


    // =====================================================
    // EXTRACT ROLE
    // =====================================================

    public String extractRole(
            String token
    ) {

        return getClaims(token)
                .get(
                        "role",
                        String.class
                );
    }


    // =====================================================
    // VALIDATE TOKEN
    // =====================================================

    public boolean isTokenValid(
            String token
    ) {

        try {

            getClaims(token);

            return true;

        } catch (Exception e) {

            return false;
        }
    }


    // =====================================================
    // GENERATE RANDOM REFRESH TOKEN
    // =====================================================

    public String generateRefreshToken() {

        return UUID.randomUUID()
                .toString()
                .replace(
                        "-",
                        ""
                );
    }


    // =====================================================
    // GET CLAIMS
    // =====================================================

    private Claims getClaims(
            String token
    ) {

        return Jwts.parser()

                .verifyWith(
                        secretKey
                )

                .build()

                .parseSignedClaims(
                        token
                )

                .getPayload();
    }
}