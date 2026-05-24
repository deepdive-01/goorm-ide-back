package com.ide.project.domain.code.dto;

public record CodeExecuteResponse(
    String output,
    String stderr,
    boolean isError
) {}