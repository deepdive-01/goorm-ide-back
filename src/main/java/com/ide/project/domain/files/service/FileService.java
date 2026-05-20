package com.ide.project.domain.files.service;

import com.ide.project.domain.files.dto.*;

public interface FileService {
    
    Long assignProblemToSpace(ProblemAssignRequest request);
    
    Long createAndAssignProblem(ProblemCreateRequest request);
    
    ProblemResponse getProblemDetails(Long problemId);
    
    void updateProblemCode(Long problemId, CodeUpdateRequest request);
    
    void updateProblem(Long problemId, ProblemUpdateRequest request);
    
    void deleteProblem(Long problemId);
    
    // 🌟 수정: 기존 updateSubmissionCode를 제거하고 통합 제출 메서드 추가
    void submitCode(Long problemId, SubmissionUpdateRequest request);
    
    void resetSubmissionCode(Long problemId);
    
    Long addTestCase(Long problemId, TestCaseCreateRequest request);
    
    void deleteTestCase(Long testCaseId);
}