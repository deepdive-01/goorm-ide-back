package com.ide.project.domain.user.service;

import com.ide.project.domain.user.dto.response.ProfileImageResponse;
import com.ide.project.domain.user.dto.response.UserMeResponse;
import com.ide.project.domain.user.entity.LoginType;
import com.ide.project.domain.user.entity.Role;
import com.ide.project.domain.user.entity.User;
import com.ide.project.domain.user.repository.UserRepository;
import com.ide.project.global.exception.ErrorCode;
import com.ide.project.global.exception.custom.BusinessException;
import com.ide.project.global.util.RedisKeys;
import com.ide.project.integration.s3.S3Service;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private S3Service s3Service;

    @InjectMocks
    private UserService userService;

    private static final Long TEST_USER_ID = 1L;


    @Test
    @DisplayName("존재하지 않는 유저 ID로 탈퇴 요청 시 USER_NOT_FOUND 예외가 발생한다")
    void withdraw_userNotFound() {
        given(userRepository.findById(TEST_USER_ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.withdraw(TEST_USER_ID, mock(HttpServletResponse.class)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_NOT_FOUND);

        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    @DisplayName("이미 탈퇴한 계정으로 탈퇴 요청 시 INACTIVE_USER 예외가 발생한다")
    void withdraw_alreadyInactive() {
        User user = mock(User.class);
        given(user.isActive()).willReturn(false);
        given(userRepository.findById(TEST_USER_ID)).willReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.withdraw(TEST_USER_ID, mock(HttpServletResponse.class)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INACTIVE_USER);

        verify(user, never()).deactivate();
        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    @DisplayName("정상 탈퇴 시 계정이 비활성화되고 RT가 삭제되며 쿠키가 만료된다")
    void withdraw_success() {
        User user = mock(User.class);
        given(user.isActive()).willReturn(true);
        given(userRepository.findById(TEST_USER_ID)).willReturn(Optional.of(user));
        HttpServletResponse response = mock(HttpServletResponse.class);

        userService.withdraw(TEST_USER_ID, response);

        verify(user).deactivate();
        verify(redisTemplate).delete(RedisKeys.REFRESH_TOKEN + TEST_USER_ID);
        verify(response).addHeader(anyString(), anyString());
    }

    @Test
    @DisplayName("존재하지 않는 userId로 내 정보 조회 시 USER_NOT_FOUND 예외가 발생한다")
    void getMe_userNotFound() {
        given(userRepository.findById(TEST_USER_ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getMe(TEST_USER_ID))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("정상 조회 시 유저 정보가 담긴 UserMeResponse를 반환한다")
    void getMe_success() {
        User user = mock(User.class);
        given(user.getId()).willReturn(TEST_USER_ID);
        given(user.getEmail()).willReturn("test@test.com");
        given(user.getName()).willReturn("테스트");
        given(user.getNickname()).willReturn("테스터");
        given(user.getRole()).willReturn(Role.MENTOR);
        given(user.getLoginType()).willReturn(LoginType.LOCAL);
        given(user.getProfileImageUrl()).willReturn(null);
        given(user.getCreatedAt()).willReturn(LocalDateTime.of(2025, 1, 1, 0, 0));
        given(userRepository.findById(TEST_USER_ID)).willReturn(Optional.of(user));

        UserMeResponse response = userService.getMe(TEST_USER_ID);

        assertThat(response.id()).isEqualTo(TEST_USER_ID);
        assertThat(response.email()).isEqualTo("test@test.com");
        assertThat(response.role()).isEqualTo(Role.MENTOR);
        assertThat(response.loginType()).isEqualTo(LoginType.LOCAL);
        assertThat(response.profileImageUrl()).isNull();
    }

    @Test
    @DisplayName("존재하지 않는 userId로 이미지 업로드 시 USER_NOT_FOUND 예외가 발생한다")
    void uploadProfileImage_userNotFound() {
        given(userRepository.findById(TEST_USER_ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.uploadProfileImage(TEST_USER_ID, mock(MultipartFile.class)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("기존 이미지가 없을 때 업로드하면 S3에 저장 후 URL을 반환한다")
    void uploadProfileImage_noExistingImage() throws IOException {
        User user = mock(User.class);
        given(user.getProfileImageUrl()).willReturn(null);
        given(userRepository.findById(TEST_USER_ID)).willReturn(Optional.of(user));

        MultipartFile file = mock(MultipartFile.class);
        String expectedUrl = "https://coderun-storage.s3.ap-northeast-2.amazonaws.com/profiles/uuid.png";
        given(s3Service.upload(file, "profiles")).willReturn(expectedUrl);

        ProfileImageResponse response = userService.uploadProfileImage(TEST_USER_ID, file);

        verify(s3Service, never()).delete(anyString());
        verify(user).updateProfileImageUrl(expectedUrl);
        assertThat(response.profileImageUrl()).isEqualTo(expectedUrl);
    }

    @Test
    @DisplayName("기존 이미지가 있을 때 업로드하면 기존 이미지를 삭제 후 새 이미지를 저장한다")
    void uploadProfileImage_withExistingImage() throws IOException {
        String oldUrl = "https://coderun-storage.s3.ap-northeast-2.amazonaws.com/profiles/old.png";
        String newUrl = "https://coderun-storage.s3.ap-northeast-2.amazonaws.com/profiles/new.png";

        User user = mock(User.class);
        given(user.getProfileImageUrl()).willReturn(oldUrl);
        given(userRepository.findById(TEST_USER_ID)).willReturn(Optional.of(user));

        MultipartFile file = mock(MultipartFile.class);
        given(s3Service.upload(file, "profiles")).willReturn(newUrl);

        ProfileImageResponse response = userService.uploadProfileImage(TEST_USER_ID, file);

        verify(s3Service).delete(oldUrl);
        verify(user).updateProfileImageUrl(newUrl);
        assertThat(response.profileImageUrl()).isEqualTo(newUrl);
    }

    @Test
    @DisplayName("존재하지 않는 userId로 이미지 삭제 시 USER_NOT_FOUND 예외가 발생한다")
    void deleteProfileImage_userNotFound() {
        given(userRepository.findById(TEST_USER_ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteProfileImage(TEST_USER_ID))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("이미지가 없는 유저가 삭제 요청 시 S3 삭제 없이 그냥 종료된다")
    void deleteProfileImage_noImage() {
        User user = mock(User.class);
        given(user.getProfileImageUrl()).willReturn(null);
        given(userRepository.findById(TEST_USER_ID)).willReturn(Optional.of(user));

        userService.deleteProfileImage(TEST_USER_ID);

        verify(s3Service, never()).delete(anyString());
        verify(user, never()).updateProfileImageUrl(any());
    }

    @Test
    @DisplayName("이미지가 있는 유저가 삭제 요청 시 S3에서 삭제 후 URL을 null로 변경한다")
    void deleteProfileImage_success() {
        String imageUrl = "https://coderun-storage.s3.ap-northeast-2.amazonaws.com/profiles/test.png";

        User user = mock(User.class);
        given(user.getProfileImageUrl()).willReturn(imageUrl);
        given(userRepository.findById(TEST_USER_ID)).willReturn(Optional.of(user));

        userService.deleteProfileImage(TEST_USER_ID);

        verify(s3Service).delete(imageUrl);
        verify(user).updateProfileImageUrl(null);
    }
}
