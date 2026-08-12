package com.tastyhouse.adminapi.shop;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.adminapi.config.security.CustomUserDetails;
import com.tastyhouse.adminapi.shop.request.ShopRequestCommentCreateRequest;
import com.tastyhouse.adminapi.shop.response.ShopRequestCommentResponse;

@Tag(name = "Admin Shop Request Comment", description = "관리자 요청건 문의 답변 API")
@RestController
@RequestMapping("/api/shops")
public class ShopRequestCommentApiController {

    private final ShopRequestCommentQueryService shopRequestCommentQueryService;
    private final ShopRequestCommentCommandService shopRequestCommentCommandService;

    public ShopRequestCommentApiController(
        ShopRequestCommentQueryService shopRequestCommentQueryService,
        ShopRequestCommentCommandService shopRequestCommentCommandService
    ) {
        this.shopRequestCommentQueryService = shopRequestCommentQueryService;
        this.shopRequestCommentCommandService = shopRequestCommentCommandService;
    }

    @Operation(
        summary = "요청건 문의 스레드 조회",
        description = "점주 요청 1건에 오간 문의와 답변을 작성순으로 조회합니다. 관리자는 가게 제약 없이 모든 요청의 스레드를 조회합니다."
    )
    @GetMapping("/v1/requests/{requestId}/comments")
    public ResponseEntity<ApiResponse<List<ShopRequestCommentResponse>>> getComments(
        @PathVariable Long requestId
    ) {
        List<ShopRequestCommentResponse> response = shopRequestCommentQueryService.getComments(requestId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(
        summary = "요청건 담당자 답변 작성",
        description = "점주 문의에 담당자 답변을 남깁니다. 처리 상태와 무관하게 작성할 수 있습니다."
    )
    @PostMapping("/v1/requests/{requestId}/comments")
    public ResponseEntity<ApiResponse<Long>> createComment(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long requestId,
        @Valid @RequestBody ShopRequestCommentCreateRequest request
    ) {
        Long commentId = shopRequestCommentCommandService.addComment(
            requestId,
            userDetails.getPrincipalId(),
            request.content()
        );
        return ResponseEntity.ok(ApiResponse.success(commentId));
    }
}
