package com.ide.project.domain.files.dto;

import jakarta.validation.constraints.NotNull;

public record CodeUpdateRequest(
    @NotNull Long problemId,
    @NotNull Long userId,
    @NotNull String savedCode  // 코드 수정은 임시저장 코드만 업데이트
) {}