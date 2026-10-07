package com.tastyhouse.application.shop.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shared.geo.GeoPoint;
import com.tastyhouse.domain.shared.geo.GeoPolygon;
import com.tastyhouse.domain.shared.geo.GeoPolygonTextCodec;
import com.tastyhouse.domain.shared.geo.GeoRing;
import com.tastyhouse.domain.shop.model.ShopDeliveryAreaPolicy;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shop.port.in.ShopDeliveryAreaPolygonDetailQueryUseCase;
import com.tastyhouse.application.shop.port.out.GeoPointView;
import com.tastyhouse.application.shop.port.out.ShopDeliveryAreaPolygonResult;
import com.tastyhouse.application.shop.port.out.ShopDeliveryAreaPolygonViewResult;
import com.tastyhouse.application.shop.port.out.ShopDeliveryAreaQueryPort;
import com.tastyhouse.application.shop.port.out.ShopLocationResult;

@Service
@Transactional(readOnly = true)
class ShopDeliveryAreaPolygonDetailQueryService implements ShopDeliveryAreaPolygonDetailQueryUseCase {

    private final ShopDeliveryAreaQueryPort shopDeliveryAreaQueryPort;

    public ShopDeliveryAreaPolygonDetailQueryService(ShopDeliveryAreaQueryPort shopDeliveryAreaQueryPort) {
        this.shopDeliveryAreaQueryPort = shopDeliveryAreaQueryPort;
    }

    @Override
    public ShopDeliveryAreaPolygonViewResult getPolygon(Long ceoId, Long shopId) {
        ShopLocationResult shopLocation =
            ShopDeliveryAreaGeoMapper.requireCoordinates(shopDeliveryAreaQueryPort.findShopLocation(ceoId, shopId)
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.SHOP_ACCESS_DENIED)));
        ShopDeliveryAreaPolygonResult stored = shopDeliveryAreaQueryPort.findPolygon(shopId).orElse(null);

        if (stored == null) {
            return new ShopDeliveryAreaPolygonViewResult(
                false, null, null, null,
                shopLocation.latitude(), shopLocation.longitude(),
                0, null,
                ShopDeliveryAreaPolicy.MAX_DELIVERY_RADIUS_METERS,
                ShopDeliveryAreaPolicy.DEFAULT_EXPOSURE_RADIUS_METERS,
                null, null, 0, null
            );
        }

        List<GeoRing> storedRings = GeoPolygonTextCodec.decodeRings(stored.rings());
        List<List<GeoPointView>> ringViews = storedRings.isEmpty()
            ? List.of()
            : ShopDeliveryAreaGeoMapper.toRingViews(GeoPolygon.of(storedRings));
        GeoPoint storedCenter = GeoPoint.of(stored.centerLatitude(), stored.centerLongitude());
        GeoPoint currentLocation = GeoPoint.of(shopLocation.latitude(), shopLocation.longitude());

        return new ShopDeliveryAreaPolygonViewResult(
            true,
            ringViews,
            stored.centerLatitude(),
            stored.centerLongitude(),
            shopLocation.latitude(),
            shopLocation.longitude(),
            (int) Math.round(storedCenter.distanceMetersTo(currentLocation)),
            stored.maxRadiusMeters(),
            ShopDeliveryAreaPolicy.MAX_DELIVERY_RADIUS_METERS,
            ShopDeliveryAreaPolicy.DEFAULT_EXPOSURE_RADIUS_METERS,
            stored.ringCount(),
            stored.vertexCount(),
            shopDeliveryAreaQueryPort.findAdminDongIdsBySource(shopId, "POLYGON").size(),
            stored.updatedAt()
        );
    }
}
