package com.tastyhouse.application.review.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;

/**
 * 사장님 답변 쓰기 인바운드 포트.
 */
@CeoApp
public interface ReviewOwnerReplyCommandUseCase {

    Long register(ReviewOwnerReplyCreateCommand command);

    void modify(ReviewOwnerReplyUpdateCommand command);

    void remove(ReviewOwnerReplyDeleteCommand command);
}
