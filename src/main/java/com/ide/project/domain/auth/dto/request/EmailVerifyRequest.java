package com.ide.project.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "이메일 인증코드 확인 요청")
public record EmailVerifyRequest (

        @Schema(description = "인증코드를 발송한 이메일 주소", example = "user@example.com")
        @NotBlank
        @Email
        String email,

        @Schema(description = "6자리 인증코드", example = "123456")
        @NotBlank
        @Size(min = 6, max = 6, message = "인증 코드는 6자리 입니다.")
        String code

) {}
