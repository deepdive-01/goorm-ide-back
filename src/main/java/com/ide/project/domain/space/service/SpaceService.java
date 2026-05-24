package com.ide.project.domain.space.service;

import com.ide.project.domain.space.dto.request.SpaceCreateRequest;
import com.ide.project.domain.space.dto.request.SpaceInviteEmailRequest;
import com.ide.project.domain.space.dto.request.SpaceJoinRequest;
import com.ide.project.domain.space.dto.request.SpaceUpdateRequest;
import com.ide.project.domain.space.dto.response.*;
import com.ide.project.domain.space.entity.Space;
import com.ide.project.domain.space.entity.SpaceMember;
import com.ide.project.domain.space.repository.SpaceMemberRepository;
import com.ide.project.domain.space.repository.SpaceRepository;
import com.ide.project.domain.user.entity.Role;
import com.ide.project.domain.user.entity.User;
import com.ide.project.domain.user.repository.UserRepository;
import com.ide.project.global.exception.ErrorCode;
import com.ide.project.global.exception.custom.BusinessException;
import com.ide.project.integration.mail.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SpaceService {
    private final SpaceRepository spaceRepository;
    private final SpaceMemberRepository spaceMemberRepository;
    private final UserRepository userRepository;
    private final MailService mailService;

    // 워크스페이스 생성
    @Transactional
    public SpaceCreateResponse createSpace(SpaceCreateRequest request, Long userId) {

        // 유저를 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 멘토만 워크스페이스를 생성할 수 있게
        if (user.getRole() != Role.MENTOR) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        // space를 생성
        Space space = Space.builder()
                .mentor(user)
                .name(request.name())
                .description(request.description())
                .inviteCode(generateInviteCode())
                .build();

        // 생성된 space 엔티티를, 레포에 저장
        Space saved = spaceRepository.save(space);

        // 저장된 값을 response DTO에 맞춰서 반환
        return new SpaceCreateResponse(
                saved.getId(),
                saved.getName(),
                saved.getDescription(),
                saved.isPublic(),
                saved.getInviteCode(),
                saved.isActive(),
                saved.getCreatedAt()
        );
    }


    // 내 워크스페이스 목록 조회
    public List<SpaceListItemResponse> getMySpaces(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (user.getRole() == Role.MENTOR) {
            return spaceRepository.findByMentor(user).stream()
                    .map(this::toListItemResponse)
                    .toList();
        }

        if (user.getRole() == Role.STUDENT) {
            return spaceMemberRepository.findByUserId(userId).stream()
                    .map(sm -> toListItemResponse(sm.getSpace()))
                    .toList();
        }

        return List.of();
    }

    // 스페이스의 디테일 정보를 조회
    public SpaceDetailResponse getSpace(Long spaceId, Long userId) {

        // 스페이스를 조회
        Space space = spaceRepository.findById(spaceId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SPACE_NOT_FOUND));


        // 멘토인지를 확인
        boolean isMentor = equalMentorsAndMembers(space, userId);

        // 멤버의 수를 카운트: 스페이스 ID를 통해 해당 스페이스의 학생들을 조회하고 사이즈를 반환
        int memberCount = spaceMemberRepository.findBySpaceId(spaceId).size();

        // SpaceDetailResponse를 반환
        return new SpaceDetailResponse(
                space.getId(),
                space.getName(),
                space.getDescription(),
                new SpaceDetailResponse.MentorInfo(
                        space.getMentor().getId(),
                        space.getMentor().getNickname()
                ),
                isMentor ? space.getInviteCode() : null, // 멘토라면, 스페이스의 초대 코드도 반환
                memberCount,
                space.isPublic(),
                space.isActive(),
                space.getCreatedAt()
        );
    }

    // 스페이스 업데이트
    @Transactional
    public SpaceUpdateResponse updateSpace(Long spaceId, SpaceUpdateRequest request, Long userId) {

        // 스페이스를 조회
        Space space = spaceRepository.findById(spaceId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SPACE_NOT_FOUND));

        if (!space.getMentor().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        // space의 update를 통해 변강할 수 있는 내용만 처리 -> null일 경우 기존 값을 유지하도록 구성했어요
        space.update(request.name(), request.description(), request.isActive());

        return new SpaceUpdateResponse(
                space.getId(),
                space.getName(),
                space.getDescription(),
                space.getUpdatedAt()
        );
    }

    // 초대코드로 워크스페이스 참여
    @Transactional
    public SpaceJoinResponse joinSpace(SpaceJoinRequest request, Long userId) {

        // 유저를 우선 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 학생만 초대 코드로 스페이스에 참여할 수 있음
        if (user.getRole() != Role.STUDENT) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }


        // 스페이스의 초대코드가 올바른지, 확인
        Space space = spaceRepository.findByInviteCode(request.inviteCode())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INVITE_CODE));

        // 만약 스페이스가 활성화가 되지 않았다면 오류를 반환
        if (!space.isActive()) {
            throw new BusinessException(ErrorCode.SPACE_NOT_FOUND);
        }

        // 스페이스에 이미 존재하는 유저일 경우도 오류를 반환
        if (spaceMemberRepository.existsBySpaceIdAndUserId(space.getId(), userId)) {
            throw new BusinessException(ErrorCode.ALREADY_JOINED);
        }

        // 스페이스 멤버에 해당 유저를 저장
        SpaceMember saved = spaceMemberRepository.save(
                SpaceMember.builder().space(space).user(user).build()
        );

        // 응답 값을 반환
        return new SpaceJoinResponse(space.getId(), space.getName(), saved.getJoinedAt());
    }

    // 스페이스 내 멤버 목록 조회
    public SpaceMemberListResponse getSpaceMembers(Long spaceId, Long userId) {

        Space space = spaceRepository.findById(spaceId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.SPACE_NOT_FOUND));

        equalMentorsAndMembers(space, userId);

        List<SpaceMemberListResponse.MemberInfo> members = spaceMemberRepository.findBySpaceId(spaceId).stream()
                .map(sm -> new SpaceMemberListResponse.MemberInfo(
                        sm.getUser().getId(),
                        sm.getUser().getNickname(),
                        sm.getUser().getProfileImageUrl(),
                        sm.getJoinedAt()
                ))
                .toList();
        return new SpaceMemberListResponse(space.getId(), space.getName(), members.size(), members);
    }

    // 초대코드 이메일 발송
    public SpaceInviteEmailResponse sendInviteEmail(Long spaceId, SpaceInviteEmailRequest request, Long userId) {

        // 스페이스를 조회
        Space space = spaceRepository.findById(spaceId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SPACE_NOT_FOUND));

        // 본인의 스페이스가 아닐 경우 오류를 반환
        if (!space.getMentor().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        // request의 emails를 순회하며 각각 email 주소에 메일을 전송
        for (String email : request.emails()) {
            mailService.sendSpaceInvite(email, space.getName(), space.getInviteCode());
        }

        // 응답 값을 반환
        return new SpaceInviteEmailResponse(space.getId(), space.getName(), request.emails().size());
    }

    // 스페이스 목록 응답 반환
    private SpaceListItemResponse toListItemResponse(Space space) {

        // 멤버 카운트: 스페이스 멤버 DB에서 스페이스를 조회하고, 해당 스페이스 ID의 사이즈를 조회
        int memberCount = spaceMemberRepository.findBySpaceId(space.getId()).size();

        return new SpaceListItemResponse(
                space.getId(),
                space.getName(),
                space.getDescription(),
                memberCount,
                space.isActive(),
                space.getCreatedAt()
        );
    }

    // 멘토인지, 학생인지를 확인하는 함수
    private boolean equalMentorsAndMembers(Space space, Long userId) {
        boolean isMentor = space.getMentor().getId().equals(userId);
        boolean isMember = spaceMemberRepository.existsBySpaceIdAndUserId(space.getId(), userId);

        if (!isMentor && !isMember) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        return isMentor;
    }

    // 랜덤한 숫자 + 영어 조합의 초대 코드 생성
    private String generateInviteCode() {
        String code;
        do {
            code = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        } while (spaceRepository.existsByInviteCode(code));
        return code;
    }

}
