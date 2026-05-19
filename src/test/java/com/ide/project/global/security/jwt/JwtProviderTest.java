package com.ide.project.global.security.jwt;

import com.ide.project.domain.user.entity.Role;
import com.ide.project.global.exception.custom.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


public class JwtProviderTest {
    private JwtProvider jwtProvider;

    private static final String SECRET = "test-secret-key-must-be-at-least-32-characters-long";
    private static final long ACCESS_EXPIRATION = 3600000L; // 1시간
    private static final long REFRESH_EXPIRATION = 604800000L; // 7일

    // 각 테스트 메서드 실행 전에 매번 호출
    @BeforeEach
    void setUp() {
        jwtProvider = new JwtProvider(SECRET, ACCESS_EXPIRATION, REFRESH_EXPIRATION);
    }

    @Test
    @DisplayName("AccessToken 발급 시 userId와 role이 토큰에 담겨 있어야 한다.")
    void generateAccessToken_success() {
        // Given
        // 유저 ID와 role 정의
        Long userId = 1L;
        Role role = Role.STUDENT;

        // When
        // AT 생성
        String token = jwtProvider.generateAccessToken(userId, role);

        // Then
        // 토큰의 값이 원래의 값과 일치해야 함
        assertThat(token).isNotNull();
        assertThat(jwtProvider.getUserId(token)).isEqualTo(userId);
        assertThat(jwtProvider.getRole(token)).isEqualTo(role.name());
    }

    @Test
    @DisplayName("RefreshToken 발급 시 userId만 담겨야 한다")
    void generateRefreshToken_success() {
        // Given
        Long userId = 1L;

        // When
        String token = jwtProvider.generateRefreshToken(userId);

        // Then
        assertThat(token).isNotNull();
        assertThat(jwtProvider.getUserId(token)).isEqualTo(userId);
    }

    @Test
    @DisplayName("유효한 토큰은 검증 시 예외가 발생하지 않아야 한다")
    void validateToken_validToken() {
        // Given
        String token = jwtProvider.generateAccessToken(1L, Role.STUDENT);

        // When, Then
        assertThatCode(() -> jwtProvider.validateToken(token)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("만료된 토큰은 검증 시 오류가 발생해야 한다")
    void validateToken_expiredToken() throws InterruptedException {

        // 만료시간이 1ms인 토큰 생성
        JwtProvider shortLivedProvider = new JwtProvider(SECRET, 1L, 1L);
        String token = shortLivedProvider.generateAccessToken(1L, Role.STUDENT);

        // 만료까지 대기
        Thread.sleep(10);

        // 토큰 검증
        assertThatThrownBy(() -> shortLivedProvider.validateToken(token)).isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("변조된 토큰은 검증 시 오류가 발생해야 한다")
    void validateToken_temperedToken() {
        String tamperedToken = "eydjdjalalsidk.qekekfjfj.invalid_signature";

        assertThatThrownBy(() -> jwtProvider.validateToken(tamperedToken)).isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("서로 다른 시크릿 키로 만든 토큰은 검증에 실패 해야 한다")
    void validateToken_differentSecretKey() {
        JwtProvider anotherProvider = new JwtProvider(
                "completely-different-secret-key-at-least-32-chars",
                ACCESS_EXPIRATION,
                REFRESH_EXPIRATION
        );
        String token = anotherProvider.generateAccessToken(1L, Role.STUDENT);

        assertThatThrownBy(() -> jwtProvider.validateToken(token)).isInstanceOf(BusinessException.class);
    }




}
