package com.tastyhouse.application.shop.store;

import java.util.Optional;

import com.tastyhouse.application.shop.port.out.write.ShopDeliveryAreaPolygonStatePort;
import com.tastyhouse.domain.shop.model.ShopDeliveryAreaPolygon;
import com.tastyhouse.domain.shop.vo.ShopId;

public class ShopDeliveryAreaPolygonStore implements ShopDeliveryAreaPolygonRepository {
    private final ShopDeliveryAreaPolygonStatePort shopDeliveryAreaPolygonStatePort;

    public ShopDeliveryAreaPolygonStore(ShopDeliveryAreaPolygonStatePort shopDeliveryAreaPolygonStatePort) {
        this.shopDeliveryAreaPolygonStatePort = shopDeliveryAreaPolygonStatePort;
    }

    @Override
    public Optional<ShopDeliveryAreaPolygon> findByShopId(ShopId shopId) {
        return shopDeliveryAreaPolygonStatePort.findByShopId(shopId.value()).map(ShopDeliveryAreaPolygonStateMapper::toDomain);
    }

    @Override
    public ShopDeliveryAreaPolygon save(ShopDeliveryAreaPolygon shopDeliveryAreaPolygon) {
        return ShopDeliveryAreaPolygonStateMapper.toDomain(shopDeliveryAreaPolygonStatePort.save(ShopDeliveryAreaPolygonStateMapper.toState(shopDeliveryAreaPolygon)));
    }

    @Override
    public void deleteByShopId(ShopId shopId) {
        shopDeliveryAreaPolygonStatePort.deleteByShopId(shopId.value());
    }
}
