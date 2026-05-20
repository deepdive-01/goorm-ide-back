package com.ide.project.domain.files.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "submissions")
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

    @Column(name = "student_id", nullable = false)
    private Long studentId; // 인증 객체 등에서 받아올 학생 식별자

    @Column(name = "submitted_code", columnDefinition = "TEXT", nullable = false)
    private String submittedCode;

    @Column(length = 20)
    private String status; // 예: PENDING(채점대기), SUCCESS(성공), FAIL(실패)

    // 기존에 제출한 코드를 최신 코드로 덮어쓰기 위한 비즈니스 메서드 (Dirty Checking용)
    public void updateCode(String newCode) {
        this.submittedCode = newCode;
        this.status = "PENDING"; // 재제출 시 채점 상태를 대기로 초기화
    }
}