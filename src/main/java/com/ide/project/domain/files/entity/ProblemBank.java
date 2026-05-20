package com.ide.project.domain.files.entity;

import jakarta.persistence.*;

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

    @Column(length = 20)
    private String difficulty;

    @Column(length = 20)
    private String language;

    @Column(columnDefinition = "TEXT", name = "starter_code")
    private String starterCode;

    // 수동 생성된 표준 Getter 메서드들
    public Long getId() { return this.id; }
    public String getTitle() { return this.title; }
    public String getDescription() { return this.description; }
    public String getDifficulty() { return this.difficulty; }
    public String getLanguage() { return this.language; }
    public String getStarterCode() { return this.starterCode; }
}