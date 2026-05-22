package com.ide.project.domain.feedback.service;

import com.ide.project.domain.feedback.dto.request.CommentCreateRequest;
import com.ide.project.domain.feedback.dto.response.FeedbackResponse;
import com.ide.project.domain.feedback.entity.Feedback;
import com.ide.project.domain.feedback.entity.FeedbackType;
import com.ide.project.domain.feedback.repository.FeedbackRepository;
import com.ide.project.domain.files.repository.SubmissionRepository;
import com.ide.project.domain.user.entity.User;
import com.ide.project.domain.user.repository.UserRepository;
import com.ide.project.global.exception.ErrorCode;
import com.ide.project.global.exception.custom.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final SubmissionRepository submissionRepository;
    private final UserRepository userRepository;

    @Transactional
    public FeedbackResponse createComment(CommentCreateRequest request, Long mentorId) {
        User mentor = userRepository.findById(mentorId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!submissionRepository.existsById(request.submission_id())) {
            throw new BusinessException(ErrorCode.SUBMISSION_NOT_FOUND);
        }

        Feedback feedback = Feedback.builder()
                .submissionId(request.submission_id())
                .mentor(mentor)
                .type(FeedbackType.COMMENT)
                .content(request.content())
                .build();

        return FeedbackResponse.from(feedbackRepository.save(feedback));
    }
}
