package com.tastyhouse.domain.shop.repository;

import java.util.Optional;

import com.tastyhouse.domain.shop.model.ShopRiderGuide;
import com.tastyhouse.domain.shop.model.ShopRiderGuideHistory;
import com.tastyhouse.domain.shop.vo.ShopId;

public interface ShopRiderGuideRepository {
    Optional<ShopRiderGuide> findByShopId(ShopId shopId);

    ShopRiderGuide save(ShopRiderGuide riderGuide);

    ShopRiderGuideHistory saveHistory(ShopRiderGuideHistory history);
}
