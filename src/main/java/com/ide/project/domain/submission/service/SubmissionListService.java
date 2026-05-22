package com.ide.project.domain.submission.service;

import com.ide.project.domain.files.entity.Submission;
import com.ide.project.domain.files.repository.ProblemRepository;
import com.ide.project.domain.files.repository.SubmissionRepository;
import com.ide.project.domain.submission.dto.response.SubmissionListResponse;
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
public class SubmissionListService {

    private final SubmissionRepository submissionRepository;
    private final ProblemRepository problemRepository;
    private final UserRepository userRepository;

    public SubmissionListResponse getSubmissions(Long questionId, String status) {
        if (!problemRepository.existsById(questionId)) {
            throw new BusinessException(ErrorCode.QUESTION_NOT_FOUND);
        }

        List<Submission> submissions = (status != null)
                ? submissionRepository.findByProblemIdAndStatus(questionId, status)
                : submissionRepository.findByProblemId(questionId);

        List<SubmissionListResponse.SubmissionItem> items = submissions.stream()
                .map(s -> {
                    User user = userRepository.findById(s.getUserId())
                            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
                    boolean hasFeedback = submissionRepository.countFeedbacksBySubmissionId(s.getId()) > 0;
                    return new SubmissionListResponse.SubmissionItem(
                            s.getId(),
                            s.getUserId(),
                            user.getNickname(),
                            s.getStatus(),
                            hasFeedback
                    );
                })
                .toList();

        return new SubmissionListResponse(questionId, items.size(), items);
    }
}
