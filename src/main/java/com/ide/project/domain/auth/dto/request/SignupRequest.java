package com.ide.project.domain.auth.dto.request;

import com.ide.project.domain.user.entity.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "로컬 회원가입 요청")
public record SignupRequest (

        @Schema(description = "이메일 주소 (인증 완료된 이메일)", example = "user@example.com")
        @NotBlank
        @Email
        String email,

        @Schema(description = "비밀번호 (8~20자, 영문+숫자+특수문자 포함)", example = "Password1!")
        @NotBlank
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,20}$",
                message = "비밀번호는 8~20자, 영문+숫자+특수문자를 포함해야 합니다."
        )
        String password,

        @Schema(description = "실명", example = "홍길동")
        @NotBlank
        @Size(max = 50)
        String name,

        @Schema(description = "닉네임", example = "길동이")
        @NotBlank
        @Size(max = 50)
        String nickname,

        @Schema(description = "역할 (STUDENT 또는 MENTOR)", example = "STUDENT")
        @NotNull
        Role role

) {}
