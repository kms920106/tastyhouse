package com.tastyhouse.application.shop.port.out.write;

import java.util.Optional;

public interface ShopDeliveryAreaPolygonStatePort {
    Optional<ShopDeliveryAreaPolygonState> findByShopId(Long shopId);

    ShopDeliveryAreaPolygonState save(ShopDeliveryAreaPolygonState shopDeliveryAreaPolygon);

    void deleteByShopId(Long shopId);
}
