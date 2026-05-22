package com.ide.project.domain.space.controller;

import com.ide.project.domain.space.dto.request.SpaceCreateRequest;
import com.ide.project.domain.space.dto.request.SpaceInviteEmailRequest;
import com.ide.project.domain.space.dto.request.SpaceJoinRequest;
import com.ide.project.domain.space.dto.request.SpaceUpdateRequest;
import com.ide.project.domain.space.dto.response.*;
import com.ide.project.domain.space.service.SpaceService;
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

import java.util.List;

@Tag(name = "Space", description = "스페이스 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/spaces")
public class SpaceController {
    private final SpaceService spaceService;

    //POST /api/v1/spaces - 워크 스페이스 생성
    @Operation(
            summary = "스페이스 생성",
            description = "새로운 스페이스를 생성합니다. 멘토 계정만 생성이 가능합니다",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping
    public ResponseEntity<ApiResponse<SpaceCreateResponse>> createSpace(
            @Valid
            @RequestBody
            SpaceCreateRequest request
    ) {
        Long userId = getCurrentUserId();
        SpaceCreateResponse response = spaceService.createSpace(request, userId);

        return ResponseEntity.status(201).body(ApiResponse.success(201, "SPACE_CREATE_SUCCESS", "워크스페이스가 생성됐습니다.", response));
    }

    // GET /api/v1/spaces - 내 워크스페이스 목록 조회
    @Operation(
            summary = "내 워크스페이스 목록 조회",
            description = "로그인한 사용자의 역할에 따라 멘토는 본인이 생성한 목록, 학생은 참여한 목록을 반환합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping
    public ResponseEntity<ApiResponse<List<SpaceListItemResponse>>> getMySpaces() {
        Long userId = getCurrentUserId();
        List<SpaceListItemResponse> responses = spaceService.getMySpaces(userId);
        return ResponseEntity.ok(ApiResponse.success(200, "SUCCESS", "워크스페이스 목록을 조회했습니다.", responses));
    }

    // GET /api/v1/spaces/{spacesId} - 워크스페이스 단건 상세 조회
    @Operation(
            summary = "워크스페이스 단건 상세 조회",
            description = "워크스페이스의 상세 정보를 조회합니다. 소속 멘토에게만 초대 코드를 보여줍니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/{spaceId}")
    public ResponseEntity<ApiResponse<SpaceDetailResponse>> getSpace(
            @PathVariable Long spaceId
    ) {
        Long userId = getCurrentUserId();
        SpaceDetailResponse response = spaceService.getSpace(spaceId, userId);
        return ResponseEntity.ok(ApiResponse.success(200, "SUCCESS", "워크스페이스를 조회했습니다.", response));
    }

    // PATCH /api/v1/spaces/{spaceId} - 워크스페이스 수정
    // id, name, description, isActive만 수정할 수 있습니다.
    @Operation(
            summary = "워크스페이스 수정",
            description = "워크스페이스의 이름, 설명, 활성 상태를 수정할 수 있습니다. 멘토만 가능합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PatchMapping("/{spaceId}")
    public ResponseEntity<ApiResponse<SpaceUpdateResponse>> updateSpace(
            @PathVariable
            Long spaceId,
            @Valid
            @RequestBody
            SpaceUpdateRequest request
    ) {
        Long userId = getCurrentUserId();
        SpaceUpdateResponse response = spaceService.updateSpace(spaceId, request, userId);
        return ResponseEntity.ok(ApiResponse.success(200, "SPACE_UPDATE_SUCCESS", "워크스페이스가 수정됐습니다.", response));
    }

    // POST /api/v1/spaces/join - 초대코드로 워크스페이스 참여
    @Operation(
            summary = "초대코드로 워크스페이스 참여",
            description = "학생 유저가 초대코드를 사용하여 워크스페이스에 참여합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/join")
    public ResponseEntity<ApiResponse<SpaceJoinResponse>> joinSpace(
            @Valid
            @RequestBody
            SpaceJoinRequest request
    ) {
        Long userId = getCurrentUserId();
        SpaceJoinResponse response = spaceService.joinSpace(request, userId);
        return ResponseEntity.ok(ApiResponse.success(200, "SPACE_JOIN_SUCCESS", "워크스페이스에 참여했습니다.",response));
    }

    // GET /api/v1/spaces/{spacesId}/members - 스페이스 내 학생 목록 조회
    @Operation(
            summary = "워크스페이스 내 학생 목록 조회",
            description = "워크스페이스에 소속된 모든 학생들의 목록을 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/{spaceId}/members")
    public ResponseEntity<ApiResponse<SpaceMemberListResponse>> getMembers(
            @PathVariable
            Long spaceId
    ) {
        Long userId = getCurrentUserId();
        SpaceMemberListResponse response = spaceService.getSpaceMembers(spaceId, userId);
        return ResponseEntity.ok(ApiResponse.success(200, "SUCCESS", "멤버 목록을 조회했습니다.", response));
    }

    // POST /api/v1/spaces/{spaceId}/invite/email - 초대코드 이메일 발송
    @Operation(
            summary = "초대 이메일 발송",
            description = "입력한 이메일 목록으로 워크스페이스 가입 초대 코드 메일을 일괄 발송합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/{spaceId}/invite/email")
    public ResponseEntity<ApiResponse<SpaceInviteEmailResponse>> sendInviteEmail(
            @PathVariable
            Long spaceId,
            @Valid
            @RequestBody
            SpaceInviteEmailRequest request
    ) {
        Long userId = getCurrentUserId();
        SpaceInviteEmailResponse response = spaceService.sendInviteEmail(spaceId, request, userId);

        return ResponseEntity.ok(ApiResponse.success(200, "INVITE_EMAIL_SENT", "초대 이메일이 발송됐습니다.", response));
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (Long) auth.getPrincipal();
    }


}
