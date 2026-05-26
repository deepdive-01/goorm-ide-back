package com.ide.project.domain.user.service;

import com.ide.project.domain.user.dto.response.UserMeResponse;
import com.ide.project.domain.user.entity.User;
import com.ide.project.domain.user.repository.UserRepository;
import com.ide.project.global.exception.ErrorCode;
import com.ide.project.global.exception.custom.BusinessException;
import com.ide.project.global.util.RedisKeys;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RedisTemplate<String, String> redisTemplate;

    // 회원 탈퇴 처리
    // isActive = false 로 변경하는 소프트 삭제 방식
    // 탈퇴와 동시에 RT 삭제 + 쿠키 만료로 자동 로그아웃 처리
    @Transactional
    public void withdraw(Long userId, HttpServletResponse response) {

        // 유저 조회 - 없으면 예외
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 이미 탈퇴한 계정이면 예외
        if (!user.isActive()) {
            throw new BusinessException(ErrorCode.INACTIVE_USER);
        }

        // isActive = false
        user.deactivate();

        // Redis에 저장된 RT 삭제
        redisTemplate.delete(RedisKeys.REFRESH_TOKEN + userId);

        // 브라우저 쿠키도 만료 처리
        ResponseCookie expiredCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .maxAge(0)
                .path("/")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, expiredCookie.toString());
    }

    public UserMeResponse getMe(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return new UserMeResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getNickname(),
                user.getRole(),
                user.getLoginType(),
                user.getProfileImageUrl(),
                user.getCreatedAt()
        );
    }
}
