package com.ide.project.domain.submission.dto.response;

import java.util.List;

public record SubmissionListResponse(
        Long questionId,
        int totalCount,
        List<SubmissionItem> submissions
) {
    public record SubmissionItem(
            Long submissionId,
            Long studentId,
            String nickname,
            String status,
            boolean hasFeedback
    ) {}
}
