package com.ide.project.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiResponse<T> {

    private final int status;
    private final String code;
    private final String message;
    private final T data;

    public static <T> ApiResponse<T> success(int status, String code, String message, T data) {
        return new ApiResponse<>(status, code, message, data);
    }

    public static ApiResponse<Void> success(int status, String code, String message) {
        return new ApiResponse<>(status, code, message, null);
    }

    public static ApiResponse<Void> error(int status, String code, String message) {
        return new ApiResponse<>(status, code, message, null);
    }
}
