package com.tastyhouse.application.review.port.in;

import java.util.List;

import com.tastyhouse.application.review.port.out.ReviewCommentListItemResult;
import com.tastyhouse.application.review.port.out.ReviewReplyListItemResult;

public interface ReviewReplyManagementListQueryUseCase {

    List<ReviewReplyListItemResult> getReplies(List<ReviewCommentListItemResult> comments);
}
