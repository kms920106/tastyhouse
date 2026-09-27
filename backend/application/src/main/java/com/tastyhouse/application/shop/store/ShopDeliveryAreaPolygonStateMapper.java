package com.tastyhouse.application.shop.store;

import com.tastyhouse.domain.shared.geo.GeoPoint;
import com.tastyhouse.domain.shared.geo.GeoPolygonTextCodec;
import com.tastyhouse.domain.shop.model.ShopDeliveryAreaPolygon;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.shop.port.out.write.ShopDeliveryAreaPolygonCenterSnapshot;
import com.tastyhouse.application.shop.port.out.write.ShopDeliveryAreaPolygonShapeSnapshot;
import com.tastyhouse.application.shop.port.out.write.ShopDeliveryAreaPolygonState;

final class ShopDeliveryAreaPolygonStateMapper {
    private ShopDeliveryAreaPolygonStateMapper() {
    }

    static ShopDeliveryAreaPolygon toDomain(ShopDeliveryAreaPolygonState state) {
        return ShopDeliveryAreaPolygon.reconstitute(
            state.id(),
            state.shopId() == null ? null : ShopId.of(state.shopId()),
            GeoPolygonTextCodec.decode(state.polygon().encodedRings()),
            GeoPoint.of(state.center().latitude(), state.center().longitude()),
            state.maxRadiusMeters()
        );
    }

    static ShopDeliveryAreaPolygonState toState(ShopDeliveryAreaPolygon shopDeliveryAreaPolygon) {
        return new ShopDeliveryAreaPolygonState(
            shopDeliveryAreaPolygon.getId(),
            shopDeliveryAreaPolygon.getShopId() == null ? null : shopDeliveryAreaPolygon.getShopId().value(),
            new ShopDeliveryAreaPolygonShapeSnapshot(
                GeoPolygonTextCodec.encode(shopDeliveryAreaPolygon.getPolygon()),
                shopDeliveryAreaPolygon.getRingCount(),
                shopDeliveryAreaPolygon.getVertexCount()
            ),
            new ShopDeliveryAreaPolygonCenterSnapshot(
                shopDeliveryAreaPolygon.getCenter().latitude(),
                shopDeliveryAreaPolygon.getCenter().longitude()
            ),
            shopDeliveryAreaPolygon.getMaxRadiusMeters()
        );
    }
}
