package com.ide.project.domain.files.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "problem_bank")
public class ProblemBank {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(nullable = false, length = 20) // 1. DB의 NOT NULL 조건 반영
    private String difficulty;

    @Column(nullable = false, length = 20) // 2. DB의 NOT NULL 조건 반영
    private String language;

    @Column(columnDefinition = "TEXT", name = "starter_code")
    private String starterCode;

    @Column(name = "source_type", nullable = false, length = 50) // 3. 새로 추가된 컬럼 매핑
    private String sourceType = "ORIGINAL"; // DB의 DEFAULT 값과 일치화

    @Column(name = "source_url", length = 500) // 4. 새로 추가된 컬럼 매핑
    private String sourceUrl;

    @Column(name = "is_active", nullable = false) // 5. 새로 추가된 컬럼 매핑
    private boolean isActive = true; // DB의 DEFAULT 값과 일치화

    @Column(name = "created_at", nullable = false, updatable = false) // 6. 생성일자 추가
    private LocalDateTime createdAt;

    // JPA 엔티티가 영속화(저장)되기 직전에 현재 시간으로 자동 세팅 (DB의 DEFAULT NOW() 역할)
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    // ================= 수동 생성된 표준 Getter / Setter =================
    
    public Long getId() { return this.id; }
    
    public String getTitle() { return this.title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return this.description; }
    public void setDescription(String description) { this.description = description; }

    public String getDifficulty() { return this.difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public String getLanguage() { return this.language; }
    public void setLanguage(String language) { this.language = language; }

    public String getStarterCode() { return this.starterCode; }
    public void setStarterCode(String starterCode) { this.starterCode = starterCode; }

    public String getSourceType() { return this.sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }

    public String getSourceUrl() { return this.sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }

    public boolean isActive() { return this.isActive; }
    public void setActive(boolean active) { this.isActive = active; }

    public LocalDateTime getCreatedAt() { return this.createdAt; }
}