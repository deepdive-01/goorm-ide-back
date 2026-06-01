package com.ide.project.domain.code.controller;

import com.ide.project.domain.code.dto.*;
import com.ide.project.domain.code.service.CodeService;
import com.ide.project.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/code")
@RequiredArgsConstructor
@Tag(name = "코드 실행 / 채점", description = "코드 실행 및 채점 API")
public class CodeController {

    private final CodeService codeService;

    @Operation(summary = "코드 채점", description = "테스트케이스 기반으로 코드를 채점합니다. 채점 결과는 PASS/FAIL로 반환됩니다.")
    @PostMapping("/grade")
    public ResponseEntity<ApiResponse<GradeResponse>> grade(
            @Valid @RequestBody GradeRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) auth.getPrincipal();
        GradeResponse response = codeService.grade(request, userId);
        return ResponseEntity.ok(
            ApiResponse.success(200, "SUCCESS", "채점 완료", response));
    }
}