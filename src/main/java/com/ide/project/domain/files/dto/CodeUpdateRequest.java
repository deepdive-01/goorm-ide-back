package com.ide.project.domain.files.dto;

import jakarta.validation.constraints.NotNull;

public record CodeUpdateRequest(
    @NotNull Long problemId,
    @NotNull Long studentId,
    @NotNull String savedCode
) {}