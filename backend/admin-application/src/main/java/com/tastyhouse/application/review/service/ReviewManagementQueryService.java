package com.tastyhouse.application.review.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.review.vo.ReviewCommentId;
import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.application.review.port.in.ReviewManagementQueryUseCase;
import com.tastyhouse.application.review.port.out.ReviewCommentListItemResult;
import com.tastyhouse.application.review.port.out.ReviewListItemResult;
import com.tastyhouse.application.review.port.out.ReviewManagementDetailResult;
import com.tastyhouse.application.review.port.out.ReviewManagementQueryPort;
import com.tastyhouse.application.review.port.out.ReviewReplyListItemResult;
import com.tastyhouse.application.review.port.out.ReviewSearchCondition;
import com.tastyhouse.application.review.port.out.ReviewTagQueryPort;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;

@Service
@Transactional(readOnly = true)
class ReviewManagementQueryService implements ReviewManagementQueryUseCase {

    private final ReviewManagementQueryPort reviewManagementQueryPort;
    private final ReviewTagQueryPort reviewTagQueryPort;

    public ReviewManagementQueryService(ReviewManagementQueryPort reviewManagementQueryPort, ReviewTagQueryPort reviewTagQueryPort) {
        this.reviewManagementQueryPort = reviewManagementQueryPort;
        this.reviewTagQueryPort = reviewTagQueryPort;
    }

    @Override
    public PageResult<ReviewListItemResult> getReviews(
        Long shopId,
        Long productId,
        Long memberId,
        Boolean hidden,
        Boolean ownerOnly,
        String content,
        Double minRating,
        Double maxRating,
        int page,
        int size
    ) {
        ReviewSearchCondition condition = ReviewSearchCondition.of(shopId, productId, memberId, hidden, ownerOnly, content, minRating, maxRating);
        return reviewManagementQueryPort.findReviews(condition, PageQuery.of(page, size));
    }

    @Override
    public ReviewManagementDetailResult getReview(Long id) {
        ReviewId reviewId = ReviewId.of(id);
        ReviewManagementDetailResult detail = reviewManagementQueryPort.findReviewManagementDetail(reviewId.value())
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.REVIEW_NOT_FOUND));

        List<Long> tagIds = reviewTagQueryPort.findTagIdsByReviewId(reviewId.value());
        if (!tagIds.isEmpty()) {
            detail = detail.withTagNames(reviewTagQueryPort.findTagNamesByIds(tagIds));
        }

        return detail;
    }

    @Override
    public List<ReviewCommentListItemResult> getComments(Long id) {
        ReviewId reviewId = ReviewId.of(id);
        return reviewManagementQueryPort.findCommentsIncludingHidden(reviewId.value());
    }

    @Override
    public List<ReviewReplyListItemResult> getReplies(List<ReviewCommentListItemResult> comments) {
        List<Long> commentIds = comments.stream()
            .map(comment -> ReviewCommentId.of(comment.id()).value())
            .toList();
        return reviewManagementQueryPort.findRepliesIncludingHidden(commentIds);
    }
}
