package com.ide.project.domain.files.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "problem_bank_testcases")
@Getter
@Setter 
public class TestCase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_bank_id")
    private ProblemBank problemBank;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id")
    private Problem problem;

    @Column(columnDefinition = "TEXT", name = "input", nullable = false)
    private String input;

    @Column(columnDefinition = "TEXT", name = "expected_output", nullable = false)
    private String expectedOutput;

    @Column(name = "is_hidden", nullable = false)
    private boolean isHidden;

    @Column(name = "order_num", nullable = false)
    private int orderNum;

  
}