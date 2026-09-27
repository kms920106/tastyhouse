package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.domain.shared.geo.GeoPoint;
import com.tastyhouse.domain.shared.geo.GeoPolygonTextCodec;
import com.tastyhouse.domain.shop.model.ShopDeliveryAreaPolygon;
import com.tastyhouse.domain.shop.vo.ShopId;

final class ShopDeliveryAreaPolygonMapper {
    private ShopDeliveryAreaPolygonMapper() {
    }

    static ShopDeliveryAreaPolygon toDomain(ShopDeliveryAreaPolygonJpaEntity entity) {
        return ShopDeliveryAreaPolygon.reconstitute(
            entity.getId(),
            entity.getShopId() == null ? null : ShopId.of(entity.getShopId()),
            GeoPolygonTextCodec.decode(entity.getRings()),
            GeoPoint.of(entity.getCenterLatitude(), entity.getCenterLongitude()),
            entity.getMaxRadiusMeters()
        );
    }

    static ShopDeliveryAreaPolygonJpaEntity toEntity(ShopDeliveryAreaPolygon shopDeliveryAreaPolygon) {
        return ShopDeliveryAreaPolygonJpaEntity.create(
            shopDeliveryAreaPolygon.getShopId() == null ? null : shopDeliveryAreaPolygon.getShopId().value(),
            GeoPolygonTextCodec.encode(shopDeliveryAreaPolygon.getPolygon()),
            shopDeliveryAreaPolygon.getCenter().latitude(),
            shopDeliveryAreaPolygon.getCenter().longitude(),
            shopDeliveryAreaPolygon.getMaxRadiusMeters(),
            shopDeliveryAreaPolygon.getRingCount(),
            shopDeliveryAreaPolygon.getVertexCount()
        );
    }

    static void applyChanges(ShopDeliveryAreaPolygonJpaEntity entity, ShopDeliveryAreaPolygon shopDeliveryAreaPolygon) {
        entity.applyChanges(
            GeoPolygonTextCodec.encode(shopDeliveryAreaPolygon.getPolygon()),
            shopDeliveryAreaPolygon.getCenter().latitude(),
            shopDeliveryAreaPolygon.getCenter().longitude(),
            shopDeliveryAreaPolygon.getMaxRadiusMeters(),
            shopDeliveryAreaPolygon.getRingCount(),
            shopDeliveryAreaPolygon.getVertexCount()
        );
    }
}
