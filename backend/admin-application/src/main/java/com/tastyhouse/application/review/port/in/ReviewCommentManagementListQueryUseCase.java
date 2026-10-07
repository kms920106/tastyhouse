package com.tastyhouse.application.review.port.in;

import java.util.List;

import com.tastyhouse.application.review.port.out.ReviewCommentListItemResult;

public interface ReviewCommentManagementListQueryUseCase {

    List<ReviewCommentListItemResult> getComments(Long id);
}
