package com.ide.project.domain.files.dto;

import jakarta.validation.constraints.NotNull;

public record ProblemAssignRequest(
    @NotNull Long spaceId,
    @NotNull Long problemBankId,
    @NotNull Long createdBy
) {}