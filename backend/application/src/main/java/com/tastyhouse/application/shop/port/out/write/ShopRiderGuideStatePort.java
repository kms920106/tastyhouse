package com.tastyhouse.application.shop.port.out.write;

import java.util.Optional;

public interface ShopRiderGuideStatePort {
    Optional<ShopRiderGuideState> findByShopId(Long shopId);

    ShopRiderGuideState save(ShopRiderGuideState riderGuide);

    ShopRiderGuideHistoryState saveHistory(ShopRiderGuideHistoryState history);
}
