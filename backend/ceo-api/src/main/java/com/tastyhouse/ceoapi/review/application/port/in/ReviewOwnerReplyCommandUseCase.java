package com.tastyhouse.ceoapi.review.application.port.in;

/**
 * 사장님 답변 쓰기 인바운드 포트.
 */
public interface ReviewOwnerReplyCommandUseCase {

    Long register(ReviewOwnerReplyCreateCommand command);

    void modify(ReviewOwnerReplyUpdateCommand command);

    void remove(ReviewOwnerReplyDeleteCommand command);
}
