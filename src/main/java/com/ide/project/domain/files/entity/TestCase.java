package com.ide.project.domain.files.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "testcases")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "problem_id")
    private Long problemId;

    @Column(name = "problem_bank_id")
    private Long problemBankId;

    @Column(columnDefinition = "TEXT", name = "input_case", nullable = false)
    private String input;

    @Column(columnDefinition = "TEXT", name = "output_case", nullable = false)
    private String expectedOutput;

    @Column(name = "is_example", nullable = false)
    private boolean isHidden;
}