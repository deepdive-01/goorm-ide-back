package com.ide.project.domain.files.dto;

import jakarta.validation.constraints.NotNull;

public record CodeUpdateRequest(
    @NotNull Long problemId,
    @NotNull Long userId,
    String savedCode, // 임시 저장용
    String submittedCode, // 최종 제출용
    boolean isFinalSubmit // 단순 저장인지, 채점 요청인지 구분
) {}