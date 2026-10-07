package com.tastyhouse.webapi.review.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.application.auth.security.MemberUserDetails;
import com.tastyhouse.application.review.port.in.ReviewCommentCreateCommand;
import com.tastyhouse.application.review.port.in.ReviewCommentCreateUseCase;
import com.tastyhouse.application.review.port.in.ReviewCommentListQueryUseCase;
import com.tastyhouse.application.review.port.in.ReviewCommentLookupUseCase;
import com.tastyhouse.application.review.port.in.ReviewReplyCreateCommand;
import com.tastyhouse.application.review.port.in.ReviewReplyCreateUseCase;
import com.tastyhouse.application.review.port.in.ReviewVisibilityQueryUseCase;
import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.webapi.security.CurrentUser;
import com.tastyhouse.webapi.review.adapter.in.web.request.CommentCreateRequest;
import com.tastyhouse.webapi.review.adapter.in.web.request.ReplyCreateRequest;
import com.tastyhouse.webapi.review.adapter.in.web.response.ReviewCommentListResponse;

@RestController
@RequestMapping("/api/reviews")
@Tag(name = "Review Comment", description = "리뷰 댓글 API")
class ReviewCommentApiController {

    private final ReviewCommentCreateUseCase reviewCommentCreateUseCase;
    private final ReviewCommentLookupUseCase reviewCommentLookupUseCase;
    private final ReviewReplyCreateUseCase reviewReplyCreateUseCase;
    private final ReviewVisibilityQueryUseCase reviewVisibilityQueryUseCase;
    private final ReviewCommentListQueryUseCase reviewCommentListQueryUseCase;

    public ReviewCommentApiController(
        ReviewCommentCreateUseCase reviewCommentCreateUseCase,
        ReviewCommentLookupUseCase reviewCommentLookupUseCase,
        ReviewReplyCreateUseCase reviewReplyCreateUseCase,
        ReviewVisibilityQueryUseCase reviewVisibilityQueryUseCase,
        ReviewCommentListQueryUseCase reviewCommentListQueryUseCase
    ) {
        this.reviewCommentCreateUseCase = reviewCommentCreateUseCase;
        this.reviewCommentLookupUseCase = reviewCommentLookupUseCase;
        this.reviewReplyCreateUseCase = reviewReplyCreateUseCase;
        this.reviewVisibilityQueryUseCase = reviewVisibilityQueryUseCase;
        this.reviewCommentListQueryUseCase = reviewCommentListQueryUseCase;
    }

    @Operation(summary = "댓글 등록", description = "리뷰에 댓글을 등록합니다. 생성된 댓글 ID를 반환합니다.")
    @PostMapping("/v1/{id}/comments")
    public ResponseEntity<ApiResponse<Long>> createComment(
        @Parameter(description = "리뷰 ID", example = "1") @PathVariable Long id,
        @Valid @RequestBody CommentCreateRequest request,
        @CurrentUser MemberUserDetails userDetails
    ) {
        reviewVisibilityQueryUseCase.requireVisibleReview(id, userDetails.getMemberId());
        ReviewCommentCreateCommand command = request.toCommand(userDetails.getMemberId(), id);
        Long commentId = reviewCommentCreateUseCase.createComment(command);
        return ResponseEntity.ok(ApiResponse.success(commentId));
    }

    @Operation(summary = "답글 등록", description = "댓글에 답글을 등록합니다. 생성된 답글 ID를 반환합니다.")
    @PostMapping("/v1/comments/{commentId}/replies")
    public ResponseEntity<ApiResponse<Long>> createReply(
        @Parameter(description = "댓글 ID", example = "1") @PathVariable Long commentId,
        @Valid @RequestBody ReplyCreateRequest request,
        @CurrentUser MemberUserDetails userDetails
    ) {
        Long parentReviewId = reviewCommentLookupUseCase.findReviewIdOfComment(commentId);
        reviewVisibilityQueryUseCase.requireVisibleReview(parentReviewId, userDetails.getMemberId());
        ReviewReplyCreateCommand command = request.toCommand(userDetails.getMemberId(), commentId);
        Long replyId = reviewReplyCreateUseCase.createReply(command);
        return ResponseEntity.ok(ApiResponse.success(replyId));
    }

    @Operation(summary = "댓글 및 답글 조회", description = "리뷰의 모든 댓글과 답글을 조회합니다.")
    @GetMapping("/v1/{id}/comments")
    public ResponseEntity<ApiResponse<ReviewCommentListResponse>> getComments(
        @Parameter(description = "리뷰 ID", example = "1") @PathVariable Long id,
        @CurrentUser MemberUserDetails userDetails
    ) {
        ReviewCommentListResponse response = ReviewCommentListResponse.from(
            reviewCommentListQueryUseCase.searchCommentsWithReplies(id, memberIdOrNull(userDetails))
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    private Long memberIdOrNull(MemberUserDetails userDetails) {
        return userDetails == null ? null : userDetails.getMemberId();
    }
}
