package com.ide.project.domain.files.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "학생 제출 코드 수정 및 재제출 요청 객체")
public class SubmissionUpdateRequest {

    @Schema(description = "재제출할 최신 소스코드 본문", example = "public class Solution {\n    public int solution(int num1, int num2) {\n        return num1 + num2; // 버그 수정 후 재제출\n    }\n}")
    private String reSubmittedCode;

    public SubmissionUpdateRequest() {}

    public SubmissionUpdateRequest(String reSubmittedCode) {
        this.reSubmittedCode = reSubmittedCode;
    }

    public String getReSubmittedCode() {
        return reSubmittedCode;
    }
}