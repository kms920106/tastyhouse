package com.tastyhouse.application.review.port.out;

import java.util.Optional;

public interface ShopReviewDisplaySettingQueryPort {

    Optional<String> findSortTypeByShopId(Long shopId);
}
