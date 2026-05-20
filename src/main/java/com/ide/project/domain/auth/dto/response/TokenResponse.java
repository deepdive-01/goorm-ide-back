package com.ide.project.domain.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "토큰 응답")
public record TokenResponse (

        @Schema(description = "AccessToken", example = "eyJhbGciOiJIUzI1NiJ9...")
        String accessToken,

        @Schema(description = "토큰 타입", example = "Bearer")
        String tokenType,

        @Schema(description = "AccessToken 만료 시간 (초)", example = "3600")
        long expiresIn

) {
    public static TokenResponse of(String accessToken, long accessExpirationMs) {
        return new TokenResponse(accessToken, "Bearer", accessExpirationMs / 1000);
    }
}
