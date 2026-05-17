package com.ide.project.domain.auth.service;

import com.ide.project.domain.auth.dto.request.SignupRequest;
import com.ide.project.domain.auth.dto.response.SignupResponse;
import com.ide.project.domain.user.entity.LoginType;
import com.ide.project.domain.user.entity.User;
import com.ide.project.domain.user.repository.UserRepository;
import com.ide.project.global.exception.ErrorCode;
import com.ide.project.global.exception.custom.BusinessException;
import com.ide.project.global.util.RedisKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.endpoints.internal.Value;

@Service
@RequiredArgsConstructor
public class SignupService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, String> redisTemplate;

    @Transactional
    public SignupResponse signup(SignupRequest request) {

        // 회원가입할 때 중복된 이메일인지 검증
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        // redis에 이메일 인증이 완료된 유저인지를 확인
        Boolean isVerified = redisTemplate.hasKey(
                RedisKeys.EMAIL_VERIFIED + request.email()
        );

        // 인증 완료 여부를 확인 후 인증이 완료되지 않았으면 오류를 반환
        // !isVerified를 사용하지 않는 이유는 hasKey()가 null을 반환할 수 있기 때문입니다, 노란줄을 무시해주세요.
        if (!Boolean.TRUE.equals(isVerified)) {
            throw new BusinessException(ErrorCode.EMAIL_NOT_VERIFIED);
        }

        // 유저를 저장
        // 비밀번호는 인코딩 후 저장
        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .name(request.name())
                .nickname(request.nickname())
                .role(request.role())
                .loginType(LoginType.LOCAL)
                .build();

        // 저장된 유저를 레포를 통해 DB로 저장
        User savedUser = userRepository.save(user);

        // 회원가입된 유저의 인증 완료 정보를 삭제
        redisTemplate.delete(RedisKeys.EMAIL_VERIFIED + request.email());

        // 회원가입 성공 응답을 반환
        return SignupResponse.from(savedUser);
    }

}
