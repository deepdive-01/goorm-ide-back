package com.ide.project.domain.auth.service;

import com.ide.project.domain.auth.dto.request.LoginRequest;
import com.ide.project.domain.auth.dto.response.TokenResponse;
import com.ide.project.domain.user.entity.LoginType;
import com.ide.project.domain.user.entity.User;
import com.ide.project.domain.user.repository.UserRepository;
import com.ide.project.global.exception.ErrorCode;
import com.ide.project.global.exception.custom.BusinessException;
import com.ide.project.global.security.jwt.JwtProvider;
import com.ide.project.global.util.RedisKeys;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${jwt.access-expiration}")
    private long accessExpiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    // 로컬 로그인
    @Transactional
    public TokenResponse login(LoginRequest request, HttpServletResponse response) {

        // 유저의 이메일을 찾아서 없으면 오류를 반환
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        // 비밀번호가 맞지 않으면 오류를 반환
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        // 비활성화 계정일 경우 오류를 반환
        if (!user.isActive()) {
            throw new BusinessException(ErrorCode.INACTIVE_USER);
        }

        // 이럴 경우가 없긴하지만
        // 로컬 로그인 유저가 이메일 인증이 완료되지 않은 상황이라면
        // 오류를 반환
        if (user.getLoginType() == LoginType.LOCAL && !user.isEmailVerified()) {
            throw new BusinessException(ErrorCode.EMAIL_NOT_VERIFIED);
        }

        return issueTokens(user, response);
    }

    // 소셜 로그인 로직
    // 토큰 발급 로직만 있으면 됨
    // 앞에서 처리하고 넘어갈 곳임
    @Transactional
    public TokenResponse oauthLogin(User user, HttpServletResponse response) {
        if (!user.isActive()) {
            throw new BusinessException(ErrorCode.INACTIVE_USER);
        }

        return issueTokens(user, response);
    }

    // 로그아웃 로직
    // redis에 저장된 RT 값을 삭제
    // 유저 브라우저의 RT 쿠키도 삭제
    public void logout(Long userId, HttpServletResponse response) {
        redisTemplate.delete(RedisKeys.REFRESH_TOKEN + userId);
        clearRefreshTokenCookie(response);
    }

    public TokenResponse reissue(String refreshToken) {

        // JWT 토큰 검증 (RT)
        jwtProvider.validateToken(refreshToken);

        // 유저 정보를 빼냄
        Long userId = jwtProvider.getUserId(refreshToken);

        // redis에 저장된 RT 값을 찾음
        String storedToken = redisTemplate.opsForValue()
                .get(RedisKeys.REFRESH_TOKEN + userId);

        // redis에 토큰 정보가 없거나, 토큰정보가 다를 경우 오류를 반환
        if (storedToken == null || !storedToken.equals(refreshToken)) {
            throw new BusinessException(ErrorCode.REVOKED_REFRESH_TOKEN);
        }

        // 레포에서 유저를 찾음
        // 없을 경우 유저를 못찾는 에러를 반환
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 인증이 완료되었으면 새로운 AT를 발급
        String newAccessToken = jwtProvider.generateAccessToken(user.getId(), user.getRole());

        return TokenResponse.of(newAccessToken, accessExpiration);

    }

    // 토큰 발급 로직
    private TokenResponse issueTokens(User user, HttpServletResponse response) {

        // accessToken 생성
        String accessToken = jwtProvider.generateAccessToken(user.getId(), user.getRole());

        // refreshToken 생성
        String refreshToken = jwtProvider.generateRefreshToken(user.getId());

        // redis에 refreshToken을 저장
        redisTemplate.opsForValue().set(
                RedisKeys.REFRESH_TOKEN + user.getId(), // 유저 ID 저장
                refreshToken, // RT값 저장
                refreshExpiration, // 파기 시간 저장
                TimeUnit.MILLISECONDS
        );

        setRefreshTokenCookie(response, refreshToken);

        return TokenResponse.of(accessToken, accessExpiration);
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {

        // RT를 쿠키의 형태로 저장
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .maxAge(refreshExpiration / 1000) // 쿠키 저장 시간
                .path("/")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .maxAge(0)
                .path("/")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

}
