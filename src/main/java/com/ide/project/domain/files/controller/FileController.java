package com.ide.project.domain.files.controller;

import com.ide.project.domain.files.dto.*;
import com.ide.project.domain.files.service.FileService;
import com.ide.project.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
@Tag(name = "파일 관리", description = "문제, 테스트케이스, 제출 관리 API")
public class FileController {

    private final FileService fileService;

    // ==========================================
    // 문제 관리 API
    // ==========================================

    @Operation(summary = "문제 생성", description = "강사가 워크스페이스에 새로운 문제를 직접 생성합니다.")
    @PostMapping("/problems")
    public ResponseEntity<ApiResponse<ProblemResponse>> createProblem(
            @Valid @RequestBody ProblemCreateRequest request) {
        ProblemResponse response = fileService.createProblem(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(201, "SUCCESS", "문제가 성공적으로 생성되었습니다.", response));
    }

    @Operation(summary = "문제 수정", description = "강사가 기존 문제의 내용을 수정합니다.")
    @PutMapping("/problems/{problemId}")
    public ResponseEntity<ApiResponse<ProblemResponse>> updateProblem(
            @PathVariable Long problemId,
            @Valid @RequestBody ProblemUpdateRequest request) {
        ProblemResponse response = fileService.updateProblem(problemId, request);
        return ResponseEntity.ok(ApiResponse.success(200, "SUCCESS", "문제가 성공적으로 수정되었습니다.", response));
    }

    @Operation(summary = "문제 삭제", description = "문제를 삭제합니다. 연관된 테스트케이스도 함께 삭제됩니다.")
    @DeleteMapping("/problems/{problemId}")
    public ResponseEntity<ApiResponse<Void>> deleteProblem(
            @PathVariable Long problemId) {
        fileService.deleteProblem(problemId);
        return ResponseEntity.ok(ApiResponse.success(200, "SUCCESS", "문제가 성공적으로 삭제되었습니다."));
    }

    @Operation(summary = "문제 은행에서 문제 배정", description = "문제 은행의 문제를 워크스페이스에 배정합니다. 테스트케이스도 함께 복사됩니다.")
    @PostMapping("/problems/assign")
    public ResponseEntity<ApiResponse<ProblemResponse>> assignProblemFromBank(
            @Valid @RequestBody ProblemAssignRequest request) {
        ProblemResponse response = fileService.assignProblemFromBank(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(201, "SUCCESS", "문제 은행에서 문제가 성공적으로 배정되었습니다.", response));
    }

    @Operation(summary = "문제 상세 조회", description = "특정 문제의 상세 정보를 조회합니다.")
    @GetMapping("/problems/{problemId}")
    public ResponseEntity<ApiResponse<ProblemResponse>> getProblemDetails(
            @PathVariable Long problemId) {
        ProblemResponse response = fileService.getProblemDetails(problemId);
        return ResponseEntity.ok(ApiResponse.success(200, "SUCCESS", "문제 상세 정보 조회에 성공하였습니다.", response));
    }

    @Operation(summary = "워크스페이스 문제 목록 조회", description = "특정 워크스페이스에 속한 모든 문제를 조회합니다.")
    @GetMapping("/problems/space/{spaceId}")
    public ResponseEntity<ApiResponse<List<ProblemResponse>>> getProblemsBySpace(
            @PathVariable Long spaceId) {
        List<ProblemResponse> response = fileService.getProblemsBySpace(spaceId);
        return ResponseEntity.ok(ApiResponse.success(200, "SUCCESS", "워크스페이스 문제 목록 조회에 성공하였습니다.", response));
    }

    // ==========================================
    // 테스트케이스 관리 API
    // ==========================================

    @Operation(summary = "테스트케이스 저장", description = "문제에 테스트케이스를 저장합니다. 기존 테스트케이스는 삭제 후 새로 저장됩니다.")
    @PostMapping("/problems/{problemId}/testcases")
    public ResponseEntity<ApiResponse<Void>> saveTestCases(
            @PathVariable Long problemId,
            @Valid @RequestBody List<TestCaseCreateRequest> testCaseRequests) {
        fileService.saveTestCases(problemId, testCaseRequests);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(201, "SUCCESS", "테스트케이스가 성공적으로 저장되었습니다."));
    }

    @Operation(summary = "테스트케이스 조회", description = "특정 문제의 테스트케이스를 조회합니다.")
    @GetMapping("/problems/{problemId}/testcases")
    public ResponseEntity<ApiResponse<List<TestCaseResponse>>> getTestCases(
            @PathVariable Long problemId) {
        List<TestCaseResponse> response = fileService.getTestCases(problemId);
        return ResponseEntity.ok(ApiResponse.success(200, "SUCCESS", "테스트케이스 조회에 성공하였습니다.", response));
    }

    // ==========================================
    // 제출 관리 API
    // ==========================================

    @Operation(summary = "코드 제출 / 임시 저장", description = "is_final_submit이 true면 최종 제출, false면 임시 저장입니다.")
    @PostMapping("/submissions")
    public ResponseEntity<ApiResponse<Void>> submitCode(
            @Valid @RequestBody SubmissionRequest request) {
        fileService.submitCode(request);
        String message = request.isFinalSubmit()
                ? "코드가 성공적으로 제출되었습니다."
                : "코드가 성공적으로 임시 저장되었습니다.";
        return ResponseEntity.ok(ApiResponse.success(200, "SUCCESS", message));
    }

    @Operation(summary = "제출 정보 조회", description = "특정 문제에 대한 특정 유저의 제출 정보를 조회합니다.")
    @GetMapping("/submissions/{problemId}/{userId}")
    public ResponseEntity<ApiResponse<SubmissionResponse>> getSubmission(
            @PathVariable Long problemId,
            @PathVariable Long userId) {
        SubmissionResponse response = fileService.getSubmission(problemId, userId);
        return ResponseEntity.ok(ApiResponse.success(200, "SUCCESS", "제출 정보 조회에 성공하였습니다.", response));
    }

    @Operation(summary = "제출 취소", description = "PENDING 상태인 제출을 DRAFT로 되돌립니다.")
    @DeleteMapping("/submissions/{problemId}/{userId}")
    public ResponseEntity<ApiResponse<Void>> cancelSubmission(
            @PathVariable Long problemId,
            @PathVariable Long userId) {
        fileService.cancelSubmission(problemId, userId);
        return ResponseEntity.ok(ApiResponse.success(200, "SUCCESS", "제출이 성공적으로 취소되었습니다."));
    }

    @Operation(summary = "임시 저장 코드 수정", description = "학생이 에디터에서 코드를 수정하여 임시 저장합니다.")
    @PutMapping("/submissions/{problemId}/{userId}")
    public ResponseEntity<ApiResponse<Void>> updateSavedCode(
            @PathVariable Long problemId,
            @PathVariable Long userId,
            @Valid @RequestBody CodeUpdateRequest request) {
        fileService.updateSavedCode(problemId, userId, request);
        return ResponseEntity.ok(ApiResponse.success(200, "SUCCESS", "코드가 성공적으로 수정되었습니다."));
    }
}