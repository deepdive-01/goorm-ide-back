package com.ide.project.domain.files.dto;

import com.ide.project.domain.files.entity.TestCase;

public record TestCaseResponse(
    Long id,
    String input,
    String expectedOutput,
    boolean isHidden,
    int orderNum
) {
    public static TestCaseResponse from(TestCase testCase) {
        return new TestCaseResponse(
            testCase.getId(),
            testCase.getInput(),
            testCase.getExpectedOutput(),
            testCase.isHidden(),
            testCase.getOrderNum()
        );
    }
}