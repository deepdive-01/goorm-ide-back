package com.ide.project.domain.auth.dto.request;

import com.ide.project.domain.user.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OAuthSignupRequest (

        // 임시 키
        @NotBlank
        String tempKey,

        // 사용자가 입력한 이메일
        @NotBlank
        @Email
        String email,

        // 사용자가 입력한 실명
        @NotBlank
        String name,

        // MENTOR, STUDENT 선택
        @NotNull
        Role role
) {}
