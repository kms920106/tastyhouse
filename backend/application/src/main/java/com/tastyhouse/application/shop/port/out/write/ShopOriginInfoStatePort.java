package com.tastyhouse.application.shop.port.out.write;

import java.util.Optional;

public interface ShopOriginInfoStatePort {
    Optional<ShopOriginInfoState> findByShopId(Long shopId);

    ShopOriginInfoState save(ShopOriginInfoState shopOriginInfo);
}
