package com.tastyhouse.application.review.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;

/**
 * 점주 리뷰 표시 설정 쓰기 인바운드 포트.
 */
@CeoApp
public interface ShopReviewCommandUseCase {

    void changeSortType(ShopReviewSortTypeChangeCommand command);
}
