package com.tastyhouse.infrastructure.shop.persistence;

import com.tastyhouse.application.shop.port.out.write.ShopDeliveryAreaPolygonCenterSnapshot;
import com.tastyhouse.application.shop.port.out.write.ShopDeliveryAreaPolygonShapeSnapshot;
import com.tastyhouse.application.shop.port.out.write.ShopDeliveryAreaPolygonState;

final class ShopDeliveryAreaPolygonMapper {
    private ShopDeliveryAreaPolygonMapper() {
    }

    static ShopDeliveryAreaPolygonState toState(ShopDeliveryAreaPolygonJpaEntity entity) {
        return new ShopDeliveryAreaPolygonState(
            entity.getId(),
            entity.getShopId(),
            new ShopDeliveryAreaPolygonShapeSnapshot(entity.getRings(), entity.getRingCount(), entity.getVertexCount()),
            new ShopDeliveryAreaPolygonCenterSnapshot(entity.getCenterLatitude(), entity.getCenterLongitude()),
            entity.getMaxRadiusMeters()
        );
    }

    static ShopDeliveryAreaPolygonJpaEntity toEntity(ShopDeliveryAreaPolygonState state) {
        return ShopDeliveryAreaPolygonJpaEntity.create(
            state.shopId(),
            state.polygon().encodedRings(),
            state.center().latitude(),
            state.center().longitude(),
            state.maxRadiusMeters(),
            state.polygon().ringCount(),
            state.polygon().vertexCount()
        );
    }

    static void applyChanges(ShopDeliveryAreaPolygonJpaEntity entity, ShopDeliveryAreaPolygonState state) {
        entity.applyChanges(
            state.polygon().encodedRings(),
            state.center().latitude(),
            state.center().longitude(),
            state.maxRadiusMeters(),
            state.polygon().ringCount(),
            state.polygon().vertexCount()
        );
    }
}
