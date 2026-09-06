package com.tastyhouse.application.review.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;

@CeoApp
public interface ShopReviewCommandUseCase {

    void changeSortType(ShopReviewSortTypeChangeCommand command);
}
