package com.ide.project.domain.files.dto;

import jakarta.validation.constraints.NotNull;

public record ProblemAssignRequest(
    @NotNull Long spaceId,
    @NotNull Long problemBankId,
    @NotNull Long createdBy // 🌟 복사된 문제가 저장될 때 created_by(NOT NULL)를 채우기 위해 추가
) {}