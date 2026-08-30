package com.tastyhouse.ceoapplication.review.port.in;

/**
 * 점주 리뷰 표시 설정 쓰기 인바운드 포트.
 */
public interface ShopReviewCommandUseCase {

    void changeSortType(ShopReviewSortTypeChangeCommand command);
}
