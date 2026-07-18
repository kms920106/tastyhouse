package com.tastyhouse.adminapi.review;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.adminapi.common.ApiResponse;
import com.tastyhouse.adminapi.common.PageRequest;
import com.tastyhouse.adminapi.common.PaginationResponse;
import com.tastyhouse.adminapi.review.request.ReviewHiddenUpdateRequest;
import com.tastyhouse.adminapi.review.request.ReviewSearchRequest;
import com.tastyhouse.adminapi.review.response.ReviewCommentListItemResponse;
import com.tastyhouse.adminapi.review.response.ReviewListItemResponse;
import com.tastyhouse.adminapi.review.response.ReviewManagementDetailResponse;

@Tag(name = "Review Admin", description = "리뷰 관리자 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
public class ReviewApiController {

    private final ReviewService reviewService;

    @Operation(summary = "리뷰 목록 조회", description = "리뷰 목록을 페이징 조회합니다. (숨김 리뷰 포함) shopId/productId/memberId/hidden/content/평점 범위로 필터링할 수 있습니다.")
    @GetMapping("/v1")
    public ResponseEntity<ApiResponse<List<ReviewListItemResponse>>> getReviews(
        @Valid @ModelAttribute ReviewSearchRequest search,
        @Valid @ModelAttribute PageRequest pageRequest
    ) {
        PaginationResponse<ReviewListItemResponse> pageResponse = reviewService.getReviews(
            search.shopId(),
            search.productId(),
            search.memberId(),
            search.hidden(),
            search.content(),
            search.minRating(),
            search.maxRating(),
            pageRequest.page(),
            pageRequest.size()
        );
        return ResponseEntity.ok(ApiResponse.success(pageResponse.content(), pageResponse.page(), pageResponse.size(), pageResponse.totalElements()));
    }

    @Operation(summary = "리뷰 상세 조회", description = "숨김 리뷰를 포함하여 리뷰 상세 정보를 조회합니다.")
    @GetMapping("/v1/{id}")
    public ResponseEntity<ApiResponse<ReviewManagementDetailResponse>> getReview(@PathVariable Long id) {
        ReviewManagementDetailResponse response = reviewService.getReview(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "리뷰 숨김/노출 전환", description = "리뷰의 숨김 여부를 전환합니다.")
    @PutMapping("/v1/{id}/hidden")
    public ResponseEntity<ApiResponse<Void>> changeReviewHidden(
        @PathVariable Long id,
        @Valid @RequestBody ReviewHiddenUpdateRequest request
    ) {
        reviewService.changeReviewHidden(id, request.hidden());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "리뷰 삭제", description = "소유권 검증 없이 리뷰를 삭제합니다.")
    @DeleteMapping("/v1/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "리뷰 댓글/답글 조회", description = "리뷰의 모든 댓글과 답글을 숨김 포함하여 조회합니다.")
    @GetMapping("/v1/{id}/comments")
    public ResponseEntity<ApiResponse<List<ReviewCommentListItemResponse>>> getComments(@PathVariable Long id) {
        List<ReviewCommentListItemResponse> response = reviewService.getComments(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "댓글 숨김/노출 전환", description = "댓글의 숨김 여부를 전환합니다.")
    @PutMapping("/v1/comments/{commentId}/hidden")
    public ResponseEntity<ApiResponse<Void>> changeCommentHidden(
        @PathVariable Long commentId,
        @Valid @RequestBody ReviewHiddenUpdateRequest request
    ) {
        reviewService.changeCommentHidden(commentId, request.hidden());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "댓글 삭제", description = "댓글을 삭제합니다.")
    @DeleteMapping("/v1/comments/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(@PathVariable Long commentId) {
        reviewService.deleteComment(commentId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "답글 숨김/노출 전환", description = "답글의 숨김 여부를 전환합니다.")
    @PutMapping("/v1/replies/{replyId}/hidden")
    public ResponseEntity<ApiResponse<Void>> changeReplyHidden(
        @PathVariable Long replyId,
        @Valid @RequestBody ReviewHiddenUpdateRequest request
    ) {
        reviewService.changeReplyHidden(replyId, request.hidden());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "답글 삭제", description = "답글을 삭제합니다.")
    @DeleteMapping("/v1/replies/{replyId}")
    public ResponseEntity<ApiResponse<Void>> deleteReply(@PathVariable Long replyId) {
        reviewService.deleteReply(replyId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
