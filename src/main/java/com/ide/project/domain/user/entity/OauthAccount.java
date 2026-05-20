package com.ide.project.domain.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "oauth_accounts", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"provider", "provider_id"}),
        @UniqueConstraint(columnNames = {"user_id", "provider"})
})
public class OauthAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 여러 소셜 로그인으로 하나의 계정으로 몰릴 수 있음, 즉 자동 연동
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private Provider provider;

    @Column(name = "provider_id", length = 255, nullable = false)
    private String providerId;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public OauthAccount(Long id, User user, Provider provider, String providerId, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.user = user;
        this.provider = provider;
        this.providerId = providerId;
    }


}
