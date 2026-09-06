package com.tastyhouse.application.shop.service;

import java.util.List;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.shared.geo.GeoPoint;
import com.tastyhouse.domain.shared.geo.GeoPolygon;
import com.tastyhouse.domain.shared.geo.GeoRing;
import com.tastyhouse.application.shop.port.out.GeoPointView;
import com.tastyhouse.application.shop.port.in.GeoPointCommand;

final class ShopDeliveryAreaGeoMapper {

    private ShopDeliveryAreaGeoMapper() {
    }

    static GeoPolygon toPolygon(List<List<GeoPointCommand>> rings) {
        if (rings == null || rings.isEmpty()) {
            throw new BusinessException(ErrorCode.SHOP_DELIVERY_AREA_POLYGON_INVALID);
        }

        try {
            return GeoPolygon.of(rings.stream()
                .map(ShopDeliveryAreaGeoMapper::toRing)
                .toList());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(
                ErrorCode.SHOP_DELIVERY_AREA_POLYGON_INVALID,
                ErrorCode.SHOP_DELIVERY_AREA_POLYGON_INVALID.getDefaultMessage() + ": " + e.getMessage()
            );
        }
    }

    static List<List<GeoPointView>> toRingViews(GeoPolygon polygon) {
        return polygon.rings().stream()
            .map(ShopDeliveryAreaGeoMapper::toPointViews)
            .toList();
    }

    static List<GeoPointView> toPointViews(GeoRing ring) {
        return ring.points().stream()
            .map(ShopDeliveryAreaGeoMapper::toPointView)
            .toList();
    }

    private static GeoRing toRing(List<GeoPointCommand> points) {
        if (points == null || points.isEmpty()) {
            throw new IllegalArgumentException("링의 좌표 목록이 비어 있습니다.");
        }

        return GeoRing.of(points.stream()
            .map(point -> GeoPoint.of(point.latitude(), point.longitude()))
            .toList());
    }

    private static GeoPointView toPointView(GeoPoint point) {
        return new GeoPointView(point.latitude(), point.longitude());
    }
}
