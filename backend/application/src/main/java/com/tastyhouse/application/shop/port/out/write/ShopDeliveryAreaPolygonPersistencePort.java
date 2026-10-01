package com.tastyhouse.application.shop.port.out.write;

import java.util.Optional;

import com.tastyhouse.domain.shop.model.ShopDeliveryAreaPolygon;
import com.tastyhouse.domain.shop.vo.ShopId;

public interface ShopDeliveryAreaPolygonPersistencePort {

    Optional<ShopDeliveryAreaPolygon> findByShopId(ShopId shopId);

    ShopDeliveryAreaPolygon save(ShopDeliveryAreaPolygon shopDeliveryAreaPolygon);

    void deleteByShopId(ShopId shopId);
}
