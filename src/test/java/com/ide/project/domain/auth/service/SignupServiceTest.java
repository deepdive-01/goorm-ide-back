package com.ide.project.domain.auth.service;

import com.ide.project.domain.auth.dto.request.SignupRequest;
import com.ide.project.domain.auth.dto.response.SignupResponse;
import com.ide.project.domain.user.entity.Role;
import com.ide.project.domain.user.entity.User;
import com.ide.project.domain.user.repository.UserRepository;
import com.ide.project.global.exception.ErrorCode;
import com.ide.project.global.exception.custom.BusinessException;
import com.ide.project.global.util.RedisKeys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SignupServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @InjectMocks
    private SignupService signupService;

    // 테스트에서 공통으로 사용할 고정값
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "Test1234!@";
    private static final String TEST_NAME = "홍길동";
    private static final String TEST_NICKNAME = "길동이";
    private static final Role TEST_ROLE = Role.STUDENT;

    // 테스트용 SignupRequest 생성 헬퍼 메서드
    private SignupRequest createRequest() {
        return new SignupRequest(TEST_EMAIL, TEST_PASSWORD, TEST_NAME, TEST_NICKNAME, TEST_ROLE);
    }

    @Test
    @DisplayName("이미 존재하는 이메일로 회원가입 시 DUPLICATE_EMAIL 예외가 발생한다")
    void signup_duplicateEmail() {
        // Given: DB에 이미 같은 이메일이 존재
        given(userRepository.existsByEmail(TEST_EMAIL)).willReturn(true);

        // When & Then
        assertThatThrownBy(() -> signupService.signup(createRequest()))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DUPLICATE_EMAIL);

        // 중복 이메일이므로 저장이 호출되지 않아야 함
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("이메일 인증을 완료하지 않으면 EMAIL_NOT_VERIFIED 예외가 발생한다")
    void signup_emailNotVerified() {
        // Given: 이메일 중복은 아니지만 Redis에 인증 완료 플래그가 없음
        given(userRepository.existsByEmail(TEST_EMAIL)).willReturn(false);
        // hasKey()가 null 또는 false이면 인증 미완료로 처리
        given(redisTemplate.hasKey(RedisKeys.EMAIL_VERIFIED + TEST_EMAIL)).willReturn(false);

        // When & Then
        assertThatThrownBy(() -> signupService.signup(createRequest()))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.EMAIL_NOT_VERIFIED);

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("정상 회원가입 시 유저가 저장되고 SignupResponse가 반환된다")
    void signup_success() {
        // Given
        given(userRepository.existsByEmail(TEST_EMAIL)).willReturn(false);
        given(redisTemplate.hasKey(RedisKeys.EMAIL_VERIFIED + TEST_EMAIL)).willReturn(true);
        given(passwordEncoder.encode(TEST_PASSWORD)).willReturn("encodedPassword");

        // userRepository.save()가 반환할 User 객체를 Mock으로 준비
        // User의 id는 DB가 생성하므로 mock()으로 만든 뒤 값을 지정
        User savedUser = mock(User.class);
        given(savedUser.getId()).willReturn(1L);
        given(savedUser.getEmail()).willReturn(TEST_EMAIL);
        given(savedUser.getName()).willReturn(TEST_NAME);
        given(savedUser.getNickname()).willReturn(TEST_NICKNAME);
        given(savedUser.getRole()).willReturn(TEST_ROLE);
        given(savedUser.getCreatedAt()).willReturn(LocalDateTime.now());
        given(userRepository.save(any(User.class))).willReturn(savedUser);

        // When
        SignupResponse response = signupService.signup(createRequest());

        // Then: 반환된 응답값 검증
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo(TEST_EMAIL);
        assertThat(response.name()).isEqualTo(TEST_NAME);
        assertThat(response.nickname()).isEqualTo(TEST_NICKNAME);
        assertThat(response.role()).isEqualTo(TEST_ROLE);

        // 비밀번호가 인코딩된 후 저장됐는지 검증
        verify(passwordEncoder).encode(TEST_PASSWORD);
        verify(userRepository).save(any(User.class));

        // 회원가입 완료 후 Redis 인증 플래그가 삭제됐는지 검증
        verify(redisTemplate).delete(RedisKeys.EMAIL_VERIFIED + TEST_EMAIL);
    }

    @Test
    @DisplayName("Redis에서 인증 플래그 조회 시 null이 반환돼도 EMAIL_NOT_VERIFIED 예외가 발생한다")
    void signup_emailNotVerified_nullFromRedis() {
        // Given: hasKey()가 null을 반환하는 경우 (Redis 연결 이슈 등)
        given(userRepository.existsByEmail(TEST_EMAIL)).willReturn(false);
        given(redisTemplate.hasKey(anyString())).willReturn(null);

        // When & Then: null도 미인증으로 처리해야 함
        assertThatThrownBy(() -> signupService.signup(createRequest()))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.EMAIL_NOT_VERIFIED);
    }
}
