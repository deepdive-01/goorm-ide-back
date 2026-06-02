package com.ide.project.domain.files.service;

import com.ide.project.domain.files.dto.*;
import java.util.List;

public interface FileService {

    // ==========================================
    // 문제 관리
    // ==========================================
    ProblemResponse createProblem(ProblemCreateRequest request);
    ProblemResponse updateProblem(Long problemId, ProblemUpdateRequest request);
    void deleteProblem(Long problemId);
    ProblemResponse assignProblemFromBank(ProblemAssignRequest request);
    ProblemResponse getProblemDetails(Long problemId);
    List<ProblemResponse> getProblemsBySpace(Long spaceId);

    // ==========================================
    // 테스트케이스 관리
    // ==========================================
    void saveTestCases(Long problemId, List<TestCaseCreateRequest> testCaseRequests);
    List<TestCaseResponse> getTestCases(Long problemId);

    // ==========================================
    // 제출 관리
    // ==========================================
    void submitCode(SubmissionRequest request);
    SubmissionResponse getSubmission(Long problemId, Long userId);
    void cancelSubmission(Long problemId, Long userId);
    void updateSavedCode(Long problemId, Long userId, CodeUpdateRequest request);
    List<StudentSubmissionResponse> getStudentSubmissions(Long userId);
}