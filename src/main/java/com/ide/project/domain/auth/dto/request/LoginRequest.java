package com.ide.project.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "로컬 로그인 요청")
public record LoginRequest (

        @Schema(description = "이메일 주소", example = "user@example.com")
        @NotNull
        @Email
        String email,

        @Schema(description = "비밀번호", example = "Password1!")
        @NotBlank
        String password

) {}
