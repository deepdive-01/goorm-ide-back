package com.ide.project.domain.files.controller;

import com.ide.project.domain.files.dto.*;
import com.ide.project.domain.files.service.FileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/files") // 도메인명에 맞춘 Base URI
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

  
    //  문제 관리 API

    /**
     * 강사가 새로운 문제를 생성합니다.
     */
    @PostMapping("/problems")
    public ResponseEntity<ProblemResponse> createProblem(@Valid @RequestBody ProblemCreateRequest request) {
        ProblemResponse response = fileService.createProblem(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    //기존 문제의 내용(지문, 난이도, 언어 등)을 수정합니다.

    @PutMapping("/problems/{problemId}")
    public ResponseEntity<ProblemResponse> updateProblem(
            @PathVariable Long problemId,
            @Valid @RequestBody ProblemUpdateRequest request) {
        ProblemResponse response = fileService.updateProblem(problemId, request);
        return ResponseEntity.ok(response);
    }

    //  문제 은행에 있는 원본 문제를 강사의 워크스페이스로 복사하여 배정합니다.

    @PostMapping("/problems/assign")
    public ResponseEntity<ProblemResponse> assignProblemFromBank(@Valid @RequestBody ProblemAssignRequest request) {
        ProblemResponse response = fileService.assignProblemFromBank(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    //  특정 문제의 상세 정보를 조회합니다.
    @GetMapping("/problems/{problemId}")
    public ResponseEntity<ProblemResponse> getProblemDetails(@PathVariable Long problemId) {
        ProblemResponse response = fileService.getProblemDetails(problemId);
        return ResponseEntity.ok(response);
    }


    //  테스트케이스 관리 API


    
     // 특정 문제에 대한 테스트케이스 목록을 일괄 저장합니다.

    @PostMapping("/problems/{problemId}/testcases")
    public ResponseEntity<Void> saveTestCases(
            @PathVariable Long problemId,
            @Valid @RequestBody List<TestCaseCreateRequest> testCaseRequests) {
        fileService.saveTestCases(problemId, testCaseRequests);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


    //  코드 제출 및 저장 API



    //  학생이 작성한 코드를 임시 저장하거나 최종 제출(채점 요청)합니다.

    @PostMapping("/submissions")
    public ResponseEntity<Void> submitCode(@Valid @RequestBody SubmissionRequest request) {
        fileService.submitCode(request);
        return ResponseEntity.ok().build();
    }
}