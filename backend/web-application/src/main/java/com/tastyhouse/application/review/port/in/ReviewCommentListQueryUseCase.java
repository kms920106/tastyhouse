package com.tastyhouse.application.review.port.in;

import com.tastyhouse.application.review.port.out.ReviewCommentListView;

public interface ReviewCommentListQueryUseCase {

    ReviewCommentListView searchCommentsWithReplies(Long reviewId, Long viewerMemberId);
}
