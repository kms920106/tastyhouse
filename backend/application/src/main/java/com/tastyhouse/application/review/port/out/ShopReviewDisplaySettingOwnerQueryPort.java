package com.tastyhouse.application.review.port.out;

import java.util.Optional;

import com.tastyhouse.domain.review.model.ReviewSortType;

public interface ShopReviewDisplaySettingOwnerQueryPort {

    Optional<ReviewSortType> findSortTypeByShopId(Long shopId);

    Optional<ShopReviewSortTypeResult> findSortTypeSettingByShopId(Long shopId);
}
