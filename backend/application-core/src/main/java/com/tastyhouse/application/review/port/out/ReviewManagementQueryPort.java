package com.tastyhouse.application.review.port.out;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;

public interface ReviewManagementQueryPort {

    PageResult<ReviewListItemResult> findReviews(ReviewSearchCondition condition, PageQuery pageQuery);

    Optional<ReviewManagementDetailResult> findReviewManagementDetail(Long reviewId);

    List<ReviewCommentListItemResult> findCommentsIncludingHidden(Long reviewId);

    List<ReviewReplyListItemResult> findRepliesIncludingHidden(List<Long> commentIds);
}
