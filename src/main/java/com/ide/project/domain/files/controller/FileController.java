package com.ide.project.domain.files.controller;

import com.ide.project.domain.files.dto.*;
import com.ide.project.domain.files.service.FileService;
import com.ide.project.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@Tag(name = "01. File & Problem Management", description = "학생 워크스페이스 문제 할당 및 에디터 로드 관련 API 명세서")
@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @Operation(summary = "문제 은행에서 워크스페이스로 문제 할당 및 복사")
    @PostMapping("/problems")
    public ResponseEntity<ApiResponse<Long>> assignProblem(@Valid @RequestBody ProblemAssignRequest request) {
        Long assignedProblemId = fileService.assignProblemToSpace(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "CREATED", "문제 할당 성공", assignedProblemId));
    }

    @Operation(summary = "강사 맞춤형 문제 직접 생성 및 할당")
    @PostMapping("/problems/custom")
    public ResponseEntity<ApiResponse<Long>> createAndAssignProblem(@Valid @RequestBody ProblemCreateRequest request) {
        Long customProblemId = fileService.createAndAssignProblem(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "CREATED", "맞춤형 문제 생성 성공", customProblemId));
    }

    @Operation(summary = "워크스페이스 문제 상세 정보 로드")
    @GetMapping("/problems/{problemId}")
    public ResponseEntity<ApiResponse<ProblemResponse>> getProblemDetails(@PathVariable Long problemId) {
        ProblemResponse problemResponse = fileService.getProblemDetails(problemId);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "OK", "문제 상세 정보 로드 성공", problemResponse));
    }

    @Operation(summary = "학생 소스코드 수정 및 임시 저장")
    @PatchMapping("/problems/{problemId}/code")
    public ResponseEntity<ApiResponse<Void>> updateCode(
            @PathVariable Long problemId,
            @RequestBody CodeUpdateRequest request) {
        fileService.updateProblemCode(problemId, request);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "OK", "코드 임시 저장 성공"));
    }

    @Operation(summary = "강사 권한 워크스페이스 문제 수정")
    @PatchMapping("/problems/{problemId}")
    public ResponseEntity<ApiResponse<Void>> updateProblemByTeacher(
            @PathVariable Long problemId,
            @RequestBody ProblemUpdateRequest request) {
        fileService.updateProblem(problemId, request);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "OK", "문제 수정 성공"));
    }

    @Operation(summary = "강사 권한 워크스페이스 문제 삭제")
    @DeleteMapping("/problems/{problemId}")
    public ResponseEntity<ApiResponse<Void>> deleteProblemByTeacher(@PathVariable Long problemId) {
        fileService.deleteProblem(problemId);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "OK", "문제 삭제 성공"));
    }

    @Operation(summary = "학생 권한 최종 제출 코드 수정 및 재제출")
    @PatchMapping("/problems/{problemId}/resubmit")
    public ResponseEntity<ApiResponse<Void>> updateSubmissionByStudent(
            @PathVariable Long problemId,
            @RequestBody SubmissionUpdateRequest request) {
        fileService.updateSubmissionCode(problemId, request);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "OK", "코드 재제출 성공"));
    }

    @Operation(summary = "학생 권한 최종 제출 취소 및 코드 초기화")
    @DeleteMapping("/problems/{problemId}/reset")
    public ResponseEntity<ApiResponse<Void>> deleteSubmissionByStudent(@PathVariable Long problemId) {
        fileService.resetSubmissionCode(problemId);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "OK", "코드 초기화 성공"));
    }

    @Operation(summary = "채점용 테스트케이스 추가")
    @PostMapping("/problems/{problemId}/testcases")
    public ResponseEntity<ApiResponse<Long>> addTestCaseByTeacher(
            @PathVariable Long problemId,
            @RequestBody TestCaseCreateRequest request) {
        Long testCaseId = fileService.addTestCase(problemId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "CREATED", "테스트케이스 추가 성공", testCaseId));
    }

    @Operation(summary = "특정 테스트케이스 개별 삭제")
    @DeleteMapping("/testcases/{testCaseId}")
    public ResponseEntity<ApiResponse<Void>> deleteTestCaseByTeacher(@PathVariable Long testCaseId) {
        fileService.deleteTestCase(testCaseId);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "OK", "테스트케이스 삭제 성공"));
    }
}