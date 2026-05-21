package com.ide.project.domain.files.controller;

import com.ide.project.domain.files.dto.CodeUpdateRequest;
import com.ide.project.domain.files.dto.ProblemCreateRequest;
import com.ide.project.domain.files.dto.ProblemResponse;
import com.ide.project.domain.files.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    // 강사의 새로운 문제 생성 API
    @PostMapping("/problems")
    public ResponseEntity<ProblemResponse> createProblem(@Valid @RequestBody ProblemCreateRequest request) {
        ProblemResponse response = fileService.createProblem(request);
        return ResponseEntity.ok(response);
    }

    // 특정 문제 정보 조회 API
    @GetMapping("/problems/{problemId}")
    public ResponseEntity<ProblemResponse> getProblem(@PathVariable Long problemId) {
        ProblemResponse response = fileService.getProblemDetails(problemId);
        return ResponseEntity.ok(response);
    }

    // 학생의 코드 임시 저장 및 제출 API
    @PostMapping("/submissions")
    public ResponseEntity<Void> submitCode(@Valid @RequestBody CodeUpdateRequest request) {
        fileService.updateCodeSubmission(request);
        return ResponseEntity.ok().build();
    }
}