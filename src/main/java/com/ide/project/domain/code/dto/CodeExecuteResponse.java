package com.ide.project.domain.code.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CodeExecuteResponse {
    private String output;    // 실행 결과 (stdout)
    private String stderr;    // 에러 출력
    private boolean isError;  // 에러 여부
}