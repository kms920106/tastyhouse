package com.tastyhouse.application.review.port.in;

public interface ReviewOwnerReplyCommandUseCase {

    Long register(ReviewOwnerReplyCreateCommand command);

    void modify(ReviewOwnerReplyUpdateCommand command);

    void remove(ReviewOwnerReplyDeleteCommand command);
}
