package com.ide.project.domain.files.service;

import com.ide.project.domain.files.dto.CodeUpdateRequest;
import com.ide.project.domain.files.dto.ProblemCreateRequest;
import com.ide.project.domain.files.dto.ProblemResponse;

public interface FileService {
    ProblemResponse createProblem(ProblemCreateRequest request);
    void updateCodeSubmission(CodeUpdateRequest request);
    ProblemResponse getProblemDetails(Long problemId);
}