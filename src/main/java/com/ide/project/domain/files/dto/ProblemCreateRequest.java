package com.ide.project.domain.files.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProblemCreateRequest(
    @NotNull Long spaceId,
    @NotNull Long createdBy,
    Long problemBankId,
    @NotBlank String title,
    @NotBlank String description,
    @NotBlank String difficulty,
    @NotBlank String language,
    String starterCode,
    boolean isPublished
) {}