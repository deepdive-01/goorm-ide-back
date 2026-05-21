package com.ide.project.domain.files.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "problem_bank_testcases")
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

    @Column(columnDefinition = "TEXT", name = "input", nullable = false) // 2. input_case -> input
    private String input;

    @Column(columnDefinition = "TEXT", name = "expected_output", nullable = false) // 3. output_case -> expected_output
    private String expectedOutput;

    @Column(name = "is_hidden", nullable = false) // 4. is_example -> is_hidden
    private boolean isHidden;

    @Column(name = "order_num", nullable = false) // 5. DB에 정의된 order_num 컬럼 추가
    private int orderNum;

    // ================= 수동 생성된 표준 Getter / Setter =================
    
    public Long getId() { return id; }
    
    public ProblemBank getProblemBank() { return problemBank; }
    public void setProblemBank(ProblemBank problemBank) { this.problemBank = problemBank; }

    public Problem getProblem() { return problem; }
    public void setProblem(Problem problem) { this.problem = problem; }

    public String getInput() { return input; }
    public void setInput(String input) { this.input = input; }

    public String getExpectedOutput() { return expectedOutput; }
    public void setExpectedOutput(String expectedOutput) { this.expectedOutput = expectedOutput; }

    public boolean isHidden() { return isHidden; }
    public void setHidden(boolean hidden) { this.isHidden = hidden; }

    public int getOrderNum() { return orderNum; }
    public void setOrderNum(int orderNum) { this.orderNum = orderNum; }
}