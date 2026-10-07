package com.tastyhouse.application.review.port.in;

import com.tastyhouse.application.review.port.out.ShopReviewSortTypeView;

public interface ShopReviewSortTypeQueryUseCase {

    ShopReviewSortTypeView getSortType(Long ceoId, Long shopId);
}
