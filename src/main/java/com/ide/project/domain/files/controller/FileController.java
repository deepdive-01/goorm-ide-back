package com.ide.project.domain.files.controller;

import com.ide.project.domain.files.dto.ProblemAssignRequest;
import com.ide.project.domain.files.dto.ProblemCreateRequest;
import com.ide.project.domain.files.dto.ProblemResponse;
import com.ide.project.domain.files.dto.CodeUpdateRequest;
import com.ide.project.domain.files.dto.ProblemUpdateRequest;
import com.ide.project.domain.files.dto.SubmissionUpdateRequest;
import com.ide.project.domain.files.dto.TestCaseCreateRequest;
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

    @Operation(summary = "문제 은행에서 워크스페이스로 문제 할당 및 복사", description = "강사가 문제 은행의 원본 ID를 전달하여 특정 스페이스에 문제를 할당하고 관련 테스트케이스 스냅샷을 복사합니다.")
    @PostMapping("/problems")
    public ResponseEntity<ApiResponse<Long>> assignProblem(@Valid @RequestBody ProblemAssignRequest request) {
        Long assignedProblemId = fileService.assignProblemToSpace(request);
        
        ApiResponse<Long> response = ApiResponse.success(
                HttpStatus.CREATED.value(), 
                "CREATED", 
                "문제 은행에서 워크스페이스로 문제가 성공적으로 할당되었습니다.", 
                assignedProblemId
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "강사 맞춤형 문제 직접 생성 및 할당", description = "문제 은행을 거치지 않고, 강사가 직접 타이핑한 본문과 초기 소스코드를 기반으로 워크스페이스에 문제를 즉시 생성합니다.")
    @PostMapping("/spaces/{spaceId}/problems/custom")
    public ResponseEntity<ApiResponse<Long>> createAndAssignProblem(
            @PathVariable Long spaceId,
            @Valid @RequestBody ProblemCreateRequest request) {
        Long customProblemId = fileService.createAndAssignProblem(spaceId, request);
        
        ApiResponse<Long> response = ApiResponse.success(
                HttpStatus.CREATED.value(), 
                "CREATED", 
                "강사 맞춤형 문제가 워크스페이스에 성공적으로 생성되었습니다.", 
                customProblemId
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "워크스페이스 문제 상세 정보 및 초기 코드 로드", description = "학생이 시험 및 에디터 화면에 진입했을 때, 해당 문제의 지문, 제목, 언어 스펙, 에디터용 최초 스타터 코드를 일괄 조회합니다.")
    @GetMapping("/problems/{problemId}")
    public ResponseEntity<ApiResponse<ProblemResponse>> getProblemDetails(@PathVariable Long problemId) {
        ProblemResponse problemResponse = fileService.getProblemDetails(problemId);
        
        ApiResponse<ProblemResponse> response = ApiResponse.success(
                HttpStatus.OK.value(), 
                "OK", 
                "워크스페이스 문제 상세 정보가 성공적으로 로드되었습니다.", 
                problemResponse
        );
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "학생 소스코드 수정 및 임시 저장", description = "학생이 에디터에서 작성한 최신 코드를 워크스페이스의 해당 문제 starterCode 필드에 덮어써서 저장합니다.")
    @PatchMapping("/problems/{problemId}/code")
    public ResponseEntity<ApiResponse<Void>> updateCode(
            @PathVariable Long problemId,
            @RequestBody CodeUpdateRequest request) {
        
        fileService.updateProblemCode(problemId, request);
        
        ApiResponse<Void> response = ApiResponse.success(
                HttpStatus.OK.value(), 
                "OK", 
                "소스코드가 워크스페이스에 안전하게 중간 저장되었습니다."
        );
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "강사 권한 워크스페이스 문제 수정", description = "강사가 특정 워크스페이스에 할당된 문제의 제목, 지문, 초기 코드 등 전체 스펙을 수정합니다.")
    @PatchMapping("/spaces/{spaceId}/problems/{problemId}")
    public ResponseEntity<ApiResponse<Void>> updateProblemByTeacher(
            @PathVariable Long spaceId,
            @PathVariable Long problemId,
            @RequestBody ProblemUpdateRequest request) {
        
        fileService.updateProblem(problemId, request);
        
        ApiResponse<Void> response = ApiResponse.success(
                HttpStatus.OK.value(), 
                "OK", 
                "워크스페이스 문제가 성공적으로 수정되었습니다."
        );
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "강사 권한 워크스페이스 문제 삭제", description = "강사가 특정 워크스페이스에 할당된 문제를 안전하게 삭제 처리합니다.")
    @DeleteMapping("/spaces/{spaceId}/problems/{problemId}")
    public ResponseEntity<ApiResponse<Void>> deleteProblemByTeacher(
            @PathVariable Long spaceId,
            @PathVariable Long problemId) {
        
        fileService.deleteProblem(problemId);
        
        ApiResponse<Void> response = ApiResponse.success(
                HttpStatus.OK.value(), 
                "OK", 
                "워크스페이스 문제가 성공적으로 삭제되었습니다."
        );
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "학생 권한 최종 제출 코드 수정 및 재제출", description = "학생이 이미 최종 제출 완료한 풀이 코드를 보완하여 다시 덮어쓰기 형태로 재제출합니다.")
    @PatchMapping("/problems/{problemId}/resubmit")
    public ResponseEntity<ApiResponse<Void>> updateSubmissionByStudent(
            @PathVariable Long problemId,
            @RequestBody SubmissionUpdateRequest request) {
        
        fileService.updateSubmissionCode(problemId, request);
        
        ApiResponse<Void> response = ApiResponse.success(
                HttpStatus.OK.value(), 
                "OK", 
                "제출하신 소스코드가 성공적으로 수정 및 재제출되었습니다."
        );
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "학생 권한 최종 제출 취소 및 코드 초기화", description = "학생이 제출했던 풀이 기록을 취소하고, 해당 문제의 코딩창을 최초 원본 스타터 코드로 깨끗하게 초기화합니다.")
    @DeleteMapping("/problems/{problemId}/reset")
    public ResponseEntity<ApiResponse<Void>> deleteSubmissionByStudent(@PathVariable Long problemId) {
        
        fileService.resetSubmissionCode(problemId);
        
        ApiResponse<Void> response = ApiResponse.success(
                HttpStatus.OK.value(), 
                "OK", 
                "풀이 제출이 취소되었으며, 소스코드가 최초 상태로 초기화되었습니다."
        );
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "특정 문제에 채점용 테스트케이스 추가", description = "강사가 직접 생성한 커스텀 문제 등에 알고리즘 정답 판별을 위한 테스트케이스(입력값/기댓값)를 추가합니다.")
    @PostMapping("/problems/{problemId}/testcases")
    public ResponseEntity<ApiResponse<Long>> addTestCaseByTeacher(
            @PathVariable Long problemId,
            @RequestBody TestCaseCreateRequest request) {
        
        Long testCaseId = fileService.addTestCase(problemId, request);
        
        ApiResponse<Long> response = ApiResponse.success(
                HttpStatus.CREATED.value(), 
                "CREATED", 
                "테스트케이스가 데이터베이스에 성공적으로 추가되었습니다.", 
                testCaseId
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "특정 테스트케이스 개별 삭제", description = "잘못 작성되었거나 불필요해진 채점용 테스트케이스를 데이터베이스에서 영구 삭제합니다.")
    @DeleteMapping("/testcases/{testCaseId}")
    public ResponseEntity<ApiResponse<Void>> deleteTestCaseByTeacher(@PathVariable Long testCaseId) {
        
        fileService.deleteTestCase(testCaseId);
        
        ApiResponse<Void> response = ApiResponse.success(
                HttpStatus.OK.value(), 
                "OK", 
                "테스트케이스가 데이터베이스에서 성공적으로 삭제되었습니다."
        );
        return ResponseEntity.ok(response);
    }
}