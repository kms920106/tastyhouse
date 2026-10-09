package com.tastyhouse.application.shop.port.out.write;

import com.tastyhouse.domain.shop.model.ShopDeliveryAreaPolygon;
import com.tastyhouse.domain.shop.vo.ShopId;

public interface ShopDeliveryAreaPolygonSavePort {

    ShopDeliveryAreaPolygon save(ShopDeliveryAreaPolygon shopDeliveryAreaPolygon);

    void deleteByShopId(ShopId shopId);
}
