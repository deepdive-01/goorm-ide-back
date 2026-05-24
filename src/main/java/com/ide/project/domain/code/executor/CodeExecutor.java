package com.ide.project.domain.code.executor;

public interface CodeExecutor {
    String execute(String language, String code, String stdin);
}