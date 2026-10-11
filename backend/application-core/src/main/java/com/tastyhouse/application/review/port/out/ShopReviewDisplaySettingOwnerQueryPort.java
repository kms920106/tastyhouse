package com.tastyhouse.application.review.port.out;

import java.util.Optional;

public interface ShopReviewDisplaySettingOwnerQueryPort {

    Optional<String> findSortTypeByShopId(Long shopId);

    Optional<ShopReviewSortTypeResult> findSortTypeSettingByShopId(Long shopId);
}
