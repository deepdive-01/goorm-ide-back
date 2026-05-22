package com.ide.project.domain.files.dto;

import jakarta.validation.constraints.NotBlank;

public record ProblemUpdateRequest(
    @NotBlank String title,
    @NotBlank String description,
    @NotBlank String difficulty,
    @NotBlank String language,
    String starterCode,
    boolean isPublished
) {}