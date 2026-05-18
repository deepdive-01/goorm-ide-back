package com.ide.project.domain.auth.service;

import com.ide.project.domain.auth.dto.request.LoginRequest;
import com.ide.project.domain.auth.dto.response.TokenResponse;
import com.ide.project.domain.user.entity.LoginType;
import com.ide.project.domain.user.entity.Role;
import com.ide.project.domain.user.entity.User;
import com.ide.project.domain.user.repository.UserRepository;
import com.ide.project.global.exception.ErrorCode;
import com.ide.project.global.exception.custom.BusinessException;
import com.ide.project.global.security.jwt.JwtProvider;
import com.ide.project.global.util.RedisKeys;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    // redisTemplate.opsForValue()가 반환하는 객체도 Mock으로 준비
    @Mock
    private ValueOperations<String, String> valueOperations;

    // @InjectMocks: 위 @Mock 객체들을 주입받아 실제 Service 객체 생성
    @InjectMocks
    private AuthService authService;

    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "Test1234!@";
    private static final Long TEST_USER_ID = 1L;
    private static final String TEST_ACCESS_TOKEN = "mockAccessToken";
    private static final String TEST_REFRESH_TOKEN = "mockRefreshToken";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "accessExpiration", 3600000L);
        ReflectionTestUtils.setField(authService, "refreshExpiration", 604800000L);
    }


    @Test
    @DisplayName("존재하지 않는 이메일로 로그인 시 INVALID_CREDENTIALS 예외가 발생한다")
    void login_userNotFound() {
        // Given: DB에 해당 이메일이 없는 상황
        LoginRequest request = new LoginRequest(TEST_EMAIL, TEST_PASSWORD);
        given(userRepository.findByEmail(TEST_EMAIL)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> authService.login(request, mock(HttpServletResponse.class)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_CREDENTIALS);
    }

    @Test
    @DisplayName("비밀번호가 틀리면 INVALID_CREDENTIALS 예외가 발생한다")
    void login_wrongPassword() {
        // Given: 이메일은 맞지만 비밀번호 다름
        LoginRequest request = new LoginRequest(TEST_EMAIL, "wrongPassword");
        User user = mock(User.class);
        given(user.getPassword()).willReturn("encodedPassword");
        given(userRepository.findByEmail(TEST_EMAIL)).willReturn(Optional.of(user));
        // passwordEncoder.matches(입력값, 저장된 해시값) → false
        given(passwordEncoder.matches("wrongPassword", "encodedPassword")).willReturn(false);

        // When & Then
        assertThatThrownBy(() -> authService.login(request, mock(HttpServletResponse.class)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_CREDENTIALS);
    }

    @Test
    @DisplayName("비활성화된 계정으로 로그인 시 INACTIVE_USER 예외가 발생한다")
    void login_inactiveUser() {
        // Given: 이메일, 비밀번호는 맞지만 계정이 비활성화 상태
        LoginRequest request = new LoginRequest(TEST_EMAIL, TEST_PASSWORD);
        User user = mock(User.class);
        given(user.getPassword()).willReturn("encodedPassword");
        given(user.isActive()).willReturn(false); // 비활성화 계정
        given(userRepository.findByEmail(TEST_EMAIL)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(TEST_PASSWORD, "encodedPassword")).willReturn(true);

        // When & Then
        assertThatThrownBy(() -> authService.login(request, mock(HttpServletResponse.class)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INACTIVE_USER);
    }

    @Test
    @DisplayName("로컬 로그인 유저가 이메일 인증을 완료하지 않으면 EMAIL_NOT_VERIFIED 예외가 발생한다")
    void login_emailNotVerified() {
        // Given: 계정은 정상이지만 이메일 인증을 아직 완료하지 않은 로컬 유저
        LoginRequest request = new LoginRequest(TEST_EMAIL, TEST_PASSWORD);
        User user = mock(User.class);
        given(user.getPassword()).willReturn("encodedPassword");
        given(user.isActive()).willReturn(true);
        given(user.getLoginType()).willReturn(LoginType.LOCAL);
        given(user.isEmailVerified()).willReturn(false); // 이메일 미인증
        given(userRepository.findByEmail(TEST_EMAIL)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(TEST_PASSWORD, "encodedPassword")).willReturn(true);

        // When & Then
        assertThatThrownBy(() -> authService.login(request, mock(HttpServletResponse.class)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.EMAIL_NOT_VERIFIED);
    }

    @Test
    @DisplayName("정상 로그인 시 TokenResponse가 반환되고 Redis에 RT가 저장된다")
    void login_success() {
        // Given
        LoginRequest request = new LoginRequest(TEST_EMAIL, TEST_PASSWORD);
        HttpServletResponse response = mock(HttpServletResponse.class);

        User user = mock(User.class);
        given(user.getId()).willReturn(TEST_USER_ID);
        given(user.getPassword()).willReturn("encodedPassword");
        given(user.isActive()).willReturn(true);
        given(user.getLoginType()).willReturn(LoginType.LOCAL);
        given(user.isEmailVerified()).willReturn(true); // 이메일 인증 완료
        given(user.getRole()).willReturn(Role.STUDENT);
        given(userRepository.findByEmail(TEST_EMAIL)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(TEST_PASSWORD, "encodedPassword")).willReturn(true);

        // JWT 및 Redis Mock 설정
        given(jwtProvider.generateAccessToken(TEST_USER_ID, Role.STUDENT)).willReturn(TEST_ACCESS_TOKEN);
        given(jwtProvider.generateRefreshToken(TEST_USER_ID)).willReturn(TEST_REFRESH_TOKEN);
        given(redisTemplate.opsForValue()).willReturn(valueOperations);

        // When
        TokenResponse tokenResponse = authService.login(request, response);

        // Then: 반환된 AccessToken 확인
        assertThat(tokenResponse.accessToken()).isEqualTo(TEST_ACCESS_TOKEN);
        assertThat(tokenResponse.tokenType()).isEqualTo("Bearer");
        assertThat(tokenResponse.expiresIn()).isEqualTo(3600L); // 3600000ms / 1000 = 3600초

        // Then: Redis에 RefreshToken이 refreshExpiration(604800000ms) TTL로 저장됐는지 확인
        verify(valueOperations).set(
                eq(RedisKeys.REFRESH_TOKEN + TEST_USER_ID),
                eq(TEST_REFRESH_TOKEN),
                eq(604800000L),
                eq(TimeUnit.MILLISECONDS)
        );

        // Then: RT 쿠키가 응답 헤더에 추가됐는지 확인
        verify(response).addHeader(anyString(), anyString());
    }


    @Test
    @DisplayName("로그아웃 시 Redis의 RT가 삭제되고 쿠키가 만료 처리된다")
    void logout_success() {
        // Given
        HttpServletResponse response = mock(HttpServletResponse.class);

        // When
        authService.logout(TEST_USER_ID, response);

        // Then: Redis에서 해당 유저의 RT 키 삭제 확인
        verify(redisTemplate).delete(RedisKeys.REFRESH_TOKEN + TEST_USER_ID);

        // Then: maxAge=0으로 쿠키 만료 헤더가 설정됐는지 확인
        verify(response).addHeader(anyString(), anyString());
    }


    @Test
    @DisplayName("Redis에 저장된 RT가 없으면(만료 or 로그아웃) REVOKED_REFRESH_TOKEN 예외가 발생한다")
    void reissue_tokenNotInRedis() {
        // Given: validateToken은 통과, Redis에 해당 토큰이 없는 상황
        given(jwtProvider.getUserId(TEST_REFRESH_TOKEN)).willReturn(TEST_USER_ID);
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get(RedisKeys.REFRESH_TOKEN + TEST_USER_ID)).willReturn(null);

        // When & Then
        assertThatThrownBy(() -> authService.reissue(TEST_REFRESH_TOKEN))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.REVOKED_REFRESH_TOKEN);
    }

    @Test
    @DisplayName("Redis의 RT와 요청 RT가 다르면(탈취 의심) REVOKED_REFRESH_TOKEN 예외가 발생한다")
    void reissue_tokenMismatch() {
        // Given: Redis에는 다른 토큰이 저장된 상황 (다른 기기에서 재발급 됐거나 탈취 시도)
        given(jwtProvider.getUserId(TEST_REFRESH_TOKEN)).willReturn(TEST_USER_ID);
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get(RedisKeys.REFRESH_TOKEN + TEST_USER_ID)).willReturn("differentToken");

        // When & Then
        assertThatThrownBy(() -> authService.reissue(TEST_REFRESH_TOKEN))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.REVOKED_REFRESH_TOKEN);
    }

    @Test
    @DisplayName("유효한 RT지만 DB에 유저가 없으면 USER_NOT_FOUND 예외가 발생한다")
    void reissue_userNotFound() {
        // Given: 토큰은 유효하고 Redis에도 있지만, 그 사이에 계정이 삭제된 상황
        given(jwtProvider.getUserId(TEST_REFRESH_TOKEN)).willReturn(TEST_USER_ID);
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get(RedisKeys.REFRESH_TOKEN + TEST_USER_ID)).willReturn(TEST_REFRESH_TOKEN);
        given(userRepository.findById(TEST_USER_ID)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> authService.reissue(TEST_REFRESH_TOKEN))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("정상적인 RT로 재발급 시 새로운 AccessToken이 담긴 TokenResponse가 반환된다")
    void reissue_success() {
        // Given: 모든 조건이 정상인 상황
        User user = mock(User.class);
        given(user.getId()).willReturn(TEST_USER_ID);
        given(user.getRole()).willReturn(Role.STUDENT);

        given(jwtProvider.getUserId(TEST_REFRESH_TOKEN)).willReturn(TEST_USER_ID);
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get(RedisKeys.REFRESH_TOKEN + TEST_USER_ID)).willReturn(TEST_REFRESH_TOKEN);
        given(userRepository.findById(TEST_USER_ID)).willReturn(Optional.of(user));
        given(jwtProvider.generateAccessToken(TEST_USER_ID, Role.STUDENT)).willReturn("newAccessToken");

        // When
        TokenResponse tokenResponse = authService.reissue(TEST_REFRESH_TOKEN);

        // Then: 새로 발급된 AccessToken 확인
        assertThat(tokenResponse.accessToken()).isEqualTo("newAccessToken");
        assertThat(tokenResponse.tokenType()).isEqualTo("Bearer");
        assertThat(tokenResponse.expiresIn()).isEqualTo(3600L); // 3600000ms / 1000
    }
}
