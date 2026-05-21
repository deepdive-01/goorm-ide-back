package com.ide.project.domain.auth.service;

import com.ide.project.domain.auth.dto.request.OAuthSignupRequest;
import com.ide.project.domain.auth.dto.response.TokenResponse;
import com.ide.project.domain.user.entity.LoginType;
import com.ide.project.domain.user.entity.OauthAccount;
import com.ide.project.domain.user.entity.Provider;
import com.ide.project.domain.user.entity.User;
import com.ide.project.domain.user.repository.OauthAccountRepository;
import com.ide.project.domain.user.repository.UserRepository;
import com.ide.project.global.exception.ErrorCode;
import com.ide.project.global.exception.custom.BusinessException;
import com.ide.project.global.util.RedisKeys;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OAuthSignupService {

    private final UserRepository userRepository;
    private final OauthAccountRepository oauthAccountRepository;
    private final AuthService authService;
    private final RedisTemplate<String, String> redisTemplate;

    @Transactional
    public TokenResponse signup(OAuthSignupRequest request, HttpServletResponse response) {

        // Redis에서 임시 데이터 조회
        String tempValue = redisTemplate.opsForValue().get(RedisKeys.OAUTH_TEMP + request.tempKey());

        if (tempValue == null) {
            throw new BusinessException(ErrorCode.OAUTH_TEMP_EXPIRED);
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        // 구분자로 파싱
        // parts[0] = "KAKAO", parts[1] = "12345678", parts[2] = 닉네임, parts[3] = "email@email"
        String[] parts = tempValue.split("::", -1);
        Provider provider = Provider.valueOf(parts[0]);
        String providerId = parts[1];
        String nickname = parts[2];

        // 유저 정보 저장
        User user = User.builder()
                .email(request.email())
                .password(null)
                .name(request.name())
                .nickname(request.nickname())
                .role(request.role())
                .loginType(LoginType.SOCIAL)
                .build();

        // 레포를 이용해 유저를 저장
        User savedUser = userRepository.save(user);

        // 소셜 계정 정보 저장
        OauthAccount oauthAccount = OauthAccount.builder()
                .user(savedUser)
                .provider(provider)
                .providerId(providerId)
                .build();

        // 소셜 계정 정보를 레포를 통해 저장
        oauthAccountRepository.save(oauthAccount);

        // 임시 데이터 삭제 (UUID)
        redisTemplate.delete(RedisKeys.OAUTH_TEMP + request.tempKey());

        return authService.oauthLogin(savedUser, response);
    }

}
