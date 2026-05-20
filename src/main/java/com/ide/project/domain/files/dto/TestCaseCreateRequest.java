package com.ide.project.domain.files.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "테스트케이스 생성 요청 객체")
public class TestCaseCreateRequest {

    @Schema(description = "테스트케이스 입력값 (Input)", example = "2 3")
    private String inputCase;

    @Schema(description = "테스트케이스 정답 기댓값 (Output)", example = "5")
    private String outputCase;

    @Schema(description = "학생용 기본 예제 파일 노출 여부", example = "true")
    private boolean isExample;

    // 기본 생성자 및 전체 생성자
    public TestCaseCreateRequest() {}

    public TestCaseCreateRequest(String inputCase, String outputCase, boolean isExample) {
        this.inputCase = inputCase;
        this.outputCase = outputCase;
        this.isExample = isExample;
    }

    // 표준 Getter 메서드들
    public String getInputCase() { return inputCase; }
    public String getOutputCase() { return outputCase; }
    public boolean isExample() { return isExample; }
}