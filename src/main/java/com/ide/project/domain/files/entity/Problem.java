package com.ide.project.domain.files.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @Column(name = "problem_bank_id")
    private Long problemBankId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(length = 20)
    private String difficulty;

    @Column(name = "starter_code", columnDefinition = "TEXT")
    private String starterCode;

    @Column(name = "creator_id", nullable = false)
    private Long creatorId;

    // 문제의 전체 지문 및 정보 수정을 위한 비즈니스 메서드 (JPA Dirty Checking 적용)
    public void updateDetails(String title, String description, String difficulty, String starterCode) {
        this.title = title;
        this.description = description;
        this.difficulty = difficulty;
        this.starterCode = starterCode;
    }

    // 학생이 에디터에서 코드를 수정하여 임시 저장할 때 사용하는 메서드
    public void updateStarterCode(String starterCode) {
        this.starterCode = starterCode;
    }
}