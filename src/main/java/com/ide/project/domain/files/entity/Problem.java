package com.ide.project.domain.files.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter 
@Setter
@Table(name = "problems")
public class Problem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ERD의 space_id 연관관계 매핑 (N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "space_id", nullable = false)
    private Space space;

    // ERD의 problem_bank_id 연관관계 매핑 (N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_bank_id")
    private ProblemBank problemBank;

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

    @Column(name = "is_published", nullable = false)
    private boolean isPublished;
}