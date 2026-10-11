package com.tastyhouse.application.shop.port.out.write;

import java.util.Optional;

import com.tastyhouse.domain.shop.model.ShopDeliveryAreaPolygon;
import com.tastyhouse.domain.shop.vo.ShopId;

public interface ShopDeliveryAreaPolygonLoadPort {

    Optional<ShopDeliveryAreaPolygon> findByShopId(ShopId shopId);
}
