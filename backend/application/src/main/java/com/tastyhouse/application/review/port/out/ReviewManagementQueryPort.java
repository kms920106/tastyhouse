package com.tastyhouse.application.review.port.out;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.review.vo.ReviewCommentId;
import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;

public interface ReviewManagementQueryPort {

    PageResult<ReviewListItemResult> findReviews(ReviewSearchCondition condition, PageQuery pageQuery);

    Optional<ReviewManagementDetailResult> findReviewManagementDetail(ReviewId reviewId);

    List<ReviewCommentListItemResult> findCommentsIncludingHidden(ReviewId reviewId);

    List<ReviewReplyListItemResult> findRepliesIncludingHidden(List<ReviewCommentId> commentIds);
}
