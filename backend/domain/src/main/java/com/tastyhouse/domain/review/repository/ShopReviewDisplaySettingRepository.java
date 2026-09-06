package com.tastyhouse.domain.review.repository;

import java.util.Optional;

import com.tastyhouse.domain.review.model.ShopReviewDisplaySetting;
import com.tastyhouse.domain.shop.vo.ShopId;

public interface ShopReviewDisplaySettingRepository {
    Optional<ShopReviewDisplaySetting> findByShopId(ShopId shopId);

    ShopReviewDisplaySetting save(ShopReviewDisplaySetting shopReviewDisplaySetting);
}
