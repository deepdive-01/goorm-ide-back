package com.ide.project.domain.code.dto;

import java.util.List;

public record GradeResponse(
    String status,
    int passCount,
    int totalCount,
    List<TestCaseResult> results
) {
    public record TestCaseResult(
        boolean passed,
        String input,
        String expectedOutput,
        String actualOutput
    ) {}
}