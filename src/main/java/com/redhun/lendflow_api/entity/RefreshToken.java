package com.redhun.lendflow_api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "refresh_tokens",
        indexes = {
                @Index(
                        name = "idx_refresh_token",
                        columnList = "token",
                        unique = true
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // =====================================================
    // TOKEN
    // =====================================================

    @Column(
            nullable = false,
            unique = true,
            length = 100
    )
    private String token;


    // =====================================================
    // USER
    // =====================================================

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private User user;


    // =====================================================
    // EXPIRATION
    // =====================================================

    @Column(nullable = false)
    private LocalDateTime expiresAt;


    // =====================================================
    // REVOKED
    // =====================================================

    @Column(nullable = false)
    private boolean revoked;


    // =====================================================
    // CREATED
    // =====================================================

    @Column(nullable = false)
    private LocalDateTime createdAt;


    @PrePersist
    protected void onCreate() {

        createdAt =
                LocalDateTime.now();

        revoked = false;
    }
}