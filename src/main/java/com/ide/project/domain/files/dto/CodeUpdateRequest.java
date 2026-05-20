package com.ide.project.domain.files.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;

@Schema(description = "학생 소스코드 수정 및 저장 요청 객체")
public class CodeUpdateRequest {

    @NotNull(message = "수정된 코드가 누락되었습니다.")
    @Schema(description = "수정된 최신 소스코드 본문", example = "public class Solution {\n    public int solution(int num1, int num2) {\n        return num1 + num2;\n    }\n}")
    private String modifiedCode;

    // Jackson 역직렬화용 기본 생성자
    public CodeUpdateRequest() {}

    public CodeUpdateRequest(String modifiedCode) {
        this.modifiedCode = modifiedCode;
    }

    public String getModifiedCode() {
        return modifiedCode;
    }
}