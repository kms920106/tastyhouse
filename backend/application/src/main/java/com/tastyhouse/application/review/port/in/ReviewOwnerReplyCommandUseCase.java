package com.tastyhouse.application.review.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;

@CeoApp
public interface ReviewOwnerReplyCommandUseCase {

    Long register(ReviewOwnerReplyCreateCommand command);

    void modify(ReviewOwnerReplyUpdateCommand command);

    void remove(ReviewOwnerReplyDeleteCommand command);
}
