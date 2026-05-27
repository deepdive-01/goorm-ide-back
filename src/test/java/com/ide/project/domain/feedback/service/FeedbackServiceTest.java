package com.ide.project.domain.feedback.service;

import com.ide.project.domain.feedback.dto.request.CommentCreateRequest;
import com.ide.project.domain.feedback.dto.request.FeedbackUpdateRequest;
import com.ide.project.domain.feedback.dto.request.HighlightCreateRequest;
import com.ide.project.domain.feedback.dto.response.FeedbackResponse;
import com.ide.project.domain.feedback.entity.Feedback;
import com.ide.project.domain.feedback.entity.FeedbackType;
import com.ide.project.domain.feedback.repository.FeedbackRepository;
import com.ide.project.domain.user.entity.User;
import com.ide.project.domain.user.repository.UserRepository;
import com.ide.project.global.exception.ErrorCode;
import com.ide.project.global.exception.custom.BusinessException;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeedbackServiceTest {

    @Mock
    private FeedbackRepository feedbackRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FeedbackService feedbackService;

    private static final Long MENTOR_ID = 1L;
    private static final Long SUBMISSION_ID = 10L;
    private static final Long FEEDBACK_ID = 100L;

    // ── createComment ─────────────────────────────────────────

    @Test
    @DisplayName("존재하지 않는 mentorId로 코멘트 작성 시 USER_NOT_FOUND 예외가 발생한다")
    void createComment_userNotFound() {
        // Given
        given(userRepository.findById(MENTOR_ID)).willReturn(Optional.empty());

        CommentCreateRequest request = new CommentCreateRequest(SUBMISSION_ID, "좋은 코드입니다.");

        // When & Then
        assertThatThrownBy(() -> feedbackService.createComment(MENTOR_ID, request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_NOT_FOUND);

        verify(feedbackRepository, never()).save(any());
    }

    @Test
    @DisplayName("정상 요청 시 COMMENT 타입 피드백이 저장되고 반환된다")
    void createComment_success() {
        // Given
        User mentor = mock(User.class);
        given(mentor.getNickname()).willReturn("세현");
        given(userRepository.findById(MENTOR_ID)).willReturn(Optional.of(mentor));

        Feedback saved = mock(Feedback.class);
        given(saved.getId()).willReturn(FEEDBACK_ID);
        given(saved.getSubmissionId()).willReturn(SUBMISSION_ID);
        given(saved.getType()).willReturn(FeedbackType.COMMENT);
        given(saved.getContent()).willReturn("좋은 코드입니다.");
        given(saved.getStartLine()).willReturn(null);
        given(saved.getEndLine()).willReturn(null);
        given(saved.getColor()).willReturn(null);
        given(saved.getMentorNickname()).willReturn("세현");
        given(saved.getCreatedAt()).willReturn(LocalDateTime.now());
        given(feedbackRepository.save(any(Feedback.class))).willReturn(saved);

        CommentCreateRequest request = new CommentCreateRequest(SUBMISSION_ID, "좋은 코드입니다.");

        // When
        FeedbackResponse result = feedbackService.createComment(MENTOR_ID, request);

        // Then
        assertThat(result.type()).isEqualTo("COMMENT");
        assertThat(result.createdBy()).isEqualTo("세현");
        assertThat(result.startLine()).isNull();
        verify(feedbackRepository).save(any(Feedback.class));
    }

    // ── createHighlight ───────────────────────────────────────

    @Test
    @DisplayName("start_line이 end_line보다 클 때 INVALID_LINE_RANGE 예외가 발생한다")
    void createHighlight_invalidLineRange() {
        // Given
        HighlightCreateRequest request = new HighlightCreateRequest(SUBMISSION_ID, 10, 5, "내용");

        // When & Then
        assertThatThrownBy(() -> feedbackService.createHighlight(MENTOR_ID, request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_LINE_RANGE);

        verify(userRepository, never()).findById(any());
        verify(feedbackRepository, never()).save(any());
    }

    @Test
    @DisplayName("존재하지 않는 mentorId로 하이라이트 작성 시 USER_NOT_FOUND 예외가 발생한다")
    void createHighlight_userNotFound() {
        // Given
        given(userRepository.findById(MENTOR_ID)).willReturn(Optional.empty());

        HighlightCreateRequest request = new HighlightCreateRequest(SUBMISSION_ID, 1, 5, "내용");

        // When & Then
        assertThatThrownBy(() -> feedbackService.createHighlight(MENTOR_ID, request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_NOT_FOUND);

        verify(feedbackRepository, never()).save(any());
    }

    @Test
    @DisplayName("정상 요청 시 HIGHLIGHT 타입 피드백이 #32EBE1 색상으로 저장되고 반환된다")
    void createHighlight_success() {
        // Given
        User mentor = mock(User.class);
        given(mentor.getNickname()).willReturn("세현");
        given(userRepository.findById(MENTOR_ID)).willReturn(Optional.of(mentor));

        Feedback saved = mock(Feedback.class);
        given(saved.getId()).willReturn(FEEDBACK_ID);
        given(saved.getSubmissionId()).willReturn(SUBMISSION_ID);
        given(saved.getType()).willReturn(FeedbackType.HIGHLIGHT);
        given(saved.getContent()).willReturn("이 부분 개선해보세요.");
        given(saved.getStartLine()).willReturn(1);
        given(saved.getEndLine()).willReturn(5);
        given(saved.getColor()).willReturn("#32EBE1");
        given(saved.getMentorNickname()).willReturn("세현");
        given(saved.getCreatedAt()).willReturn(LocalDateTime.now());
        given(feedbackRepository.save(any(Feedback.class))).willReturn(saved);

        HighlightCreateRequest request = new HighlightCreateRequest(SUBMISSION_ID, 1, 5, "이 부분 개선해보세요.");

        // When
        FeedbackResponse result = feedbackService.createHighlight(MENTOR_ID, request);

        // Then
        assertThat(result.type()).isEqualTo("HIGHLIGHT");
        assertThat(result.color()).isEqualTo("#32EBE1");
        assertThat(result.startLine()).isEqualTo(1);
        assertThat(result.endLine()).isEqualTo(5);
        verify(feedbackRepository).save(any(Feedback.class));
    }

    // ── getList ───────────────────────────────────────────────

    @Test
    @DisplayName("submission_id로 피드백 목록 조회 시 등록 순서대로 반환된다")
    void getList_success() {
        // Given
        Feedback comment = mock(Feedback.class);
        given(comment.getId()).willReturn(1L);
        given(comment.getSubmissionId()).willReturn(SUBMISSION_ID);
        given(comment.getType()).willReturn(FeedbackType.COMMENT);
        given(comment.getContent()).willReturn("전체 피드백");
        given(comment.getStartLine()).willReturn(null);
        given(comment.getEndLine()).willReturn(null);
        given(comment.getColor()).willReturn(null);
        given(comment.getMentorNickname()).willReturn("세현");
        given(comment.getCreatedAt()).willReturn(LocalDateTime.now());

        Feedback highlight = mock(Feedback.class);
        given(highlight.getId()).willReturn(2L);
        given(highlight.getSubmissionId()).willReturn(SUBMISSION_ID);
        given(highlight.getType()).willReturn(FeedbackType.HIGHLIGHT);
        given(highlight.getContent()).willReturn("라인 피드백");
        given(highlight.getStartLine()).willReturn(3);
        given(highlight.getEndLine()).willReturn(5);
        given(highlight.getColor()).willReturn("#32EBE1");
        given(highlight.getMentorNickname()).willReturn("세현");
        given(highlight.getCreatedAt()).willReturn(LocalDateTime.now());

        given(feedbackRepository.findBySubmissionIdOrderByCreatedAtAsc(SUBMISSION_ID))
                .willReturn(List.of(comment, highlight));

        // When
        List<FeedbackResponse> result = feedbackService.getList(SUBMISSION_ID);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).type()).isEqualTo("COMMENT");
        assertThat(result.get(1).type()).isEqualTo("HIGHLIGHT");
        verify(feedbackRepository).findBySubmissionIdOrderByCreatedAtAsc(SUBMISSION_ID);
    }

    // ── update ────────────────────────────────────────────────

    @Test
    @DisplayName("존재하지 않는 feedbackId로 수정 시 FEEDBACK_NOT_FOUND 예외가 발생한다")
    void update_feedbackNotFound() {
        // Given
        given(feedbackRepository.findById(FEEDBACK_ID)).willReturn(Optional.empty());

        FeedbackUpdateRequest request = new FeedbackUpdateRequest("수정된 내용");

        // When & Then
        assertThatThrownBy(() -> feedbackService.update(MENTOR_ID, FEEDBACK_ID, request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.FEEDBACK_NOT_FOUND);
    }

    @Test
    @DisplayName("작성자가 아닌 사용자가 수정 시 FORBIDDEN 예외가 발생한다")
    void update_forbidden() {
        // Given
        Long otherMentorId = 999L;
        Feedback feedback = mock(Feedback.class);
        given(feedback.getMentorId()).willReturn(MENTOR_ID);
        given(feedbackRepository.findById(FEEDBACK_ID)).willReturn(Optional.of(feedback));

        FeedbackUpdateRequest request = new FeedbackUpdateRequest("수정된 내용");

        // When & Then
        assertThatThrownBy(() -> feedbackService.update(otherMentorId, FEEDBACK_ID, request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.FORBIDDEN);

        verify(feedback, never()).updateContent(any());
    }

    @Test
    @DisplayName("작성자 본인이 수정 시 content가 변경된다")
    void update_success() {
        // Given
        Feedback feedback = mock(Feedback.class);
        given(feedback.getMentorId()).willReturn(MENTOR_ID);
        given(feedbackRepository.findById(FEEDBACK_ID)).willReturn(Optional.of(feedback));

        FeedbackUpdateRequest request = new FeedbackUpdateRequest("수정된 내용");

        // When
        feedbackService.update(MENTOR_ID, FEEDBACK_ID, request);

        // Then
        verify(feedback).updateContent("수정된 내용");
    }

    // ── delete ────────────────────────────────────────────────

    @Test
    @DisplayName("존재하지 않는 feedbackId로 삭제 시 FEEDBACK_NOT_FOUND 예외가 발생한다")
    void delete_feedbackNotFound() {
        // Given
        given(feedbackRepository.findById(FEEDBACK_ID)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> feedbackService.delete(MENTOR_ID, FEEDBACK_ID))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.FEEDBACK_NOT_FOUND);

        verify(feedbackRepository, never()).delete(any());
    }

    @Test
    @DisplayName("작성자가 아닌 사용자가 삭제 시 FORBIDDEN 예외가 발생한다")
    void delete_forbidden() {
        // Given
        Long otherMentorId = 999L;
        Feedback feedback = mock(Feedback.class);
        given(feedback.getMentorId()).willReturn(MENTOR_ID);
        given(feedbackRepository.findById(FEEDBACK_ID)).willReturn(Optional.of(feedback));

        // When & Then
        assertThatThrownBy(() -> feedbackService.delete(otherMentorId, FEEDBACK_ID))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.FORBIDDEN);

        verify(feedbackRepository, never()).delete(any());
    }

    @Test
    @DisplayName("작성자 본인이 삭제 시 피드백이 삭제된다")
    void delete_success() {
        // Given
        Feedback feedback = mock(Feedback.class);
        given(feedback.getMentorId()).willReturn(MENTOR_ID);
        given(feedbackRepository.findById(FEEDBACK_ID)).willReturn(Optional.of(feedback));

        // When
        feedbackService.delete(MENTOR_ID, FEEDBACK_ID);

        // Then
        verify(feedbackRepository).delete(feedback);
    }
}
