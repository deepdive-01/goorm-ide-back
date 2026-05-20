package com.ide.project.domain.auth.service;

import com.ide.project.domain.auth.dto.request.EmailSendRequest;
import com.ide.project.domain.auth.dto.request.EmailVerifyRequest;
import com.ide.project.domain.user.repository.UserRepository;
import com.ide.project.global.exception.ErrorCode;
import com.ide.project.global.exception.custom.BusinessException;
import com.ide.project.global.util.RedisKeys;
import com.ide.project.integration.mail.MailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

// Spring 없이 Mockito만 사용하는 순수 단위 테스트
@ExtendWith(MockitoExtension.class)
class EmailVerifyServiceTest {

    // @Mock: 실제 구현 없이 동작을 흉내내는 가짜 객체
    @Mock
    private UserRepository userRepository;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    // redisTemplate.opsForValue()가 반환하는 객체도 Mock으로 준비
    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private MailService mailService;

    // @InjectMocks: 위 @Mock 객체들을 주입받아 실제 Service 객체 생성
    @InjectMocks
    private EmailVerifyService emailVerifyService;

    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_CODE = "123456";


    @Test
    @DisplayName("이미 가입된 이메일로 인증코드 발송 시 DUPLICATE_EMAIL 예외가 발생한다")
    void sendVerificationCode_duplicateEmail() {
        // Given: 해당 이메일이 DB에 이미 존재한다고 가정
        EmailSendRequest request = new EmailSendRequest(TEST_EMAIL);
        given(userRepository.existsByEmail(TEST_EMAIL)).willReturn(true);

        // When & Then: DUPLICATE_EMAIL 예외 발생 확인
        assertThatThrownBy(() -> emailVerifyService.sendVerificationCode(request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DUPLICATE_EMAIL);

        // 예외가 발생했으므로 메일 발송이 호출되지 않아야 함
        verify(mailService, never()).sendVerificationCode(anyString(), anyString());
    }

    @Test
    @DisplayName("새 이메일로 인증코드 발송 시 Redis에 저장되고 메일이 발송된다")
    void sendVerificationCode_success() {
        // Given: 해당 이메일이 DB에 없다고 가정
        EmailSendRequest request = new EmailSendRequest(TEST_EMAIL);
        given(userRepository.existsByEmail(TEST_EMAIL)).willReturn(false);
        // redisTemplate.opsForValue() 호출 시 준비한 valueOperations Mock 반환
        given(redisTemplate.opsForValue()).willReturn(valueOperations);

        // When: 예외 없이 성공해야 함
        assertThatCode(() -> emailVerifyService.sendVerificationCode(request))
                .doesNotThrowAnyException();

        // Then: Redis에 코드가 저장됐는지 검증
        // 인증코드는 랜덤이라 정확한 값을 모르므로 anyString() 사용
        verify(valueOperations).set(
                eq(RedisKeys.EMAIL_CODE + TEST_EMAIL),
                anyString(),
                eq(5L),
                eq(TimeUnit.MINUTES)
        );

        // 메일 발송 메서드가 호출됐는지 검증
        verify(mailService).sendVerificationCode(eq(TEST_EMAIL), anyString());
    }


    @Test
    @DisplayName("Redis에 코드가 없으면 (만료) EXPIRED_VERIFY_CODE 예외가 발생한다")
    void verifyCode_expiredCode() {
        // Given: Redis에 해당 키가 없는 상황 (null 반환 = 만료)
        EmailVerifyRequest request = new EmailVerifyRequest(TEST_EMAIL, TEST_CODE);
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get(RedisKeys.EMAIL_CODE + TEST_EMAIL)).willReturn(null);

        // When & Then
        assertThatThrownBy(() -> emailVerifyService.verifyCode(request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.EXPIRED_VERIFY_CODE);
    }

    @Test
    @DisplayName("코드가 일치하지 않으면 INVALID_VERIFY_CODE 예외가 발생한다")
    void verifyCode_wrongCode() {
        // Given: Redis에는 "123456"이 있는데 "999999"를 입력한 상황
        EmailVerifyRequest request = new EmailVerifyRequest(TEST_EMAIL, "999999");
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get(RedisKeys.EMAIL_CODE + TEST_EMAIL)).willReturn(TEST_CODE);

        // When & Then
        assertThatThrownBy(() -> emailVerifyService.verifyCode(request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_VERIFY_CODE);
    }

    @Test
    @DisplayName("코드가 일치하면 기존 코드는 삭제되고 인증 완료 플래그가 저장된다")
    void verifyCode_success() {
        // Given: Redis에 올바른 코드가 있는 상황
        EmailVerifyRequest request = new EmailVerifyRequest(TEST_EMAIL, TEST_CODE);
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get(RedisKeys.EMAIL_CODE + TEST_EMAIL)).willReturn(TEST_CODE);

        // When: 예외 없이 성공해야 함
        assertThatCode(() -> emailVerifyService.verifyCode(request))
                .doesNotThrowAnyException();

        // Then: 인증 코드 키 삭제 확인
        verify(redisTemplate).delete(RedisKeys.EMAIL_CODE + TEST_EMAIL);

        // Then: 인증 완료 플래그 저장 확인 (30분 TTL)
        verify(valueOperations).set(
                eq(RedisKeys.EMAIL_VERIFIED + TEST_EMAIL),
                eq("true"),
                eq(30L),
                eq(TimeUnit.MINUTES)
        );
    }
}
