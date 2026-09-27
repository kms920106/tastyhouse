package com.tastyhouse.application.review.port.out.write;

import java.util.Optional;

public interface ShopReviewDisplaySettingStatePort {
    Optional<ShopReviewDisplaySettingState> findByShopId(Long shopId);

    ShopReviewDisplaySettingState save(ShopReviewDisplaySettingState state);
}
