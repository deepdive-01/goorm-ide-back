package com.ide.project.domain.files.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "testcases")
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

    @Column(columnDefinition = "TEXT", name = "input_case", nullable = false)
    private String inputCase;

    @Column(columnDefinition = "TEXT", name = "output_case", nullable = false)
    private String outputCase;

    @Column(name = "is_example", nullable = false)
    private boolean isExample;

    // 수동 생성된 표준 Getter / Setter
    public Long getId() { return id; }
    
    public ProblemBank getProblemBank() { return problemBank; }
    public void setProblemBank(ProblemBank problemBank) { this.problemBank = problemBank; }

    public Problem getProblem() { return problem; }
    public void setProblem(Problem problem) { this.problem = problem; }

    public String getInputCase() { return inputCase; }
    public void setInputCase(String inputCase) { this.inputCase = inputCase; }

    public String getOutputCase() { return outputCase; }
    public void setOutputCase(String outputCase) { this.outputCase = outputCase; }

    public boolean isExample() { return isExample; }
    public void setExample(boolean isExample) { this.isExample = isExample; }
}