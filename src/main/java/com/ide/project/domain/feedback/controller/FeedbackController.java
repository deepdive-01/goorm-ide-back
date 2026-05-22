package com.ide.project.domain.feedback.controller;

import com.ide.project.domain.feedback.dto.request.CommentCreateRequest;
import com.ide.project.domain.feedback.dto.response.FeedbackResponse;
import com.ide.project.domain.feedback.service.FeedbackService;
import com.ide.project.domain.user.entity.Role;
import com.ide.project.domain.user.entity.User;
import com.ide.project.domain.user.repository.UserRepository;
import com.ide.project.global.exception.ErrorCode;
import com.ide.project.global.exception.custom.BusinessException;
import com.ide.project.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Feedback", description = "피드백 API")
@RestController
@RequestMapping("/api/v1/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;
    private final UserRepository userRepository;

    @Operation(summary = "전체 피드백 작성", description = "제출 코드 전체에 대한 피드백을 작성합니다. MENTOR만 가능합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/comments")
    public ResponseEntity<ApiResponse<FeedbackResponse>> createComment(
            @Valid @RequestBody CommentCreateRequest request
    ) {
        Long mentorId = getCurrentUserId();
        checkMentorRole(mentorId);

        FeedbackResponse response = feedbackService.createComment(request, mentorId);
        return ResponseEntity.status(201)
                .body(ApiResponse.success(201, "FEEDBACK_CREATED", "피드백이 등록되었습니다.", response));
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (Long) auth.getPrincipal();
    }

    private void checkMentorRole(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (user.getRole() != Role.MENTOR) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }
}
