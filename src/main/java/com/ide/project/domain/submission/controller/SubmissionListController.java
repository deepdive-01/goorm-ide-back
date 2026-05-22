package com.ide.project.domain.submission.controller;

import com.ide.project.domain.submission.dto.response.SubmissionListResponse;
import com.ide.project.domain.submission.service.SubmissionListService;
import com.ide.project.global.exception.ErrorCode;
import com.ide.project.global.exception.custom.BusinessException;
import com.ide.project.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "02. Submission", description = "문제 제출 학생 목록 조회 API")
@RestController
@RequestMapping("/api/v1/questions")
@RequiredArgsConstructor
public class SubmissionListController {

    private final SubmissionListService submissionListService;

    @Operation(
            summary = "제출 학생 목록 조회",
            description = "특정 문제에 코드를 제출한 학생 목록을 조회합니다. MENTOR 전용 API입니다.\n\n" +
                    "status 파라미터로 필터링 가능합니다: PENDING / SUCCESS / FAIL / ERROR (생략 시 전체 조회)",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/{questionId}/submissions")
    public ResponseEntity<ApiResponse<SubmissionListResponse>> getSubmissions(
            @Parameter(description = "조회할 문제 ID", required = true)
            @PathVariable Long questionId,
            @Parameter(description = "제출 상태 필터 (PENDING / SUCCESS / FAIL / ERROR), 생략 시 전체")
            @RequestParam(required = false) String status
    ) {
        requireMentor(SecurityContextHolder.getContext().getAuthentication());

        SubmissionListResponse data = submissionListService.getSubmissions(questionId, status);
        return ResponseEntity.ok(
                ApiResponse.success(200, "SUBMISSION_LIST_SUCCESS", "제출 학생 목록을 조회했습니다.", data)
        );
    }

    private void requireMentor(Authentication auth) {
        boolean isMentor = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_MENTOR"));
        if (!isMentor) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }
}
