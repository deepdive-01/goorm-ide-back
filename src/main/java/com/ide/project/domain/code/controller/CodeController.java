package com.ide.project.domain.code.controller;

import com.ide.project.domain.code.dto.CodeExecuteRequest;
import com.ide.project.domain.code.dto.CodeExecuteResponse;
import com.ide.project.domain.code.service.CodeService;
import com.ide.project.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/code")
@RequiredArgsConstructor
public class CodeController {

    private final CodeService codeService;

    @PostMapping("/execute")
    public ResponseEntity<ApiResponse<CodeExecuteResponse>> execute(
            @Valid @RequestBody CodeExecuteRequest request) {
        CodeExecuteResponse response = codeService.execute(request);
        return ResponseEntity.ok(
            ApiResponse.success(200, "SUCCESS", "코드 실행 성공", response));
    }
}