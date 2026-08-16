package com.redhun.lendflow_api.security;

import com.redhun.lendflow_api.service.CustomUserDetailsService;
import com.redhun.lendflow_api.service.security.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter
        extends OncePerRequestFilter {

    private final JwtService jwtService;

    private final CustomUserDetailsService userDetailsService;


    // =========================================================
    // SKIP JWT FILTER FOR AUTH ENDPOINTS
    // =========================================================

    @Override
    protected boolean shouldNotFilter(
            HttpServletRequest request
    ) {

        String path =
                request.getServletPath();

        return path.equals("/api/auth/login")
                || path.equals("/api/auth/refresh")
                || path.equals("/api/auth/logout");
    }


    // =========================================================
    // JWT FILTER
    // =========================================================

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {


        // =====================================================
        // GET AUTHORIZATION HEADER
        // =====================================================

        String authorizationHeader =
                request.getHeader("Authorization");


        // =====================================================
        // NO TOKEN
        // =====================================================

        if (authorizationHeader == null
                || !authorizationHeader.startsWith(
                "Bearer "
        )) {

            filterChain.doFilter(
                    request,
                    response
            );

            return;
        }


        // =====================================================
        // EXTRACT TOKEN
        // =====================================================

        String token =
                authorizationHeader.substring(7);


        try {

            // =================================================
            // EXTRACT PHONE
            // =================================================

            String phone =
                    jwtService.extractPhone(token);


            if (phone == null
                    || phone.isBlank()) {

                sendUnauthorized(
                        response,
                        "Invalid access token"
                );

                return;
            }


            // =================================================
            // CHECK AUTHENTICATION
            // =================================================

            if (SecurityContextHolder
                    .getContext()
                    .getAuthentication()
                    == null) {


                // =============================================
                // LOAD USER
                // =============================================

                UserDetails userDetails =
                        userDetailsService
                                .loadUserByUsername(
                                        phone
                                );


                // =============================================
                // VALIDATE TOKEN
                // =============================================

                if (!jwtService.isTokenValid(
                        token
                )) {

                    sendUnauthorized(
                            response,
                            "Invalid access token"
                    );

                    return;
                }


                // =============================================
                // CREATE AUTHENTICATION
                // =============================================

                UsernamePasswordAuthenticationToken
                        authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );


                authentication.setDetails(
                        new WebAuthenticationDetailsSource()
                                .buildDetails(request)
                );


                SecurityContextHolder
                        .getContext()
                        .setAuthentication(
                                authentication
                        );
            }


            // =================================================
            // CONTINUE
            // =================================================

            filterChain.doFilter(
                    request,
                    response
            );


        } catch (ExpiredJwtException e) {

            // =================================================
            // EXPIRED TOKEN
            // =================================================

            sendUnauthorized(
                    response,
                    "Access token has expired"
            );


        } catch (JwtException e) {

            // =================================================
            // INVALID JWT
            // =================================================

            sendUnauthorized(
                    response,
                    "Invalid access token"
            );


        } catch (Exception e) {

            // =================================================
            // AUTHENTICATION ERROR
            // =================================================

            sendUnauthorized(
                    response,
                    "Authentication failed"
            );
        }
    }


    // =========================================================
    // SEND 401 RESPONSE
    // =========================================================

    private void sendUnauthorized(
            HttpServletResponse response,
            String message
    ) throws IOException {

        response.setStatus(
                HttpServletResponse.SC_UNAUTHORIZED
        );

        response.setContentType(
                "application/json"
        );

        response.setCharacterEncoding(
                "UTF-8"
        );

        response.getWriter().write(
                """
                {
                    "status": 401,
                    "error": "UNAUTHORIZED",
                    "message": "%s"
                }
                """.formatted(message)
        );
    }
}