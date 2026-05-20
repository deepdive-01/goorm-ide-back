package com.ide.project.domain.auth.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ide.project.domain.user.entity.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "소셜 신규 유저 회원가입 요청")
public record OAuthSignupRequest (

        @Schema(description = "소셜 로그인 후 발급된 임시 키 (리다이렉트 URL의 tempKey 쿼리파라미터)", example = "550e8400-e29b-41d4-a716-446655440000")
        @JsonProperty("temp_key")
        @NotBlank
        String tempKey,

        @Schema(description = "사용자가 직접 입력하는 이메일 주소", example = "user@example.com")
        @NotBlank
        @Email
        String email,

        @Schema(description = "실명", example = "홍길동")
        @NotBlank
        String name,

        @Schema(description = "역할 (STUDENT 또는 MENTOR)", example = "STUDENT")
        @NotNull
        Role role

) {}
