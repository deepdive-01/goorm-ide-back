package com.ide.project.domain.code.controller;

import com.ide.project.domain.code.dto.*;
import com.ide.project.domain.code.service.CodeService;
import com.ide.project.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/code")
@RequiredArgsConstructor
public class CodeController {

    private final CodeService codeService;

    // 코드 채점
    @PostMapping("/grade")
    public ResponseEntity<ApiResponse<GradeResponse>> grade(
            @Valid @RequestBody GradeRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) auth.getPrincipal();
        GradeResponse response = codeService.grade(request, userId);
        return ResponseEntity.ok(
            ApiResponse.success(200, "SUCCESS", "코드 채점 완료", response));
    }
}