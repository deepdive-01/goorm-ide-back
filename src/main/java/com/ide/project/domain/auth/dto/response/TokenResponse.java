package com.ide.project.domain.auth.dto.response;

public record TokenResponse (
        String accessToken,
        String tokenType,
        long expiresIn
) {
    public static TokenResponse of(String accessToken, long accessExpirationMs) {
        return new TokenResponse(accessToken, "Bearer", accessExpirationMs / 1000);
    }
}
