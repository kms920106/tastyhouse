package com.tastyhouse.domain.shop.model;

import com.tastyhouse.domain.shared.geo.GeoPoint;
import com.tastyhouse.domain.shared.geo.GeoPolygon;
import com.tastyhouse.domain.shop.vo.ShopId;

public class ShopDeliveryAreaPolygon {
    private final Long id;
    private final ShopId shopId;
    private GeoPolygon polygon;
    private GeoPoint center;
    private int maxRadiusMeters;

    private ShopDeliveryAreaPolygon(Long id, ShopId shopId, GeoPolygon polygon, GeoPoint center, int maxRadiusMeters) {
        this.id = id;
        this.shopId = shopId;
        this.polygon = polygon;
        this.center = center;
        this.maxRadiusMeters = maxRadiusMeters;
    }

    public static ShopDeliveryAreaPolygon of(ShopId shopId, GeoPolygon polygon, GeoPoint center) {
        requireArguments(shopId, polygon, center);
        return new ShopDeliveryAreaPolygon(null, shopId, polygon, center, computeMaxRadiusMeters(polygon, center));
    }

    public static ShopDeliveryAreaPolygon reconstitute(
        Long id,
        ShopId shopId,
        GeoPolygon polygon,
        GeoPoint center,
        int maxRadiusMeters
    ) {
        requireArguments(shopId, polygon, center);
        return new ShopDeliveryAreaPolygon(id, shopId, polygon, center, maxRadiusMeters);
    }

    public void replace(GeoPolygon newPolygon, GeoPoint newCenter) {
        requireArguments(this.shopId, newPolygon, newCenter);
        this.polygon = newPolygon;
        this.center = newCenter;
        this.maxRadiusMeters = computeMaxRadiusMeters(newPolygon, newCenter);
    }

    private static int computeMaxRadiusMeters(GeoPolygon polygon, GeoPoint center) {
        return (int) Math.ceil(polygon.maxDistanceMetersFrom(center));
    }

    private static void requireArguments(ShopId shopId, GeoPolygon polygon, GeoPoint center) {
        if (shopId == null) {
            throw new IllegalArgumentException("배달지역 도형의 가게 식별자는 필수입니다.");
        }
        if (polygon == null) {
            throw new IllegalArgumentException("배달지역 도형은 필수입니다.");
        }
        if (center == null) {
            throw new IllegalArgumentException("배달지역 도형의 기준점은 필수입니다.");
        }
    }

    public Long getId() {
        return this.id;
    }

    public ShopId getShopId() {
        return this.shopId;
    }

    public GeoPolygon getPolygon() {
        return this.polygon;
    }

    public GeoPoint getCenter() {
        return this.center;
    }

    public int getMaxRadiusMeters() {
        return this.maxRadiusMeters;
    }

    public int getRingCount() {
        return this.polygon.ringCount();
    }

    public int getVertexCount() {
        return this.polygon.vertexCount();
    }
}
