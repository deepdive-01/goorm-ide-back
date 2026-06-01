package com.ide.project.domain.space.service;

import com.ide.project.domain.space.dto.request.*;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpaceServiceTest {

    @Mock private SpaceRepository spaceRepository;
    @Mock private SpaceMemberRepository spaceMemberRepository;
    @Mock private UserRepository userRepository;
    @Mock private MailService mailService;

    @InjectMocks private SpaceService spaceService;

    private static final Long MENTOR_ID = 1L;
    private static final Long STUDENT_ID = 2L;
    private static final Long OTHER_ID = 3L;
    private static final Long SPACE_ID = 10L;


    @Test
    @DisplayName("존재하지 않는 유저가 워크스페이스 생성 시 USER_NOT_FOUND 예외가 발생한다")
    void createSpace_userNotFound() {
        given(userRepository.findById(MENTOR_ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> spaceService.createSpace(new SpaceCreateRequest("테스트", "설명"), MENTOR_ID))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("STUDENT가 워크스페이스 생성 시 FORBIDDEN 예외가 발생한다")
    void createSpace_forbidden() {
        User student = mock(User.class);
        given(student.getRole()).willReturn(Role.STUDENT);
        given(userRepository.findById(STUDENT_ID)).willReturn(Optional.of(student));

        assertThatThrownBy(() -> spaceService.createSpace(new SpaceCreateRequest("테스트", "설명"), STUDENT_ID))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.FORBIDDEN);
    }

    @Test
    @DisplayName("MENTOR가 워크스페이스 생성 시 스페이스가 저장되고 응답 DTO를 반환한다")
    void createSpace_success() {
        User mentor = mock(User.class);
        given(mentor.getRole()).willReturn(Role.MENTOR);
        given(userRepository.findById(MENTOR_ID)).willReturn(Optional.of(mentor));
        given(spaceRepository.existsByInviteCode(anyString())).willReturn(false);

        Space saved = mock(Space.class);
        given(saved.getId()).willReturn(SPACE_ID);
        given(saved.getName()).willReturn("테스트 스페이스");
        given(saved.getInviteCode()).willReturn("ABCD1234");
        given(saved.isActive()).willReturn(true);
        given(saved.getCreatedAt()).willReturn(LocalDateTime.now());
        given(spaceRepository.save(any(Space.class))).willReturn(saved);

        SpaceCreateResponse response = spaceService.createSpace(new SpaceCreateRequest("테스트 스페이스", "설명"), MENTOR_ID);

        assertThat(response.id()).isEqualTo(SPACE_ID);
        assertThat(response.name()).isEqualTo("테스트 스페이스");
        assertThat(response.isActive()).isTrue();
        assertThat(response.inviteCode()).isEqualTo("ABCD1234");
        verify(spaceRepository).save(any(Space.class));
    }


    @Test
    @DisplayName("MENTOR가 목록 조회 시 본인이 생성한 스페이스 목록을 반환한다")
    void getMySpaces_mentor() {
        User mentor = mock(User.class);
        given(mentor.getRole()).willReturn(Role.MENTOR);
        given(userRepository.findById(MENTOR_ID)).willReturn(Optional.of(mentor));

        Space space = mock(Space.class);
        given(space.getId()).willReturn(SPACE_ID);
        given(spaceRepository.findByMentor(mentor)).willReturn(List.of(space));
        given(spaceMemberRepository.findBySpaceId(SPACE_ID)).willReturn(List.of());

        List<SpaceListItemResponse> result = spaceService.getMySpaces(MENTOR_ID);

        assertThat(result).hasSize(1);
        verify(spaceRepository).findByMentor(mentor);
        verify(spaceMemberRepository, never()).findByUserId(any());
    }

    @Test
    @DisplayName("STUDENT가 목록 조회 시 본인이 참여한 스페이스 목록을 반환한다")
    void getMySpaces_student() {
        User student = mock(User.class);
        given(student.getRole()).willReturn(Role.STUDENT);
        given(userRepository.findById(STUDENT_ID)).willReturn(Optional.of(student));

        Space space = mock(Space.class);
        given(space.getId()).willReturn(SPACE_ID);

        SpaceMember spaceMember = mock(SpaceMember.class);
        given(spaceMember.getSpace()).willReturn(space);
        given(spaceMemberRepository.findByUserId(STUDENT_ID)).willReturn(List.of(spaceMember));
        given(spaceMemberRepository.findBySpaceId(SPACE_ID)).willReturn(List.of(spaceMember));

        List<SpaceListItemResponse> result = spaceService.getMySpaces(STUDENT_ID);

        assertThat(result).hasSize(1);
        verify(spaceMemberRepository).findByUserId(STUDENT_ID);
        verify(spaceRepository, never()).findByMentor(any());
    }


    @Test
    @DisplayName("존재하지 않는 스페이스 조회 시 SPACE_NOT_FOUND 예외가 발생한다")
    void getSpace_notFound() {
        given(spaceRepository.findById(SPACE_ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> spaceService.getSpace(SPACE_ID, MENTOR_ID))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.SPACE_NOT_FOUND);
    }

    @Test
    @DisplayName("멘토도 멤버도 아닌 유저가 조회 시 FORBIDDEN 예외가 발생한다")
    void getSpace_forbidden() {
        User mentor = mock(User.class);
        given(mentor.getId()).willReturn(MENTOR_ID);

        Space space = mock(Space.class);
        given(space.getId()).willReturn(SPACE_ID);
        given(space.getMentor()).willReturn(mentor);
        given(spaceRepository.findById(SPACE_ID)).willReturn(Optional.of(space));
        given(spaceMemberRepository.existsBySpaceIdAndUserId(SPACE_ID, OTHER_ID)).willReturn(false);

        assertThatThrownBy(() -> spaceService.getSpace(SPACE_ID, OTHER_ID))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.FORBIDDEN);
    }

    @Test
    @DisplayName("멘토가 조회 시 inviteCode가 응답에 포함된다")
    void getSpace_mentorSeesInviteCode() {
        User mentor = mock(User.class);
        given(mentor.getId()).willReturn(MENTOR_ID);
        given(mentor.getNickname()).willReturn("멘토");

        Space space = mock(Space.class);
        given(space.getId()).willReturn(SPACE_ID);
        given(space.getMentor()).willReturn(mentor);
        given(space.getInviteCode()).willReturn("ABCD1234");
        given(spaceRepository.findById(SPACE_ID)).willReturn(Optional.of(space));
        given(spaceMemberRepository.findBySpaceId(SPACE_ID)).willReturn(List.of());

        SpaceDetailResponse response = spaceService.getSpace(SPACE_ID, MENTOR_ID);

        assertThat(response.inviteCode()).isEqualTo("ABCD1234");
    }

    @Test
    @DisplayName("멤버가 조회 시 inviteCode는 null이다")
    void getSpace_memberDoesNotSeeInviteCode() {
        User mentor = mock(User.class);
        given(mentor.getId()).willReturn(MENTOR_ID);
        given(mentor.getNickname()).willReturn("멘토");

        Space space = mock(Space.class);
        given(space.getId()).willReturn(SPACE_ID);
        given(space.getMentor()).willReturn(mentor);
        given(spaceRepository.findById(SPACE_ID)).willReturn(Optional.of(space));
        given(spaceMemberRepository.existsBySpaceIdAndUserId(SPACE_ID, STUDENT_ID)).willReturn(true);
        given(spaceMemberRepository.findBySpaceId(SPACE_ID)).willReturn(List.of());

        SpaceDetailResponse response = spaceService.getSpace(SPACE_ID, STUDENT_ID);

        assertThat(response.inviteCode()).isNull();
    }


    @Test
    @DisplayName("스페이스 소유자가 아닌 경우 수정 시 FORBIDDEN 예외가 발생한다")
    void updateSpace_forbidden() {
        User mentor = mock(User.class);
        given(mentor.getId()).willReturn(MENTOR_ID);

        Space space = mock(Space.class);
        given(space.getMentor()).willReturn(mentor);
        given(spaceRepository.findById(SPACE_ID)).willReturn(Optional.of(space));

        assertThatThrownBy(() -> spaceService.updateSpace(SPACE_ID, new SpaceUpdateRequest("새이름", null, null), OTHER_ID))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.FORBIDDEN);
    }

    @Test
    @DisplayName("스페이스 소유자가 수정 시 space.update()가 호출되고 응답 DTO를 반환한다")
    void updateSpace_success() {
        User mentor = mock(User.class);
        given(mentor.getId()).willReturn(MENTOR_ID);

        Space space = mock(Space.class);
        given(space.getId()).willReturn(SPACE_ID);
        given(space.getMentor()).willReturn(mentor);
        given(space.getName()).willReturn("새이름");
        given(space.getUpdatedAt()).willReturn(LocalDateTime.now());
        given(spaceRepository.findById(SPACE_ID)).willReturn(Optional.of(space));

        SpaceUpdateResponse response = spaceService.updateSpace(SPACE_ID, new SpaceUpdateRequest("새이름", null, null), MENTOR_ID);

        verify(space).update("새이름", null, null);
        assertThat(response.name()).isEqualTo("새이름");
    }


    @Test
    @DisplayName("MENTOR가 참여 시도 시 FORBIDDEN 예외가 발생한다")
    void joinSpace_mentorForbidden() {
        User mentor = mock(User.class);
        given(mentor.getRole()).willReturn(Role.MENTOR);
        given(userRepository.findById(MENTOR_ID)).willReturn(Optional.of(mentor));

        assertThatThrownBy(() -> spaceService.joinSpace(new SpaceJoinRequest("ABCD1234"), MENTOR_ID))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.FORBIDDEN);
    }

    @Test
    @DisplayName("존재하지 않는 초대코드로 참여 시 INVALID_INVITE_CODE 예외가 발생한다")
    void joinSpace_invalidInviteCode() {
        User student = mock(User.class);
        given(student.getRole()).willReturn(Role.STUDENT);
        given(userRepository.findById(STUDENT_ID)).willReturn(Optional.of(student));
        given(spaceRepository.findByInviteCode("INVALID")).willReturn(Optional.empty());

        assertThatThrownBy(() -> spaceService.joinSpace(new SpaceJoinRequest("INVALID"), STUDENT_ID))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_INVITE_CODE);
    }

    @Test
    @DisplayName("비활성화된 스페이스의 초대코드로 참여 시 SPACE_NOT_FOUND 예외가 발생한다")
    void joinSpace_inactiveSpace() {
        User student = mock(User.class);
        given(student.getRole()).willReturn(Role.STUDENT);
        given(userRepository.findById(STUDENT_ID)).willReturn(Optional.of(student));

        Space space = mock(Space.class);
        given(space.isActive()).willReturn(false);
        given(spaceRepository.findByInviteCode("ABCD1234")).willReturn(Optional.of(space));

        assertThatThrownBy(() -> spaceService.joinSpace(new SpaceJoinRequest("ABCD1234"), STUDENT_ID))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.SPACE_NOT_FOUND);
    }

    @Test
    @DisplayName("이미 참여한 스페이스에 재참여 시 ALREADY_JOINED 예외가 발생한다")
    void joinSpace_alreadyJoined() {
        User student = mock(User.class);
        given(student.getRole()).willReturn(Role.STUDENT);
        given(userRepository.findById(STUDENT_ID)).willReturn(Optional.of(student));

        Space space = mock(Space.class);
        given(space.getId()).willReturn(SPACE_ID);
        given(space.isActive()).willReturn(true);
        given(spaceRepository.findByInviteCode("ABCD1234")).willReturn(Optional.of(space));
        given(spaceMemberRepository.existsBySpaceIdAndUserId(SPACE_ID, STUDENT_ID)).willReturn(true);

        assertThatThrownBy(() -> spaceService.joinSpace(new SpaceJoinRequest("ABCD1234"), STUDENT_ID))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ALREADY_JOINED);
    }

    @Test
    @DisplayName("정상적인 참여 요청 시 SpaceMember가 저장되고 응답 DTO를 반환한다")
    void joinSpace_success() {
        User student = mock(User.class);
        given(student.getRole()).willReturn(Role.STUDENT);
        given(userRepository.findById(STUDENT_ID)).willReturn(Optional.of(student));

        Space space = mock(Space.class);
        given(space.getId()).willReturn(SPACE_ID);
        given(space.getName()).willReturn("테스트 스페이스");
        given(space.isActive()).willReturn(true);
        given(spaceRepository.findByInviteCode("ABCD1234")).willReturn(Optional.of(space));
        given(spaceMemberRepository.existsBySpaceIdAndUserId(SPACE_ID, STUDENT_ID)).willReturn(false);

        SpaceMember savedMember = mock(SpaceMember.class);
        given(savedMember.getJoinedAt()).willReturn(LocalDateTime.now());
        given(spaceMemberRepository.save(any(SpaceMember.class))).willReturn(savedMember);

        SpaceJoinResponse response = spaceService.joinSpace(new SpaceJoinRequest("ABCD1234"), STUDENT_ID);

        assertThat(response.spaceId()).isEqualTo(SPACE_ID);
        assertThat(response.spaceName()).isEqualTo("테스트 스페이스");
        verify(spaceMemberRepository).save(any(SpaceMember.class));
    }


    @Test
    @DisplayName("멤버 목록 조회 시 멘토도 멤버도 아니면 FORBIDDEN 예외가 발생한다")
    void getSpaceMembers_forbidden() {
        User mentor = mock(User.class);
        given(mentor.getId()).willReturn(MENTOR_ID);

        Space space = mock(Space.class);
        given(space.getId()).willReturn(SPACE_ID);
        given(space.getMentor()).willReturn(mentor);
        given(spaceRepository.findById(SPACE_ID)).willReturn(Optional.of(space));
        given(spaceMemberRepository.existsBySpaceIdAndUserId(SPACE_ID, OTHER_ID)).willReturn(false);

        assertThatThrownBy(() -> spaceService.getSpaceMembers(SPACE_ID, OTHER_ID))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.FORBIDDEN);
    }

    @Test
    @DisplayName("멘토가 멤버 목록 조회 시 전체 멤버 정보를 반환한다")
    void getSpaceMembers_success() {
        User mentor = mock(User.class);
        given(mentor.getId()).willReturn(MENTOR_ID);

        Space space = mock(Space.class);
        given(space.getId()).willReturn(SPACE_ID);
        given(space.getName()).willReturn("테스트 스페이스");
        given(space.getMentor()).willReturn(mentor);
        given(spaceRepository.findById(SPACE_ID)).willReturn(Optional.of(space));

        User student = mock(User.class);
        given(student.getId()).willReturn(STUDENT_ID);
        given(student.getNickname()).willReturn("학생");
        given(student.getProfileImageUrl()).willReturn(null);

        SpaceMember spaceMember = mock(SpaceMember.class);
        given(spaceMember.getUser()).willReturn(student);
        given(spaceMember.getJoinedAt()).willReturn(LocalDateTime.now());
        given(spaceMemberRepository.findBySpaceId(SPACE_ID)).willReturn(List.of(spaceMember));

        SpaceMemberListResponse response = spaceService.getSpaceMembers(SPACE_ID, MENTOR_ID);

        assertThat(response.spaceId()).isEqualTo(SPACE_ID);
        assertThat(response.memberCount()).isEqualTo(1);
        assertThat(response.members()).hasSize(1);
        assertThat(response.members().get(0).nickname()).isEqualTo("학생");
    }


    @Test
    @DisplayName("스페이스 소유자가 아닌 경우 이메일 발송 시 FORBIDDEN 예외가 발생한다")
    void sendInviteEmail_forbidden() {
        User mentor = mock(User.class);
        given(mentor.getId()).willReturn(MENTOR_ID);

        Space space = mock(Space.class);
        given(space.getMentor()).willReturn(mentor);
        given(spaceRepository.findById(SPACE_ID)).willReturn(Optional.of(space));

        assertThatThrownBy(() -> spaceService.sendInviteEmail(SPACE_ID, new SpaceInviteEmailRequest(List.of("test@test.com")), OTHER_ID))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.FORBIDDEN);
    }

    @Test
    @DisplayName("이메일 발송 시 emails 수만큼 mailService가 호출되고 sentCount를 반환한다")
    void sendInviteEmail_success() {
        User mentor = mock(User.class);
        given(mentor.getId()).willReturn(MENTOR_ID);

        Space space = mock(Space.class);
        given(space.getId()).willReturn(SPACE_ID);
        given(space.getName()).willReturn("테스트 스페이스");
        given(space.getMentor()).willReturn(mentor);
        given(space.getInviteCode()).willReturn("ABCD1234");
        given(spaceRepository.findById(SPACE_ID)).willReturn(Optional.of(space));

        SpaceInviteEmailResponse response = spaceService.sendInviteEmail(
                SPACE_ID,
                new SpaceInviteEmailRequest(List.of("a@test.com", "b@test.com")),
                MENTOR_ID
        );

        verify(mailService, times(2)).sendSpaceInvite(anyString(), eq("테스트 스페이스"), eq("ABCD1234"));
        assertThat(response.sentCount()).isEqualTo(2);
        assertThat(response.spaceId()).isEqualTo(SPACE_ID);
    }
}
