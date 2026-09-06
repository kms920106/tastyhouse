package com.tastyhouse.application.review.port.in;

import com.tastyhouse.application.shared.marker.AdminApp;

@AdminApp
public interface ReviewManagementCommandUseCase {

    void changeReviewHidden(ReviewHiddenChangeCommand command);

    void deleteReview(ReviewManagementDeleteCommand command);

    void changeCommentHidden(ReviewCommentHiddenChangeCommand command);

    void deleteComment(ReviewCommentDeleteCommand command);

    void changeReplyHidden(ReviewReplyHiddenChangeCommand command);

    void deleteReply(ReviewReplyDeleteCommand command);
}
