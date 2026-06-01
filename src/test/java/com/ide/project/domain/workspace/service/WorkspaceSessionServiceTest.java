package com.ide.project.domain.workspace.service;

import com.ide.project.domain.workspace.dto.request.EnterRequest;
import com.ide.project.domain.workspace.dto.response.ParticipantEventMessage;
import com.ide.project.global.util.RedisKeys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkspaceSessionServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private SetOperations<String, String> setOperations;

    @InjectMocks
    private WorkspaceSessionService workspaceSessionService;

    private static final String SESSION_ID = "abc123";
    private static final Long SPACE_ID = 42L;
    private static final Long USER_ID = 5L;
    private static final String NICKNAME = "홍길동";

    // ── enter ─────────────────────────────────────────────────

    @Test
    @DisplayName("입장 시 Redis에 세션과 참여자가 저장되고 ENTER 메시지가 반환된다")
    void enter_success() {
        // Given
        EnterRequest request = new EnterRequest(USER_ID, NICKNAME);
        String membersKey = RedisKeys.SPACE_MEMBERS + SPACE_ID;

        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(redisTemplate.opsForSet()).willReturn(setOperations);
        given(setOperations.members(membersKey)).willReturn(Set.of(NICKNAME));

        // When
        ParticipantEventMessage result = workspaceSessionService.enter(SESSION_ID, SPACE_ID, request);

        // Then
        assertThat(result.type()).isEqualTo("ENTER");
        assertThat(result.spaceId()).isEqualTo(SPACE_ID);
        assertThat(result.userId()).isEqualTo(USER_ID);
        assertThat(result.nickname()).isEqualTo(NICKNAME);
        assertThat(result.participants()).contains(NICKNAME);

        verify(valueOperations).set(
                RedisKeys.SPACE_SESSION + SESSION_ID,
                SPACE_ID + ":" + USER_ID + ":" + NICKNAME
        );
        verify(setOperations).add(membersKey, NICKNAME);
    }

    @Test
    @DisplayName("입장 시 이미 다른 참여자가 있으면 전체 참여자 목록이 반환된다")
    void enter_withExistingParticipants() {
        // Given
        EnterRequest request = new EnterRequest(USER_ID, NICKNAME);
        String membersKey = RedisKeys.SPACE_MEMBERS + SPACE_ID;

        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(redisTemplate.opsForSet()).willReturn(setOperations);
        given(setOperations.members(membersKey)).willReturn(Set.of("김철수", NICKNAME));

        // When
        ParticipantEventMessage result = workspaceSessionService.enter(SESSION_ID, SPACE_ID, request);

        // Then
        assertThat(result.participants()).hasSize(2);
        assertThat(result.participants()).contains("김철수", NICKNAME);
    }

    // ── leave ─────────────────────────────────────────────────

    @Test
    @DisplayName("존재하지 않는 세션으로 퇴장 시 빈 Optional이 반환된다")
    void leave_sessionNotFound() {
        // Given
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get(RedisKeys.SPACE_SESSION + SESSION_ID)).willReturn(null);

        // When
        Optional<ParticipantEventMessage> result = workspaceSessionService.leave(SESSION_ID);

        // Then
        assertThat(result).isEmpty();
        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    @DisplayName("퇴장 시 Redis에서 세션이 삭제되고 LEAVE 메시지가 반환된다")
    void leave_success() {
        // Given
        String sessionKey = RedisKeys.SPACE_SESSION + SESSION_ID;
        String membersKey = RedisKeys.SPACE_MEMBERS + SPACE_ID;
        String storedValue = SPACE_ID + ":" + USER_ID + ":" + NICKNAME;

        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(redisTemplate.opsForSet()).willReturn(setOperations);
        given(valueOperations.get(sessionKey)).willReturn(storedValue);
        given(setOperations.members(membersKey)).willReturn(Set.of());

        // When
        Optional<ParticipantEventMessage> result = workspaceSessionService.leave(SESSION_ID);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().type()).isEqualTo("LEAVE");
        assertThat(result.get().spaceId()).isEqualTo(SPACE_ID);
        assertThat(result.get().userId()).isEqualTo(USER_ID);
        assertThat(result.get().nickname()).isEqualTo(NICKNAME);
        assertThat(result.get().participants()).isEmpty();

        verify(redisTemplate).delete(sessionKey);
        verify(setOperations).remove(membersKey, NICKNAME);
    }

    @Test
    @DisplayName("퇴장 후 남은 참여자가 있으면 참여자 목록에 포함된다")
    void leave_withRemainingParticipants() {
        // Given
        String sessionKey = RedisKeys.SPACE_SESSION + SESSION_ID;
        String membersKey = RedisKeys.SPACE_MEMBERS + SPACE_ID;
        String storedValue = SPACE_ID + ":" + USER_ID + ":" + NICKNAME;

        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(redisTemplate.opsForSet()).willReturn(setOperations);
        given(valueOperations.get(sessionKey)).willReturn(storedValue);
        given(setOperations.members(membersKey)).willReturn(Set.of("김철수"));

        // When
        Optional<ParticipantEventMessage> result = workspaceSessionService.leave(SESSION_ID);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().participants()).containsExactly("김철수");
    }
}
