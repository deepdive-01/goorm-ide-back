package com.ide.project.domain.files.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "강사 문제 수정 요청 객체")
public class ProblemUpdateRequest {

    @Schema(description = "수정할 문제 제목", example = "(수정) 두 수의 차 구하기")
    private String title;

    @Schema(description = "수정할 문제 상세 설명 및 지문", example = "정수 num1과 num2가 주어질 때 두 수의 차를 return하도록 함수를 완성하세요.")
    private String description;

    @Schema(description = "수정할 난이도 레벨", example = "LEVEL_2")
    private String difficulty;

    @Schema(description = "수정할 제한 언어 스펙", example = "JAVA")
    private String language;

    @Schema(description = "수정할 에디터 초기 소스코드", example = "public class Solution {\n    public int solution(int num1, int num2) {\n        return num1 - num2;\n    }\n}")
    private String starterCode;

    // 기본 생성자 및 전체 생성자
    public ProblemUpdateRequest() {}

    public ProblemUpdateRequest(String title, String description, String difficulty, String language, String starterCode) {
        this.title = title;
        this.description = description;
        this.difficulty = difficulty;
        this.language = language;
        this.starterCode = starterCode;
    }

    // 표준 Getter 메서드들
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getDifficulty() { return difficulty; }
    public String getLanguage() { return language; }
    public String getStarterCode() { return starterCode; }
}