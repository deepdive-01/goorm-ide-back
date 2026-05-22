package com.ide.project.domain.files.service;

import com.ide.project.domain.files.dto.*;
import java.util.List;

public interface FileService {

    ProblemResponse createProblem(ProblemCreateRequest request);
    ProblemResponse updateProblem(Long problemId, ProblemUpdateRequest request);
    ProblemResponse assignProblemFromBank(ProblemAssignRequest request);
    ProblemResponse getProblemDetails(Long problemId);


    void saveTestCases(Long problemId, List<TestCaseCreateRequest> testCaseRequests);


    void submitCode(SubmissionRequest request);
}