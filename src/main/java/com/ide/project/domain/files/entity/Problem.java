package com.ide.project.domain.files.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "problems")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Problem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "space_id", nullable = false)
    private Long spaceId;

    @Column(name = "created_by", nullable = false) // 1. creator_id에서 DB 컬럼명인 created_by로 변경
    private Long createdBy;

    @Column(name = "problem_bank_id")
    private Long problemBankId;

    @Column(nullable = false, length = 200) // 2. DB의 VARCHAR(200) 반영
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(nullable = false, length = 20) // 3. DB의 NOT NULL 반영
    private String difficulty;

    @Column(nullable = false, length = 20) // 4. 자바 코드에 누락되었던 언어(language) 필드 추가
    private String language;

    @Column(name = "starter_code", columnDefinition = "TEXT")
    private String starterCode;

    @Column(name = "is_published", nullable = false) // 5. 자바 코드에 누락되었던 공개 여부 필드 추가
    @Builder.Default
    private boolean isPublished = false;

    @Column(name = "created_at", nullable = false, updatable = false) // 6. 생성 시간 추가
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false) // 7. 수정 시간 추가
    private LocalDateTime updatedAt;

    // 엔티티가 처음 저장될 때 시간 자동 입력
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // 엔티티가 수정될 때마다 수정 시간 자동 갱신
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ================= 비즈니스 메서드 (새로운 필드 반영) =================

    // 문제의 전체 지문 및 정보 수정을 위한 메서드
    public void updateDetails(String title, String description, String difficulty, String language, String starterCode, boolean isPublished) {
        this.title = title;
        this.description = description;
        this.difficulty = difficulty;
        this.language = language; // 언어 수정 반영
        this.starterCode = starterCode;
        this.isPublished = isPublished; // 공개 여부 수정 반영
    }

    // 학생이 에디터에서 코드를 수정하여 임시 저장할 때 사용하는 메서드
    public void updateStarterCode(String starterCode) {
        this.starterCode = starterCode;
    }
}