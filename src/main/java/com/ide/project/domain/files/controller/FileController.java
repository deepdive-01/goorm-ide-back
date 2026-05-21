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

    
     // 강사가 새로운 문제를 생성합니다.
     
    @PostMapping("/problems")
    public ResponseEntity<ApiResponse<ProblemResponse>> createProblem(@Valid @RequestBody ProblemCreateRequest request) {
        ProblemResponse response = fileService.createProblem(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(201, "SUCCESS", "문제가 성공적으로 생성되었습니다.", response));
    }

    
     // 기존 문제의 내용(지문, 난이도, 언어 등)을 수정합니다.
    
    @PutMapping("/problems/{problemId}")
    public ResponseEntity<ApiResponse<ProblemResponse>> updateProblem(
            @PathVariable Long problemId,
            @Valid @RequestBody ProblemUpdateRequest request) {
        ProblemResponse response = fileService.updateProblem(problemId, request);
        return ResponseEntity.ok(ApiResponse.success(200, "SUCCESS", "문제가 성공적으로 수정되었습니다.", response));
    }

   
     // 문제 은행에 있는 원본 문제를 강사의 워크스페이스로 복사하여 배정합니다.
    
    @PostMapping("/problems/assign")
    public ResponseEntity<ApiResponse<ProblemResponse>> assignProblemFromBank(@Valid @RequestBody ProblemAssignRequest request) {
        ProblemResponse response = fileService.assignProblemFromBank(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(201, "SUCCESS", "문제 은행에서 문제가 성공적으로 배정되었습니다.", response));
    }

    
     // 특정 문제의 상세 정보를 조회합니다.
    
    @GetMapping("/problems/{problemId}")
    public ResponseEntity<ApiResponse<ProblemResponse>> getProblemDetails(@PathVariable Long problemId) {
        ProblemResponse response = fileService.getProblemDetails(problemId);
        return ResponseEntity.ok(ApiResponse.success(200, "SUCCESS", "문제 상세 정보 조회에 성공하였습니다.", response));
    }

    // 테스트케이스 관리 API

    
     // 특정 문제에 대한 테스트케이스 목록을 일괄 저장합니다.
    
    @PostMapping("/problems/{problemId}/testcases")
    public ResponseEntity<ApiResponse<Void>> saveTestCases(
            @PathVariable Long problemId,
            @Valid @RequestBody List<TestCaseCreateRequest> testCaseRequests) {
        fileService.saveTestCases(problemId, testCaseRequests);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(201, "SUCCESS", "테스트케이스가 성공적으로 저장되었습니다."));
    }

    // 코드 제출 및 저장 API

     // 학생이 작성한 코드를 임시 저장하거나 최종 제출(채점 요청)합니다.

    @PostMapping("/submissions")
    public ResponseEntity<ApiResponse<Void>> submitCode(@Valid @RequestBody SubmissionRequest request) {
        fileService.submitCode(request);
        
        // request 내부의 최종 제출 여부(isFinalSubmit)에 따라 다르게 나갈 메시지 동적 처리 가능
        String message = request.isFinalSubmit() ? "코드가 성공적으로 제출되었습니다." : "코드가 성공적으로 임시 저장되었습니다.";
        
        return ResponseEntity.ok(ApiResponse.success(200, "SUCCESS", message));
    }
}