package com.tastyhouse.application.review.port.out.write;

import java.util.Optional;

import com.tastyhouse.domain.review.model.ShopReviewDisplaySetting;
import com.tastyhouse.domain.shop.vo.ShopId;

public interface ShopReviewDisplaySettingLoadPort {

    Optional<ShopReviewDisplaySetting> findByShopId(ShopId shopId);
}
