package com.tastyhouse.application.review.port.out;

import java.util.List;

public record ReviewCommentListView(
    List<CommentWithReplies> comments,
    int totalCount
) {

    public record CommentWithReplies(
        ReviewCommentItemResult comment,
        List<ReviewReplyItemResult> replies
    ) {
    }
}
