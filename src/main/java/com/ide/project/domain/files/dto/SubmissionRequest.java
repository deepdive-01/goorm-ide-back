package com.ide.project.domain.files.dto;

import jakarta.validation.constraints.NotNull;

public record SubmissionRequest(
    @NotNull Long problemId,
    @NotNull Long userId,
    String savedCode,
    String submittedCode,
    boolean isFinalSubmit
) {}