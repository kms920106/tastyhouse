package com.tastyhouse.domain.shop.repository;

import java.util.Optional;

import com.tastyhouse.domain.shop.model.ShopDeliveryAreaPolygon;
import com.tastyhouse.domain.shop.vo.ShopId;

public interface ShopDeliveryAreaPolygonRepository {
    Optional<ShopDeliveryAreaPolygon> findByShopId(ShopId shopId);

    ShopDeliveryAreaPolygon save(ShopDeliveryAreaPolygon shopDeliveryAreaPolygon);

    void deleteByShopId(ShopId shopId);
}
