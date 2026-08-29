package com.tastyhouse.adminapi.review.adapter.in.web;

import com.tastyhouse.adminapi.review.application.port.in.ReviewCommandUseCase;
import com.tastyhouse.adminapi.review.application.port.in.ReviewCommentDeleteCommand;
import com.tastyhouse.adminapi.review.application.port.in.ReviewCommentHiddenChangeCommand;
import com.tastyhouse.adminapi.review.application.port.in.ReviewDeleteCommand;
import com.tastyhouse.adminapi.review.application.port.in.ReviewHiddenChangeCommand;
import com.tastyhouse.adminapi.review.application.port.in.ReviewReplyDeleteCommand;
import com.tastyhouse.adminapi.review.application.port.in.ReviewReplyHiddenChangeCommand;
import com.tastyhouse.adminapi.review.application.service.ReviewQueryService;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.apicommon.common.PageRequest;
import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.adminapi.review.adapter.in.web.request.ReviewHiddenUpdateRequest;
import com.tastyhouse.adminapi.review.adapter.in.web.request.ReviewSearchRequest;
import com.tastyhouse.adminapi.review.adapter.in.web.response.ReviewCommentListItemResponse;
import com.tastyhouse.adminapi.review.adapter.in.web.response.ReviewListItemResponse;
import com.tastyhouse.adminapi.review.adapter.in.web.response.ReviewManagementDetailResponse;

@Tag(name = "Review Admin", description = "리뷰 관리자 API")
@RestController
@RequestMapping("/api/reviews")
public class ReviewApiController {

    private final ReviewCommandUseCase reviewCommandUseCase;
    private final ReviewQueryService reviewQueryService;

    public ReviewApiController(ReviewCommandUseCase reviewCommandUseCase, ReviewQueryService reviewQueryService) {
        this.reviewCommandUseCase = reviewCommandUseCase;
        this.reviewQueryService = reviewQueryService;
    }

    @Operation(summary = "리뷰 목록 조회", description = "리뷰 목록을 페이징 조회합니다. (숨김 리뷰 포함) shopId/productId/memberId/hidden/content/평점 범위로 필터링할 수 있습니다.")
    @GetMapping("/v1")
    public ResponseEntity<ApiResponse<List<ReviewListItemResponse>>> getReviews(
        @Valid @ModelAttribute ReviewSearchRequest search,
        @Valid @ModelAttribute PageRequest pageRequest
    ) {
        PaginationResponse<ReviewListItemResponse> pageResponse = reviewQueryService.getReviews(
            search.shopId(),
            search.productId(),
            search.memberId(),
            search.hidden(),
            search.ownerOnly(),
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
        ReviewManagementDetailResponse response = reviewQueryService.getReview(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "리뷰 숨김/노출 전환", description = "리뷰의 숨김 여부를 전환합니다.")
    @PutMapping("/v1/{id}/hidden")
    public ResponseEntity<ApiResponse<Void>> changeReviewHidden(
        @PathVariable Long id,
        @Valid @RequestBody ReviewHiddenUpdateRequest request
    ) {
        ReviewHiddenChangeCommand command = request.toReviewCommand(id);
        reviewCommandUseCase.changeReviewHidden(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "리뷰 삭제", description = "소유권 검증 없이 리뷰를 삭제합니다.")
    @DeleteMapping("/v1/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(@PathVariable Long id) {
        ReviewDeleteCommand command = ReviewDeleteCommand.of(id);
        reviewCommandUseCase.deleteReview(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "리뷰 댓글/답글 조회", description = "리뷰의 모든 댓글과 답글을 숨김 포함하여 조회합니다.")
    @GetMapping("/v1/{id}/comments")
    public ResponseEntity<ApiResponse<List<ReviewCommentListItemResponse>>> getComments(@PathVariable Long id) {
        List<ReviewCommentListItemResponse> response = reviewQueryService.getComments(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "댓글 숨김/노출 전환", description = "댓글의 숨김 여부를 전환합니다.")
    @PutMapping("/v1/comments/{commentId}/hidden")
    public ResponseEntity<ApiResponse<Void>> changeCommentHidden(
        @PathVariable Long commentId,
        @Valid @RequestBody ReviewHiddenUpdateRequest request
    ) {
        ReviewCommentHiddenChangeCommand command = request.toCommentCommand(commentId);
        reviewCommandUseCase.changeCommentHidden(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "댓글 삭제", description = "댓글을 삭제합니다.")
    @DeleteMapping("/v1/comments/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(@PathVariable Long commentId) {
        ReviewCommentDeleteCommand command = ReviewCommentDeleteCommand.of(commentId);
        reviewCommandUseCase.deleteComment(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "답글 숨김/노출 전환", description = "답글의 숨김 여부를 전환합니다.")
    @PutMapping("/v1/replies/{replyId}/hidden")
    public ResponseEntity<ApiResponse<Void>> changeReplyHidden(
        @PathVariable Long replyId,
        @Valid @RequestBody ReviewHiddenUpdateRequest request
    ) {
        ReviewReplyHiddenChangeCommand command = request.toReplyCommand(replyId);
        reviewCommandUseCase.changeReplyHidden(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "답글 삭제", description = "답글을 삭제합니다.")
    @DeleteMapping("/v1/replies/{replyId}")
    public ResponseEntity<ApiResponse<Void>> deleteReply(@PathVariable Long replyId) {
        ReviewReplyDeleteCommand command = ReviewReplyDeleteCommand.of(replyId);
        reviewCommandUseCase.deleteReply(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
