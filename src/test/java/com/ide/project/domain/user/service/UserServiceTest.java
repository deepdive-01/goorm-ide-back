package com.ide.project.domain.user.service;

import com.ide.project.domain.user.entity.User;
import com.ide.project.domain.user.repository.UserRepository;
import com.ide.project.global.exception.ErrorCode;
import com.ide.project.global.exception.custom.BusinessException;
import com.ide.project.global.util.RedisKeys;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @InjectMocks
    private UserService userService;

    private static final Long TEST_USER_ID = 1L;

    @Test
    @DisplayName("존재하지 않는 유저 ID로 탈퇴 요청 시 USER_NOT_FOUND 예외가 발생한다")
    void withdraw_userNotFound() {
        // Given: DB에 해당 유저가 없는 상황
        given(userRepository.findById(TEST_USER_ID)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.withdraw(TEST_USER_ID, mock(HttpServletResponse.class)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_NOT_FOUND);

        // 이미 예외가 발생했으므로 Redis 삭제가 호출되지 않아야 함
        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    @DisplayName("이미 탈퇴한 계정으로 탈퇴 요청 시 INACTIVE_USER 예외가 발생한다")
    void withdraw_alreadyInactive() {
        // Given: 이미 비활성화(탈퇴)된 유저
        User user = mock(User.class);
        given(user.isActive()).willReturn(false);
        given(userRepository.findById(TEST_USER_ID)).willReturn(Optional.of(user));

        // When & Then
        assertThatThrownBy(() -> userService.withdraw(TEST_USER_ID, mock(HttpServletResponse.class)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INACTIVE_USER);

        // 이미 탈퇴한 계정이므로 deactivate()가 다시 호출되면 안 됨
        verify(user, never()).deactivate();
        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    @DisplayName("정상 탈퇴 시 계정이 비활성화되고 RT가 삭제되며 쿠키가 만료된다")
    void withdraw_success() {
        // Given: 정상 활성 유저
        User user = mock(User.class);
        given(user.isActive()).willReturn(true);
        given(userRepository.findById(TEST_USER_ID)).willReturn(Optional.of(user));
        HttpServletResponse response = mock(HttpServletResponse.class);

        // When
        userService.withdraw(TEST_USER_ID, response);

        // Then: 호출 확인
        verify(user).deactivate();

        // Then: Redis에서 RT 삭제 확인
        verify(redisTemplate).delete(RedisKeys.REFRESH_TOKEN + TEST_USER_ID);

        // Then: 쿠키 만료 헤더가 응답에 추가됐는지 확인
        verify(response).addHeader(anyString(), anyString());
    }
}
