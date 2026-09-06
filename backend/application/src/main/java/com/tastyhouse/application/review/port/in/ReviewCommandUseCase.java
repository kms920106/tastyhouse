package com.tastyhouse.application.review.port.in;

import com.tastyhouse.application.shared.marker.WebApp;

@WebApp
public interface ReviewCommandUseCase {

    Long createReview(ReviewCreateCommand command);

    Long updateReview(ReviewUpdateCommand command);

    void deleteReview(ReviewDeleteCommand command);

    boolean toggleReviewLike(ReviewLikeToggleCommand command);

    Long createComment(ReviewCommentCreateCommand command);

    Long findReviewIdOfComment(Long commentId);

    Long createReply(ReviewReplyCreateCommand command);
}
