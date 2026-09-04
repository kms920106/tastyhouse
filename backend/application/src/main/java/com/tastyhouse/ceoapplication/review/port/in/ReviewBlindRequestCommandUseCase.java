package com.tastyhouse.ceoapplication.review.port.in;

/**
 * 리뷰 게시중단 요청 쓰기 인바운드 포트.
 */
public interface ReviewBlindRequestCommandUseCase {

    Long request(ReviewBlindRequestCreateCommand command);

    void cancel(ReviewBlindRequestCancelCommand command);
}
