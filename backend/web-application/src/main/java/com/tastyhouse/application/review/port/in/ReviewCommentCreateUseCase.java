package com.tastyhouse.application.review.port.in;

public interface ReviewCommentCreateUseCase {

    Long createComment(ReviewCommentCreateCommand command);
}
