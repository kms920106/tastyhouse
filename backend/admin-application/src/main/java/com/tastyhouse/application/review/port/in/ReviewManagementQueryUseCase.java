package com.tastyhouse.application.review.port.in;

import java.util.List;

import com.tastyhouse.application.review.port.out.ReviewCommentListItemResult;
import com.tastyhouse.application.review.port.out.ReviewListItemResult;
import com.tastyhouse.application.review.port.out.ReviewManagementDetailResult;
import com.tastyhouse.application.review.port.out.ReviewReplyListItemResult;
import com.tastyhouse.application.shared.port.out.page.PageResult;

public interface ReviewManagementQueryUseCase {

    PageResult<ReviewListItemResult> getReviews(
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
    );

    ReviewManagementDetailResult getReview(Long id);

    List<ReviewCommentListItemResult> getComments(Long id);

    List<ReviewReplyListItemResult> getReplies(List<ReviewCommentListItemResult> comments);
}
