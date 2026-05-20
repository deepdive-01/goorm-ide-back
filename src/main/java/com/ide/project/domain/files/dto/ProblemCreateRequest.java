package com.ide.project.domain.files.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "강사 수제 문제 직접 생성 요청 객체")
public class ProblemCreateRequest {

    @NotBlank(message = "문제 제목은 필수 입력값입니다.")
    @Size(max = 100, message = "제목은 100자를 초과할 수 없습니다.")
    @Schema(description = "출제할 문제 제목", example = "두 수의 합 구하기 알고리즘")
    private String title;

    @NotBlank(message = "문제 지문 및 설명은 필수 입력값입니다.")
    @Schema(description = "문제 상세 설명 및 제한사항 지문", example = "정수 num1과 num2가 주어질 때 두 수의 합을 return하도록 함수를 완성하세요.")
    private String description;

    @NotBlank(message = "난이도 설정은 필수입니다. (예: LEVEL_1)")
    @Schema(description = "난이도 설정", example = "LEVEL_1")
    private String difficulty;

    @NotBlank(message = "제한 언어를 명시해야 합니다. (예: JAVA)")
    @Schema(description = "제한 언어 스펙", example = "JAVA")
    private String language;

    @NotNull(message = "초기 코드는 null일 수 없습니다. (빈 칸인 경우 빈 문자열 전송)")
    @Schema(description = "에디터 코딩창 초기 렌더링용 스타터 소스코드", example = "public class Solution {\n    public int solution(int num1, int num2) {\n        return 0;\n    }\n}")
    private String starterCode;

    public ProblemCreateRequest() {}

    public ProblemCreateRequest(String title, String description, String difficulty, String language, String starterCode) {
        this.title = title;
        this.description = description;
        this.difficulty = difficulty;
        this.language = language;
        this.starterCode = starterCode;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getDifficulty() { return difficulty; }
    public String getLanguage() { return language; }
    public String getStarterCode() { return starterCode; }
}