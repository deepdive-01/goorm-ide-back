package com.ide.project.domain.feedback.controller;

import com.ide.project.domain.feedback.dto.request.CommentCreateRequest;
import com.ide.project.domain.feedback.dto.request.FeedbackUpdateRequest;
import com.ide.project.domain.feedback.dto.request.HighlightCreateRequest;
import com.ide.project.domain.feedback.dto.response.FeedbackResponse;
import com.ide.project.domain.feedback.service.FeedbackService;
import com.ide.project.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Feedback", description = "피드백 API")
@RestController
@RequestMapping("/api/v1/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    @Operation(summary = "코멘트 피드백 등록", description = "멘토가 제출 코드에 코멘트 피드백을 등록합니다.", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/comments")
    public ResponseEntity<ApiResponse<FeedbackResponse>> createComment(
            @Valid @RequestBody CommentCreateRequest request
    ) {
        Long mentorId = getMentorId();
        FeedbackResponse data = feedbackService.createComment(mentorId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(201, "FEEDBACK_CREATED", "피드백이 등록되었습니다.", data));
    }

    @Operation(summary = "하이라이트 피드백 등록", description = "멘토가 특정 라인에 하이라이트 피드백을 등록합니다.", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/highlights")
    public ResponseEntity<ApiResponse<FeedbackResponse>> createHighlight(
            @Valid @RequestBody HighlightCreateRequest request
    ) {
        Long mentorId = getMentorId();
        FeedbackResponse data = feedbackService.createHighlight(mentorId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(201, "FEEDBACK_CREATED", "피드백이 등록되었습니다.", data));
    }

    @Operation(summary = "피드백 목록 조회", description = "submission_id로 해당 제출의 피드백 목록을 등록 순으로 조회합니다.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping
    public ResponseEntity<ApiResponse<List<FeedbackResponse>>> getList(
            @RequestParam("submission_id") Long submissionId
    ) {
        List<FeedbackResponse> data = feedbackService.getList(submissionId);
        return ResponseEntity.ok(ApiResponse.success(200, "FEEDBACK_LIST_SUCCESS", "피드백 목록을 조회했습니다.", data));
    }

    @Operation(summary = "피드백 수정", description = "작성자 본인만 피드백 내용을 수정할 수 있습니다.", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/{feedbackId}")
    public ResponseEntity<ApiResponse<Void>> update(
            @PathVariable Long feedbackId,
            @Valid @RequestBody FeedbackUpdateRequest request
    ) {
        Long mentorId = getMentorId();
        feedbackService.update(mentorId, feedbackId, request);
        return ResponseEntity.ok(ApiResponse.success(200, "FEEDBACK_UPDATED", "피드백이 수정되었습니다."));
    }

    @Operation(summary = "피드백 삭제", description = "작성자 본인만 피드백을 삭제할 수 있습니다.", security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/{feedbackId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long feedbackId
    ) {
        Long mentorId = getMentorId();
        feedbackService.delete(mentorId, feedbackId);
        return ResponseEntity.ok(ApiResponse.success(200, "FEEDBACK_DELETED", "피드백이 삭제되었습니다."));
    }

    private Long getMentorId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (Long) auth.getPrincipal();
    }
}
