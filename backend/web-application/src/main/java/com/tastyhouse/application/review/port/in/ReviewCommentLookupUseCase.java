package com.tastyhouse.application.review.port.in;

public interface ReviewCommentLookupUseCase {

    Long findReviewIdOfComment(Long commentId);
}
