package com.ide.project.domain.code.dto;

public record GradeRequest(
    Long problemId,
    String language,
    String code
) {}