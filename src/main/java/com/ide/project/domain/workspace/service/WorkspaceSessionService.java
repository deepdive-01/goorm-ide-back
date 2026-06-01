package com.ide.project.domain.workspace.service;

import com.ide.project.domain.workspace.dto.request.EnterRequest;
import com.ide.project.domain.workspace.dto.response.ParticipantEventMessage;
import com.ide.project.global.util.RedisKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class WorkspaceSessionService {

    private final RedisTemplate<String, String> redisTemplate;

    public ParticipantEventMessage enter(String sessionId, Long spaceId, EnterRequest request) {
        // sessionId → "spaceId:userId:nickname" 저장 (나중에 퇴장 시 꺼내 쓰기 위함)
        String sessionKey = RedisKeys.SPACE_SESSION + sessionId;
        redisTemplate.opsForValue().set(sessionKey, spaceId + ":" + request.userId() + ":" + request.nickname());

        // 해당 방의 참여자 Set에 닉네임 추가
        String membersKey = RedisKeys.SPACE_MEMBERS + spaceId;
        redisTemplate.opsForSet().add(membersKey, request.nickname());

        List<String> participants = getParticipants(spaceId);
        return new ParticipantEventMessage(spaceId, "ENTER", request.userId(), request.nickname(), participants);
    }

    public Optional<ParticipantEventMessage> leave(String sessionId) {
        String sessionKey = RedisKeys.SPACE_SESSION + sessionId;
        String value = redisTemplate.opsForValue().get(sessionKey);

        if (value == null) {
            return Optional.empty();
        }

        String[] parts = value.split(":", 3);
        Long spaceId = Long.parseLong(parts[0]);
        Long userId = Long.parseLong(parts[1]);
        String nickname = parts[2];

        // 세션 삭제
        redisTemplate.delete(sessionKey);

        // 참여자 목록에서 제거
        String membersKey = RedisKeys.SPACE_MEMBERS + spaceId;
        redisTemplate.opsForSet().remove(membersKey, nickname);

        List<String> participants = getParticipants(spaceId);
        return Optional.of(new ParticipantEventMessage(spaceId, "LEAVE", userId, nickname, participants));
    }

    private List<String> getParticipants(Long spaceId) {
        String membersKey = RedisKeys.SPACE_MEMBERS + spaceId;
        Set<String> members = redisTemplate.opsForSet().members(membersKey);
        return members == null ? List.of() : List.copyOf(members);
    }
}
