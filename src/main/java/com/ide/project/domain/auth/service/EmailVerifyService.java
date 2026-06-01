package com.ide.project.domain.auth.service;

import com.ide.project.domain.auth.dto.request.EmailSendRequest;
import com.ide.project.domain.auth.dto.request.EmailVerifyRequest;
import com.ide.project.domain.user.repository.UserRepository;
import com.ide.project.global.exception.ErrorCode;
import com.ide.project.global.exception.custom.BusinessException;
import com.ide.project.global.util.RedisKeys;
import com.ide.project.integration.mail.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;


@Service
@RequiredArgsConstructor
public class EmailVerifyService {
    private final UserRepository userRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final MailService mailService;

    // 보안 문제를 해결하기 위한 SecureRandom 함수 사용
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private static final long CODE_TTL = 5L; // 코드 인증 시간 5분
    private static final long VERIFIED_TTL = 30L; // 인증 완료 후 회원가입 완료 시간 30분

    public void sendVerificationCode(EmailSendRequest request) {

        // 유저 레포를 통해 이미 존재하는 email인지를 확인
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        String code = String.format("%06d", SECURE_RANDOM.nextInt(1_000_000));

        // redis에 키에 맞게 저장
        redisTemplate.opsForValue().set(
                RedisKeys.EMAIL_CODE + request.email(),
                code,
                CODE_TTL,
                TimeUnit.MINUTES
        );

        mailService.sendVerificationCode(request.email(), code);
    }

    public void verifyCode(EmailVerifyRequest request) {

        // redis에 해당하는 email이 있는지 확인
        String storedCode = redisTemplate.opsForValue()
                .get(RedisKeys.EMAIL_CODE + request.email());

        // 없으면 만료된 유저이므로 오류를 반환
        if (storedCode == null) {
            throw new BusinessException(ErrorCode.EXPIRED_VERIFY_CODE);
        }

        // 잘못된 코드를 입력했을 때 오류를 반환
        if (!storedCode.equals(request.code())) {
            throw new BusinessException(ErrorCode.INVALID_VERIFY_CODE);
        }

        // 저장된 이메일 정보를 삭제하고
        redisTemplate.delete(RedisKeys.EMAIL_CODE + request.email());;

        // 인증 완료된 이메일 정보로 업데이트
        redisTemplate.opsForValue().set(
                RedisKeys.EMAIL_VERIFIED + request.email(),
                "true",
                VERIFIED_TTL,
                TimeUnit.MINUTES
        );
    }
}
