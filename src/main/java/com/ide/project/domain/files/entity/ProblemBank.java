package com.ide.project.domain.files.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "problem_bank")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProblemBank {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(nullable = false, length = 20) // DB의 NOT NULL 조건 반영
    private String difficulty;

    @Column(nullable = false, length = 20) // DB의 NOT NULL 조건 반영
    private String language;

    @Column(columnDefinition = "TEXT", name = "starter_code")
    private String starterCode;

    @Column(name = "source_type", nullable = false, length = 50) // 새로 추가된 컬럼 매핑
    private String sourceType = "ORIGINAL"; // DB의 DEFAULT 값과 일치화

    @Column(name = "source_url", length = 500) // 새로 추가된 컬럼 매핑
    private String sourceUrl;

    @Column(name = "is_active", nullable = false) // 새로 추가된 컬럼 매핑
    private boolean isActive = true; // DB의 DEFAULT 값과 일치화

    @Column(name = "created_at", nullable = false, updatable = false) // 생성일자 추가
    private LocalDateTime createdAt;

    // JPA 엔티티가 영속화(저장)되기 직전에 현재 시간으로 자동 세팅 (DB의 DEFAULT NOW() 역할)
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }


}