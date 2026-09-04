package com.tastyhouse.application.review.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;

/**
 * 리뷰 게시중단 요청 쓰기 인바운드 포트.
 */
@CeoApp
public interface ReviewBlindRequestOwnerCommandUseCase {

    Long request(ReviewBlindRequestCreateCommand command);

    void cancel(ReviewBlindRequestCancelCommand command);
}
