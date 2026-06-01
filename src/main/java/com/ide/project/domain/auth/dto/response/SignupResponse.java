package com.ide.project.domain.auth.dto.response;

import com.ide.project.domain.user.entity.Role;
import com.ide.project.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "회원가입 응답")
public record SignupResponse (

        @Schema(description = "유저 ID", example = "1")
        Long id,

        @Schema(description = "이메일", example = "user@example.com")
        String email,

        @Schema(description = "실명", example = "홍길동")
        String name,

        @Schema(description = "닉네임", example = "길동이")
        String nickname,

        @Schema(description = "역할", example = "STUDENT")
        Role role,

        @Schema(description = "가입일시")
        LocalDateTime createdAt

) {
    public static SignupResponse from(User user) {
        return new SignupResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getNickname(),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}
