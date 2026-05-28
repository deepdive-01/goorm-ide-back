package com.ide.project.domain.files.controller;

import com.ide.project.domain.files.dto.*;
import com.ide.project.domain.files.service.FileService;
import com.ide.project.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    // ==========================================
    // 문제 관리 API
    // ==========================================

    @PostMapping("/problems")
    public ResponseEntity<ApiResponse<ProblemResponse>> createProblem(
            @Valid @RequestBody ProblemCreateRequest request) {
        ProblemResponse response = fileService.createProblem(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(201, "SUCCESS", "문제가 성공적으로 생성되었습니다.", response));
    }

    @PutMapping("/problems/{problemId}")
    public ResponseEntity<ApiResponse<ProblemResponse>> updateProblem(
            @PathVariable Long problemId,
            @Valid @RequestBody ProblemUpdateRequest request) {
        ProblemResponse response = fileService.updateProblem(problemId, request);
        return ResponseEntity.ok(ApiResponse.success(200, "SUCCESS", "문제가 성공적으로 수정되었습니다.", response));
    }

    @DeleteMapping("/problems/{problemId}")
    public ResponseEntity<ApiResponse<Void>> deleteProblem(
            @PathVariable Long problemId) {
        fileService.deleteProblem(problemId);
        return ResponseEntity.ok(ApiResponse.success(200, "SUCCESS", "문제가 성공적으로 삭제되었습니다."));
    }

    @PostMapping("/problems/assign")
    public ResponseEntity<ApiResponse<ProblemResponse>> assignProblemFromBank(
            @Valid @RequestBody ProblemAssignRequest request) {
        ProblemResponse response = fileService.assignProblemFromBank(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(201, "SUCCESS", "문제 은행에서 문제가 성공적으로 배정되었습니다.", response));
    }

    @GetMapping("/problems/{problemId}")
    public ResponseEntity<ApiResponse<ProblemResponse>> getProblemDetails(
            @PathVariable Long problemId) {
        ProblemResponse response = fileService.getProblemDetails(problemId);
        return ResponseEntity.ok(ApiResponse.success(200, "SUCCESS", "문제 상세 정보 조회에 성공하였습니다.", response));
    }

    @GetMapping("/problems/space/{spaceId}")
    public ResponseEntity<ApiResponse<List<ProblemResponse>>> getProblemsBySpace(
            @PathVariable Long spaceId) {
        List<ProblemResponse> response = fileService.getProblemsBySpace(spaceId);
        return ResponseEntity.ok(ApiResponse.success(200, "SUCCESS", "워크스페이스 문제 목록 조회에 성공하였습니다.", response));
    }

    // ==========================================
    // 테스트케이스 관리 API
    // ==========================================

    @PostMapping("/problems/{problemId}/testcases")
    public ResponseEntity<ApiResponse<Void>> saveTestCases(
            @PathVariable Long problemId,
            @Valid @RequestBody List<TestCaseCreateRequest> testCaseRequests) {
        fileService.saveTestCases(problemId, testCaseRequests);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(201, "SUCCESS", "테스트케이스가 성공적으로 저장되었습니다."));
    }
    @GetMapping("/problems/{problemId}/testcases")
    public ResponseEntity<ApiResponse<List<TestCaseResponse>>> getTestCases(
            @PathVariable Long problemId) {
        List<TestCaseResponse> response = fileService.getTestCases(problemId);
        return ResponseEntity.ok(ApiResponse.success(200, "SUCCESS", "테스트케이스 조회에 성공하였습니다.", response));
}

    // ==========================================
    // 제출 관리 API
    // ==========================================

    @PostMapping("/submissions")
    public ResponseEntity<ApiResponse<Void>> submitCode(
            @Valid @RequestBody SubmissionRequest request) {
        fileService.submitCode(request);
        String message = request.isFinalSubmit()
                ? "코드가 성공적으로 제출되었습니다."
                : "코드가 성공적으로 임시 저장되었습니다.";
        return ResponseEntity.ok(ApiResponse.success(200, "SUCCESS", message));
    }

    @GetMapping("/submissions/{problemId}/{userId}")
    public ResponseEntity<ApiResponse<SubmissionResponse>> getSubmission(
            @PathVariable Long problemId,
            @PathVariable Long userId) {
        SubmissionResponse response = fileService.getSubmission(problemId, userId);
        return ResponseEntity.ok(ApiResponse.success(200, "SUCCESS", "제출 정보 조회에 성공하였습니다.", response));
    }

    @DeleteMapping("/submissions/{problemId}/{userId}")
    public ResponseEntity<ApiResponse<Void>> cancelSubmission(
            @PathVariable Long problemId,
            @PathVariable Long userId) {
        fileService.cancelSubmission(problemId, userId);
        return ResponseEntity.ok(ApiResponse.success(200, "SUCCESS", "제출이 성공적으로 취소되었습니다."));
    }

    @PutMapping("/submissions/{problemId}/{userId}")
    public ResponseEntity<ApiResponse<Void>> updateSavedCode(
            @PathVariable Long problemId,
            @PathVariable Long userId,
            @Valid @RequestBody CodeUpdateRequest request) {
        fileService.updateSavedCode(problemId, userId, request);
        return ResponseEntity.ok(ApiResponse.success(200, "SUCCESS", "코드가 성공적으로 수정되었습니다."));
    }
}