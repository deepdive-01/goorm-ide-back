package com.ide.project.domain.files.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "워크스페이스 문제 상세 정보 및 초기 코드 응답 객체 (태스크 1-2)")
public class ProblemResponse {

    @Schema(description = "할당된 복사본 문제의 고유 ID (problems 테이블의 PK)", example = "12")
    private Long id;

    @Schema(description = "문제 제목", example = "두 수의 합 구하기 알고리즘")
    private String title;

    @Schema(description = "문제 상세 설명 및 제한사항 지문", example = "정수 num1과 num2가 주어질 때 두 수의 합을 return하도록 함수를 완성하세요.")
    private String description;

    @Schema(description = "난이도 레벨", example = "LEVEL_1")
    private String difficulty;

    @Schema(description = "제한 언어 스펙", example = "JAVA")
    private String language;

    @Schema(description = "에디터 코딩창에 최초로 채워줄 스타터 소스코드", example = "public class Solution {\n    public int solution(int num1, int num2) {\n        return 0;\n    }\n}")
    private String starterCode;

    // 데이터를 채워줄 표준 생성자
    public ProblemResponse(Long id, String title, String description, String difficulty, String language, String starterCode) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.difficulty = difficulty;
        this.language = language;
        this.starterCode = starterCode;
    }

    // Jackson 직렬화 및 스웨거 바인딩을 위한 표준 Getter 메서드들
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getDifficulty() { return difficulty; }
    public String getLanguage() { return language; }
    public String getStarterCode() { return starterCode; }
}