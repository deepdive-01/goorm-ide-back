package com.ide.project.domain.files.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TestCaseCreateRequest(
    @NotBlank String input,
    @NotBlank String expectedOutput,
    boolean isHidden,
    @NotNull int orderNum
) {}