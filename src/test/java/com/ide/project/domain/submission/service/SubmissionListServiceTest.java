package com.ide.project.domain.submission.service;

import com.ide.project.domain.files.entity.Submission;
import com.ide.project.domain.files.repository.ProblemRepository;
import com.ide.project.domain.files.repository.SubmissionRepository;
import com.ide.project.domain.submission.dto.response.SubmissionListResponse;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubmissionListServiceTest {

    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private ProblemRepository problemRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SubmissionListService submissionListService;

    private static final Long TEST_QUESTION_ID = 1L;
    private static final Long TEST_STUDENT_ID = 42L;

    // ── getSubmissions ────────────────────────────────────────

    @Test
    @DisplayName("존재하지 않는 questionId로 조회 시 QUESTION_NOT_FOUND 예외가 발생한다")
    void getSubmissions_questionNotFound() {
        // Given
        given(problemRepository.existsById(TEST_QUESTION_ID)).willReturn(false);

        // When & Then
        assertThatThrownBy(() -> submissionListService.getSubmissions(TEST_QUESTION_ID, null))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.QUESTION_NOT_FOUND);

        verify(submissionRepository, never()).findByProblemId(any());
        verify(submissionRepository, never()).findByProblemIdAndStatus(any(), any());
    }

    @Test
    @DisplayName("status 필터 없이 조회 시 해당 문제의 전체 제출 목록을 반환한다")
    void getSubmissions_noFilter_success() {
        // Given
        given(problemRepository.existsById(TEST_QUESTION_ID)).willReturn(true);

        Submission submission = mock(Submission.class);
        given(submission.getId()).willReturn(1L);
        given(submission.getStudentId()).willReturn(TEST_STUDENT_ID);
        given(submission.getStatus()).willReturn("SUCCESS");

        given(submissionRepository.findByProblemId(TEST_QUESTION_ID)).willReturn(List.of(submission));

        User user = mock(User.class);
        given(user.getNickname()).willReturn("겨운");
        given(userRepository.findById(TEST_STUDENT_ID)).willReturn(Optional.of(user));

        given(submissionRepository.countFeedbacksBySubmissionId(1L)).willReturn(0L);

        // When
        SubmissionListResponse result = submissionListService.getSubmissions(TEST_QUESTION_ID, null);

        // Then
        assertThat(result.questionId()).isEqualTo(TEST_QUESTION_ID);
        assertThat(result.totalCount()).isEqualTo(1);
        assertThat(result.submissions()).hasSize(1);
        assertThat(result.submissions().get(0).nickname()).isEqualTo("겨운");
        assertThat(result.submissions().get(0).status()).isEqualTo("SUCCESS");
        assertThat(result.submissions().get(0).hasFeedback()).isFalse();

        verify(submissionRepository).findByProblemId(TEST_QUESTION_ID);
        verify(submissionRepository, never()).findByProblemIdAndStatus(any(), any());
    }

    @Test
    @DisplayName("status 필터를 지정하면 해당 상태의 제출 목록만 반환한다")
    void getSubmissions_withStatusFilter_success() {
        // Given
        given(problemRepository.existsById(TEST_QUESTION_ID)).willReturn(true);

        Submission submission = mock(Submission.class);
        given(submission.getId()).willReturn(2L);
        given(submission.getStudentId()).willReturn(TEST_STUDENT_ID);
        given(submission.getStatus()).willReturn("FAIL");

        given(submissionRepository.findByProblemIdAndStatus(TEST_QUESTION_ID, "FAIL"))
                .willReturn(List.of(submission));

        User user = mock(User.class);
        given(user.getNickname()).willReturn("성민");
        given(userRepository.findById(TEST_STUDENT_ID)).willReturn(Optional.of(user));

        given(submissionRepository.countFeedbacksBySubmissionId(2L)).willReturn(0L);

        // When
        SubmissionListResponse result = submissionListService.getSubmissions(TEST_QUESTION_ID, "FAIL");

        // Then
        assertThat(result.totalCount()).isEqualTo(1);
        assertThat(result.submissions().get(0).status()).isEqualTo("FAIL");

        verify(submissionRepository).findByProblemIdAndStatus(TEST_QUESTION_ID, "FAIL");
        verify(submissionRepository, never()).findByProblemId(any());
    }

    @Test
    @DisplayName("제출한 학생이 없으면 빈 리스트를 반환한다")
    void getSubmissions_emptyList() {
        // Given
        given(problemRepository.existsById(TEST_QUESTION_ID)).willReturn(true);
        given(submissionRepository.findByProblemId(TEST_QUESTION_ID)).willReturn(List.of());

        // When
        SubmissionListResponse result = submissionListService.getSubmissions(TEST_QUESTION_ID, null);

        // Then
        assertThat(result.totalCount()).isEqualTo(0);
        assertThat(result.submissions()).isEmpty();
    }

    @Test
    @DisplayName("피드백이 존재하는 제출의 경우 has_feedback이 true로 반환된다")
    void getSubmissions_hasFeedback_true() {
        // Given
        given(problemRepository.existsById(TEST_QUESTION_ID)).willReturn(true);

        Submission submission = mock(Submission.class);
        given(submission.getId()).willReturn(3L);
        given(submission.getStudentId()).willReturn(TEST_STUDENT_ID);
        given(submission.getStatus()).willReturn("SUCCESS");

        given(submissionRepository.findByProblemId(TEST_QUESTION_ID)).willReturn(List.of(submission));

        User user = mock(User.class);
        given(user.getNickname()).willReturn("겨운");
        given(userRepository.findById(TEST_STUDENT_ID)).willReturn(Optional.of(user));

        given(submissionRepository.countFeedbacksBySubmissionId(3L)).willReturn(2L);

        // When
        SubmissionListResponse result = submissionListService.getSubmissions(TEST_QUESTION_ID, null);

        // Then
        assertThat(result.submissions().get(0).hasFeedback()).isTrue();
    }
}
