package com.tastyhouse.domain.shop.service;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.shared.geo.GeoPoint;
import com.tastyhouse.domain.shared.geo.GeoPolygon;
import com.tastyhouse.domain.shared.geo.GeoRing;

public final class ShopDeliveryAreaPolicy {
    public static final int MAX_DELIVERY_RADIUS_METERS = 7000;

    public static final int MIN_DELIVERY_RADIUS_METERS = 500;

    public static final int DEFAULT_EXPOSURE_RADIUS_METERS = 4000;

    public static final int MAX_DELIVERY_AREA_COUNT = 500;

    public static final int MAX_RINGS = 20;

    public static final int MAX_VERTICES = 5000;

    public static final int CIRCLE_SEGMENTS = 72;

    public static final double COVERAGE_THRESHOLD = 0.30;

    public static final int BOUNDARY_SAMPLE_LIMIT = 200;

    private ShopDeliveryAreaPolicy() {
    }

    public static void validateShape(GeoPolygon polygon) {
        if (polygon.ringCount() > MAX_RINGS) {
            throw new BusinessException(
                ErrorCode.SHOP_DELIVERY_AREA_POLYGON_INVALID,
                ErrorCode.SHOP_DELIVERY_AREA_POLYGON_INVALID.getDefaultMessage()
                    + ": 링은 최대 " + MAX_RINGS + "개까지 가능합니다."
            );
        }
        if (polygon.vertexCount() > MAX_VERTICES) {
            throw new BusinessException(
                ErrorCode.SHOP_DELIVERY_AREA_POLYGON_INVALID,
                ErrorCode.SHOP_DELIVERY_AREA_POLYGON_INVALID.getDefaultMessage()
                    + ": 정점은 최대 " + MAX_VERTICES + "개까지 가능합니다."
            );
        }
        for (GeoRing ring : polygon.rings()) {
            if (ring.vertexCount() < GeoRing.MIN_POINTS) {
                throw new BusinessException(
                    ErrorCode.SHOP_DELIVERY_AREA_POLYGON_INVALID,
                    ErrorCode.SHOP_DELIVERY_AREA_POLYGON_INVALID.getDefaultMessage()
                        + ": 각 링은 좌표가 " + GeoRing.MIN_POINTS + "개 이상이어야 합니다."
                );
            }
        }
    }

    public static void validateWithinMaxRadius(GeoPolygon polygon, GeoPoint center) {
        double maxDistance = polygon.maxDistanceMetersFrom(center);
        if (exceedsMaxRadius(maxDistance)) {
            throw new BusinessException(ErrorCode.SHOP_DELIVERY_AREA_RADIUS_EXCEEDED);
        }
    }

    public static void validateRadius(int radiusMeters) {
        if (radiusMeters < MIN_DELIVERY_RADIUS_METERS || radiusMeters > MAX_DELIVERY_RADIUS_METERS) {
            throw new BusinessException(ErrorCode.SHOP_DELIVERY_AREA_RADIUS_EXCEEDED);
        }
    }

    public static void validateTotalCount(int totalCountAfterApply) {
        if (totalCountAfterApply > MAX_DELIVERY_AREA_COUNT) {
            throw new BusinessException(ErrorCode.SHOP_DELIVERY_AREA_COUNT_EXCEEDED);
        }
    }

    public static boolean exceedsMaxRadius(double distanceMeters) {
        return distanceMeters > MAX_DELIVERY_RADIUS_METERS + 1e-3;
    }
}
