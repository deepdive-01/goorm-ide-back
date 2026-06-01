package com.ide.project.domain.files.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "problem_bank_testcases")
@Getter
@NoArgsConstructor
public class ProblemBankTestCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "problem_bank_id")
    private Long problemBankId;

    @Column(name = "problem_id")
    private Long problemId;

    @Column(columnDefinition = "TEXT", name = "input", nullable = false)
    private String input;

    @Column(columnDefinition = "TEXT", name = "expected_output", nullable = false)
    private String expectedOutput;

    @Column(name = "is_hidden", nullable = false)
    private boolean isHidden;

    @Column(name = "order_num", nullable = false)
    private int orderNum;
}