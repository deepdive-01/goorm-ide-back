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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedbackService {

    private static final String HIGHLIGHT_COLOR = "#32EBE1";

    private final FeedbackRepository feedbackRepository;
    private final UserRepository userRepository;

    @Transactional
    public FeedbackResponse createComment(Long mentorId, CommentCreateRequest request) {
        User mentor = findUser(mentorId);

        Feedback feedback = Feedback.builder()
                .submissionId(request.submissionId())
                .mentorId(mentorId)
                .mentorNickname(mentor.getNickname())
                .type(FeedbackType.COMMENT)
                .content(request.content())
                .build();

        return FeedbackResponse.from(feedbackRepository.save(feedback));
    }

    @Transactional
    public FeedbackResponse createHighlight(Long mentorId, HighlightCreateRequest request) {
        if (request.startLine() > request.endLine()) {
            throw new BusinessException(ErrorCode.INVALID_LINE_RANGE);
        }

        User mentor = findUser(mentorId);

        Feedback feedback = Feedback.builder()
                .submissionId(request.submissionId())
                .mentorId(mentorId)
                .mentorNickname(mentor.getNickname())
                .type(FeedbackType.HIGHLIGHT)
                .content(request.content())
                .startLine(request.startLine())
                .endLine(request.endLine())
                .startChar(request.startChar())
                .endChar(request.endChar())
                .color(HIGHLIGHT_COLOR)
                .build();

        return FeedbackResponse.from(feedbackRepository.save(feedback));
    }

    public List<FeedbackResponse> getList(Long submissionId) {
        return feedbackRepository.findBySubmissionIdOrderByCreatedAtAsc(submissionId)
                .stream()
                .map(FeedbackResponse::from)
                .toList();
    }

    @Transactional
    public void update(Long mentorId, Long feedbackId, FeedbackUpdateRequest request) {
        Feedback feedback = findFeedback(feedbackId);

        if (!feedback.getMentorId().equals(mentorId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        feedback.updateContent(request.content());
    }

    @Transactional
    public void delete(Long mentorId, Long feedbackId) {
        Feedback feedback = findFeedback(feedbackId);

        if (!feedback.getMentorId().equals(mentorId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        feedbackRepository.delete(feedback);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private Feedback findFeedback(Long feedbackId) {
        return feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FEEDBACK_NOT_FOUND));
    }
}
