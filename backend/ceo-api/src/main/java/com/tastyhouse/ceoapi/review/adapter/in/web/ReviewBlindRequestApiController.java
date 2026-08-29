package com.tastyhouse.ceoapi.review.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.ceoapi.config.security.CustomUserDetails;
import com.tastyhouse.ceoapi.review.adapter.in.web.request.ReviewBlindRequestCreateRequest;
import com.tastyhouse.ceoapi.review.application.port.in.ReviewBlindRequestCancelCommand;
import com.tastyhouse.ceoapi.review.application.port.in.ReviewBlindRequestCommandUseCase;
import com.tastyhouse.ceoapi.review.application.port.in.ReviewBlindRequestCreateCommand;

@Tag(name = "Ceo Review Blind Request", description = "점주 리뷰 게시중단 요청 API")
@RestController
@RequestMapping("/api/shops")
public class ReviewBlindRequestApiController {

    private final ReviewBlindRequestCommandUseCase reviewBlindRequestCommandUseCase;

    public ReviewBlindRequestApiController(ReviewBlindRequestCommandUseCase reviewBlindRequestCommandUseCase) {
        this.reviewBlindRequestCommandUseCase = reviewBlindRequestCommandUseCase;
    }

    @Operation(
        summary = "게시중단 요청 등록",
        description = "부당한 리뷰의 게시중단을 관리자에게 요청합니다. 같은 리뷰에 대기중인 요청이 이미 "
            + "있으면 409를 반환하며, 사유가 ETC면 상세 사유가 필수입니다. 접수된 요청은 요청처리 현황에서도 "
            + "함께 조회됩니다."
    )
    @PostMapping("/v1/{id}/reviews/{reviewId}/blind-requests")
    public ResponseEntity<ApiResponse<Long>> createBlindRequest(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long id,
        @PathVariable Long reviewId,
        @Valid @RequestBody ReviewBlindRequestCreateRequest request
    ) {
        ReviewBlindRequestCreateCommand command = request.toCommand(userDetails.getCeoId(), id, reviewId);
        Long blindRequestId = reviewBlindRequestCommandUseCase.request(command);
        return ResponseEntity.ok(ApiResponse.success(blindRequestId));
    }

    @Operation(
        summary = "게시중단 요청 취소",
        description = "대기중인 게시중단 요청을 취소합니다. 이미 승인·반려된 요청은 취소할 수 없습니다. "
            + "취소 후에는 같은 리뷰에 다시 요청할 수 있습니다."
    )
    @PatchMapping("/v1/{id}/reviews/blind-requests/{requestId}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelBlindRequest(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long id,
        @PathVariable Long requestId
    ) {
        ReviewBlindRequestCancelCommand command =
            ReviewBlindRequestCancelCommand.of(userDetails.getCeoId(), id, requestId);
        reviewBlindRequestCommandUseCase.cancel(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
