package com.ide.project.domain.files.dto;

import com.ide.project.domain.files.entity.TestCase;

public record TestCaseResponse(
    Long id,
    String inputCase,
    String outputCase,
    boolean isExample
) {
    public static TestCaseResponse from(TestCase testCase) {
        return new TestCaseResponse(
            testCase.getId(),
            testCase.getInput(),
            testCase.getExpectedOutput(),
            testCase.isHidden()
        );
    }
}