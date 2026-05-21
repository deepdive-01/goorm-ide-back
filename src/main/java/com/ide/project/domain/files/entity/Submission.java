package com.ide.project.domain.files.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "submissions",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_problem_user", columnNames = {"problem_id", "user_id"})
    }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "problem_id", nullable = false)
    private Long problemId;

    @Column(name = "user_id", nullable = false) // DDL에 맞추어 student_id에서 user_id로 변경
    private Long userId;

    @Column(name = "saved_code", columnDefinition = "TEXT")
    private String savedCode;

    @Column(name = "submitted_code", columnDefinition = "TEXT")
    private String submittedCode;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "PENDING"; // PENDING, SUCCESS, FAIL, ERROR

    @Column(name = "execution_time_ms")
    private Integer executionTimeMs;

    @Column(name = "execution_memory_kb")
    private Integer executionMemoryKb;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // 코드 임시 저장 및 제출 갱신 비즈니스 메서드
    public void updateSubmission(String savedCode, String submittedCode, String status) {
        if (savedCode != null) this.savedCode = savedCode;
        if (submittedCode != null) this.submittedCode = submittedCode;
        if (status != null) this.status = status;
    }
}