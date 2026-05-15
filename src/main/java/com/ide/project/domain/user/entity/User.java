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

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class) // 리스너가 있어야, 엔티티의 저장/수정 이벤트를 감지하고 시간을 채워줄 수 있음
public class User {
    @Id // PK
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 자동으로 늘려줌
    private Long id;

    @Column(length = 100, unique = true)
    private String email;

    @Column(length = 255)
    private String password;

    @Column(length = 50, nullable = false)
    private String name;

    @Column(length = 50, nullable = false)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private Role role;

    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "login_type", length = 20, nullable = false)
    private LoginType loginType;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @CreatedDate // 처음 저장될 때 시간을 채워줌
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate // 업데이트될때마다 갱신
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // 생성자 역할
    @Builder
    public User(String email, String password, String name, String nickname, Role role, String profileImageUrl, LoginType loginType) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.nickname = nickname;
        this.role = role;
        this.profileImageUrl = profileImageUrl;
        this.loginType = loginType;
        this.emailVerified = false;
        this.active = true;
    }
}
