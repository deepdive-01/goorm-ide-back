package com.ide.project.domain.code.dto;

import lombok.Getter;

@Getter
public class CodeExecuteRequest {
    private String language;  // python, java, javascript, cpp
    private String code;
    private String stdin;     // 입력값 (없으면 빈 문자열)
}