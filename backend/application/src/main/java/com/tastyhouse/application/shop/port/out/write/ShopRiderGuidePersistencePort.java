package com.tastyhouse.application.shop.port.out.write;

import java.util.Optional;

import com.tastyhouse.domain.shop.model.ShopRiderGuide;
import com.tastyhouse.domain.shop.model.ShopRiderGuideHistory;
import com.tastyhouse.domain.shop.vo.ShopId;

public interface ShopRiderGuidePersistencePort {

    Optional<ShopRiderGuide> findByShopId(ShopId shopId);

    ShopRiderGuide save(ShopRiderGuide riderGuide);

    ShopRiderGuideHistory saveHistory(ShopRiderGuideHistory history);
}
