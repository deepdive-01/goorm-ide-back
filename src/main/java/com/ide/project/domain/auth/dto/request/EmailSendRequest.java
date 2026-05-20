package com.ide.project.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "이메일 인증코드 발송 요청")
public record EmailSendRequest (

        @Schema(description = "인증코드를 받을 이메일 주소", example = "user@example.com")
        @NotBlank
        @Email
        String email

) {}
