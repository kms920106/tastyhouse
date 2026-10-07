package com.tastyhouse.webapi.review.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.application.auth.security.MemberUserDetails;
import com.tastyhouse.application.review.port.in.ReviewLikeStatusQueryUseCase;
import com.tastyhouse.application.review.port.in.ReviewLikeToggleCommand;
import com.tastyhouse.application.review.port.in.ReviewLikeToggleUseCase;
import com.tastyhouse.application.review.port.in.ReviewVisibilityQueryUseCase;
import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.webapi.security.CurrentUser;
import com.tastyhouse.webapi.review.adapter.in.web.response.ReviewLikeResponse;
import com.tastyhouse.webapi.review.adapter.in.web.response.ReviewLikeStatusResponse;

@RestController
@RequestMapping("/api/reviews")
@Tag(name = "Review Like", description = "리뷰 좋아요 API")
class ReviewLikeApiController {

    private final ReviewLikeToggleUseCase reviewLikeToggleUseCase;
    private final ReviewLikeStatusQueryUseCase reviewLikeStatusQueryUseCase;
    private final ReviewVisibilityQueryUseCase reviewVisibilityQueryUseCase;

    public ReviewLikeApiController(
        ReviewLikeToggleUseCase reviewLikeToggleUseCase,
        ReviewLikeStatusQueryUseCase reviewLikeStatusQueryUseCase,
        ReviewVisibilityQueryUseCase reviewVisibilityQueryUseCase
    ) {
        this.reviewLikeToggleUseCase = reviewLikeToggleUseCase;
        this.reviewLikeStatusQueryUseCase = reviewLikeStatusQueryUseCase;
        this.reviewVisibilityQueryUseCase = reviewVisibilityQueryUseCase;
    }

    @Operation(summary = "리뷰 좋아요 여부 조회", description = "리뷰가 현재 사용자에 의해 좋아요되었는지 여부를 조회합니다.")
    @GetMapping("/v1/{id}/like")
    public ResponseEntity<ApiResponse<ReviewLikeStatusResponse>> isLiked(
        @PathVariable Long id,
        @CurrentUser MemberUserDetails userDetails
    ) {
        ReviewLikeStatusResponse liked;
        if (userDetails == null) {
            liked = ReviewLikeStatusResponse.from(false);
        } else {
            Long memberId = userDetails.getMemberId();
            liked = ReviewLikeStatusResponse.from(reviewLikeStatusQueryUseCase.isLiked(id, memberId));
        }
        return ResponseEntity.ok(ApiResponse.success(liked));
    }

    @Operation(summary = "리뷰 좋아요 토글", description = "리뷰에 좋아요를 토글합니다. 이미 좋아요한 경우 취소되고, 아닌 경우 좋아요가 추가됩니다.")
    @PostMapping("/v1/{id}/like")
    public ResponseEntity<ApiResponse<ReviewLikeResponse>> toggleReviewLike(
        @Parameter(description = "리뷰 ID", example = "1") @PathVariable Long id,
        @CurrentUser MemberUserDetails userDetails
    ) {
        reviewVisibilityQueryUseCase.requireVisibleReview(id, userDetails.getMemberId());
        ReviewLikeToggleCommand command = ReviewLikeToggleCommand.of(userDetails.getMemberId(), id);
        boolean liked = reviewLikeToggleUseCase.toggleReviewLike(command);
        return ResponseEntity.ok(ApiResponse.success(ReviewLikeResponse.from(liked)));
    }
}
