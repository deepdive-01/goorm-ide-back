package com.ide.project.domain.files.dto;

import jakarta.validation.constraints.NotNull;

public record TestCaseCreateRequest(
    @NotNull String inputCase,
    @NotNull String outputCase,
    boolean isExample
) {}