package com.tastyhouse.ceoapi.review.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.ceoapi.config.security.CustomUserDetails;
import com.tastyhouse.ceoapi.review.adapter.in.web.request.ReviewOwnerReplyCreateRequest;
import com.tastyhouse.ceoapi.review.application.port.in.ReviewOwnerReplyCommandUseCase;
import com.tastyhouse.ceoapi.review.application.port.in.ReviewOwnerReplyCreateCommand;
import com.tastyhouse.ceoapi.review.application.port.in.ReviewOwnerReplyDeleteCommand;
import com.tastyhouse.ceoapi.review.application.port.in.ReviewOwnerReplyUpdateCommand;

@Tag(name = "Ceo Review Owner Reply", description = "점주 사장님 답변 API")
@RestController
@RequestMapping("/api/shops")
public class ReviewOwnerReplyApiController {

    private final ReviewOwnerReplyCommandUseCase reviewOwnerReplyCommandUseCase;

    public ReviewOwnerReplyApiController(ReviewOwnerReplyCommandUseCase reviewOwnerReplyCommandUseCase) {
        this.reviewOwnerReplyCommandUseCase = reviewOwnerReplyCommandUseCase;
    }

    @Operation(
        summary = "사장님 답변 등록",
        description = "리뷰에 사장님 답변을 등록합니다. 리뷰당 1건만 등록할 수 있으며, 이미 답변이 있으면 "
            + "409를 반환합니다. 금칙어가 포함되면 저장되지 않습니다."
    )
    @PostMapping("/v1/{id}/reviews/{reviewId}/owner-reply")
    public ResponseEntity<ApiResponse<Long>> createOwnerReply(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long id,
        @PathVariable Long reviewId,
        @Valid @RequestBody ReviewOwnerReplyCreateRequest request
    ) {
        ReviewOwnerReplyCreateCommand command = request.toCommand(userDetails.getCeoId(), id, reviewId);
        Long ownerReplyId = reviewOwnerReplyCommandUseCase.register(command);
        return ResponseEntity.ok(ApiResponse.success(ownerReplyId));
    }

    @Operation(
        summary = "사장님 답변 수정",
        description = "등록한 사장님 답변의 내용을 수정합니다. 답변이 없으면 404를 반환합니다."
    )
    @PutMapping("/v1/{id}/reviews/{reviewId}/owner-reply")
    public ResponseEntity<ApiResponse<Void>> updateOwnerReply(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long id,
        @PathVariable Long reviewId,
        @Valid @RequestBody ReviewOwnerReplyCreateRequest request
    ) {
        ReviewOwnerReplyUpdateCommand command = request.toUpdateCommand(userDetails.getCeoId(), id, reviewId);
        reviewOwnerReplyCommandUseCase.modify(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(
        summary = "사장님 답변 삭제",
        description = "등록한 사장님 답변을 삭제합니다. 삭제 후 같은 리뷰에 다시 답변할 수 있습니다."
    )
    @DeleteMapping("/v1/{id}/reviews/{reviewId}/owner-reply")
    public ResponseEntity<ApiResponse<Void>> deleteOwnerReply(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long id,
        @PathVariable Long reviewId
    ) {
        ReviewOwnerReplyDeleteCommand command =
            ReviewOwnerReplyDeleteCommand.of(userDetails.getCeoId(), id, reviewId);
        reviewOwnerReplyCommandUseCase.remove(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
