package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.domain.shared.geo.GeoPoint;
import com.tastyhouse.domain.shop.model.ShopDeliveryAreaPolygon;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.infrastructure.shared.persistence.GeoPolygonTextCodec;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class ShopDeliveryAreaPolygonMapper {
    private ShopDeliveryAreaPolygonMapper() {
    }

    static ShopDeliveryAreaPolygon toDomain(ShopDeliveryAreaPolygonJpaEntity entity) {
        return ShopDeliveryAreaPolygon.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getShopId(), ShopId::of),
            GeoPolygonTextCodec.decode(entity.getRings()),
            GeoPoint.of(entity.getCenterLatitude(), entity.getCenterLongitude()),
            entity.getMaxRadiusMeters()
        );
    }

    static ShopDeliveryAreaPolygonJpaEntity toEntity(ShopDeliveryAreaPolygon domain) {
        return ShopDeliveryAreaPolygonJpaEntity.create(
            IdMapping.raw(domain.getShopId(), ShopId::value),
            GeoPolygonTextCodec.encode(domain.getPolygon()),
            domain.getCenter().latitude(),
            domain.getCenter().longitude(),
            domain.getMaxRadiusMeters(),
            domain.getRingCount(),
            domain.getVertexCount()
        );
    }

    static void applyChanges(ShopDeliveryAreaPolygonJpaEntity entity, ShopDeliveryAreaPolygon domain) {
        entity.applyChanges(
            GeoPolygonTextCodec.encode(domain.getPolygon()),
            domain.getCenter().latitude(),
            domain.getCenter().longitude(),
            domain.getMaxRadiusMeters(),
            domain.getRingCount(),
            domain.getVertexCount()
        );
    }
}
