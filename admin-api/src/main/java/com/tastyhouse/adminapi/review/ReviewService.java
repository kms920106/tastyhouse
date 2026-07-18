package com.tastyhouse.adminapi.review;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.tastyhouse.core.domain.review.domain.vo.ReviewCommentId;
import com.tastyhouse.core.domain.review.domain.vo.ReviewId;
import com.tastyhouse.core.domain.review.domain.vo.ReviewReplyId;
import com.tastyhouse.core.domain.review.application.ReviewCommandService;
import com.tastyhouse.core.domain.review.application.ReviewQueryService;
import com.tastyhouse.core.domain.review.application.dto.ReviewSearchCondition;
import com.tastyhouse.core.domain.review.application.dto.result.ReviewCommentListItemResult;
import com.tastyhouse.core.domain.review.application.dto.result.ReviewListItemResult;
import com.tastyhouse.core.domain.review.application.dto.result.ReviewManagementDetailResult;
import com.tastyhouse.core.domain.review.application.dto.result.ReviewReplyListItemResult;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;
import com.tastyhouse.core.shared.page.PageResult;
import com.tastyhouse.external.file.FileService;
import com.tastyhouse.adminapi.common.PaginationResponse;
import com.tastyhouse.adminapi.review.response.ReviewCommentListItemResponse;
import com.tastyhouse.adminapi.review.response.ReviewListItemResponse;
import com.tastyhouse.adminapi.review.response.ReviewManagementDetailResponse;
import com.tastyhouse.adminapi.review.response.ReviewReplyListItemResponse;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewCommandService reviewCommandService;
    private final ReviewQueryService reviewQueryService;
    private final FileService fileService;

    public PaginationResponse<ReviewListItemResponse> getReviews(
        Long shopId,
        Long productId,
        Long memberId,
        Boolean hidden,
        String content,
        Double minRating,
        Double maxRating,
        int page,
        int size
    ) {
        ReviewSearchCondition condition = ReviewSearchCondition.of(shopId, productId, memberId, hidden, content, minRating, maxRating);
        PageResult<ReviewListItemResponse> pageResult = reviewQueryService.findReviews(condition, page, size)
            .map(this::toReviewListItemResponse);
        return PaginationResponse.from(pageResult);
    }

    public ReviewManagementDetailResponse getReview(Long id) {
        ReviewId reviewId = ReviewId.of(id);
        ReviewManagementDetailResult detail = reviewQueryService.findReviewManagementDetail(reviewId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.REVIEW_NOT_FOUND));
        return toReviewManagementDetailResponse(detail);
    }

    public void changeReviewHidden(Long id, boolean hidden) {
        ReviewId reviewId = ReviewId.of(id);
        reviewCommandService.changeReviewHidden(reviewId, hidden);
    }

    public void deleteReview(Long id) {
        ReviewId reviewId = ReviewId.of(id);
        reviewCommandService.deleteReview(reviewId);
    }

    public List<ReviewCommentListItemResponse> getComments(Long id) {
        ReviewId reviewId = ReviewId.of(id);
        List<ReviewCommentListItemResult> comments = reviewQueryService.findCommentsIncludingHidden(reviewId);

        List<ReviewCommentId> commentIds = comments.stream()
            .map(comment -> ReviewCommentId.of(comment.id()))
            .toList();
        List<ReviewReplyListItemResult> replies = reviewQueryService.findRepliesIncludingHidden(commentIds);

        return comments.stream()
            .map(comment -> toReviewCommentListItemResponse(comment, replies))
            .toList();
    }

    public void changeCommentHidden(Long commentId, boolean hidden) {
        ReviewCommentId reviewCommentId = ReviewCommentId.of(commentId);
        reviewCommandService.changeCommentHidden(reviewCommentId, hidden);
    }

    public void deleteComment(Long commentId) {
        ReviewCommentId reviewCommentId = ReviewCommentId.of(commentId);
        reviewCommandService.deleteComment(reviewCommentId);
    }

    public void changeReplyHidden(Long replyId, boolean hidden) {
        ReviewReplyId reviewReplyId = ReviewReplyId.of(replyId);
        reviewCommandService.changeReplyHidden(reviewReplyId, hidden);
    }

    public void deleteReply(Long replyId) {
        ReviewReplyId reviewReplyId = ReviewReplyId.of(replyId);
        reviewCommandService.deleteReply(reviewReplyId);
    }

    private ReviewListItemResponse toReviewListItemResponse(ReviewListItemResult dto) {
        return ReviewListItemResponse.from(
            dto.id(),
            dto.shopId(),
            dto.productId(),
            dto.memberId().value(),
            dto.memberNickname(),
            dto.totalRating(),
            dto.content(),
            dto.hidden(),
            dto.createdAt()
        );
    }

    private ReviewManagementDetailResponse toReviewManagementDetailResponse(ReviewManagementDetailResult dto) {
        return ReviewManagementDetailResponse.from(
            dto.id(),
            dto.shopId(),
            dto.shopName(),
            dto.stationName(),
            dto.content(),
            dto.totalRating(),
            dto.tasteRating(),
            dto.amountRating(),
            dto.priceRating(),
            dto.atmosphereRating(),
            dto.kindnessRating(),
            dto.hygieneRating(),
            dto.willRevisit(),
            dto.hidden(),
            dto.memberId().value(),
            dto.memberNickname(),
            fileService.getUrlByPath(dto.memberProfileImageUrl()),
            dto.createdAt(),
            dto.imageUrls().stream().map(fileService::getUrlByPath).toList(),
            dto.tagNames()
        );
    }

    private ReviewCommentListItemResponse toReviewCommentListItemResponse(ReviewCommentListItemResult comment, List<ReviewReplyListItemResult> replies) {
        List<ReviewReplyListItemResponse> commentReplies = replies.stream()
            .filter(reply -> reply.commentId().equals(comment.id()))
            .map(this::toReviewReplyListItemResponse)
            .toList();

        return ReviewCommentListItemResponse.from(
            comment.id(),
            comment.memberId().value(),
            comment.memberNickname(),
            comment.content(),
            comment.hidden(),
            comment.createdAt(),
            commentReplies
        );
    }

    private ReviewReplyListItemResponse toReviewReplyListItemResponse(ReviewReplyListItemResult dto) {
        return ReviewReplyListItemResponse.from(
            dto.id(),
            dto.memberId().value(),
            dto.memberNickname(),
            dto.replyToMemberId() != null ? dto.replyToMemberId().value() : null,
            dto.replyToMemberNickname(),
            dto.content(),
            dto.hidden(),
            dto.createdAt()
        );
    }
}
