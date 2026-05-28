package com.ide.project.domain.user.service;

import com.ide.project.domain.user.dto.response.ProfileImageResponse;
import com.ide.project.domain.user.dto.response.UserMeResponse;
import com.ide.project.domain.user.entity.User;
import com.ide.project.domain.user.repository.UserRepository;
import com.ide.project.global.exception.ErrorCode;
import com.ide.project.global.exception.custom.BusinessException;
import com.ide.project.global.util.RedisKeys;
import com.ide.project.integration.s3.S3Service;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class UserService {

    // 유저 조회 관련 레포
    private final UserRepository userRepository;

    // redis 관련
    private final RedisTemplate<String, String> redisTemplate;

    // S3 관련
    private final S3Service s3Service;

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

    @Transactional
    public ProfileImageResponse uploadProfileImage(Long userId, MultipartFile file) throws IOException {

        // 유저를 찾음
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 만약 유저가 기존 프로필 이미지를 가지고 있다면
        // 삭제하고 덮어씌우기
        if (user.getProfileImageUrl() != null) {
            s3Service.delete(user.getProfileImageUrl());
        }

        // 이미지를 업로드
        String imageUrl = s3Service.upload(file, "profiles");
        user.updateProfileImageUrl(imageUrl);

        // 응답 리턴
        return new ProfileImageResponse(imageUrl);
    }

    // 프로필 이미지 삭제
    @Transactional
    public void deleteProfileImage(Long userId) {

        // 유저를 찾고
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 유저가 이미지가 이미 없으면
        // 그냥 리턴
        if (user.getProfileImageUrl() == null) {
            return;
        }

        // 이미지 삭제 후 null 등록
        s3Service.delete(user.getProfileImageUrl());
        user.updateProfileImageUrl(null);
    }

}
