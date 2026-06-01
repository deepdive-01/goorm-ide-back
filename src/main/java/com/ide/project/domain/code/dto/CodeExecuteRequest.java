package com.ide.project.domain.code.dto;

public record CodeExecuteRequest(
    String language,
    String code,
    String stdin
) {}
