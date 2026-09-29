package com.tastyhouse.application.review.port.out.write;

import java.util.Optional;

import com.tastyhouse.domain.review.model.ShopReviewDisplaySetting;
import com.tastyhouse.domain.shop.vo.ShopId;

public interface ShopReviewDisplaySettingPersistencePort {
    Optional<ShopReviewDisplaySetting> findByShopId(ShopId shopId);

    ShopReviewDisplaySetting save(ShopReviewDisplaySetting shopReviewDisplaySetting);
}
