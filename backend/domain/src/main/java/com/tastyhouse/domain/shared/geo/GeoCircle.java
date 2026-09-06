package com.tastyhouse.domain.shared.geo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public final class GeoCircle {
    private static final int COORDINATE_SCALE = 6;

    private GeoCircle() {
    }

    public static GeoRing approximate(GeoPoint center, int radiusMeters, int segments) {
        if (radiusMeters <= 0) {
            throw new IllegalArgumentException("반경은 0보다 커야 합니다.");
        }
        if (segments < GeoRing.MIN_POINTS) {
            throw new IllegalArgumentException("원 근사 정점 수는 " + GeoRing.MIN_POINTS + "개 이상이어야 합니다.");
        }

        double centerLatitude = center.latitude().doubleValue();
        double centerLongitude = center.longitude().doubleValue();

        double latitudeDelta = Math.toDegrees((double) radiusMeters / GeoDistance.EARTH_RADIUS_METERS);
        double cosLatitude = Math.cos(Math.toRadians(centerLatitude));

        double longitudeDelta = Math.abs(cosLatitude) < 1e-12
            ? 0
            : latitudeDelta / cosLatitude;

        List<GeoPoint> points = new ArrayList<>(segments);
        for (int i = 0; i < segments; i++) {
            double angle = 2 * Math.PI * i / segments;
            double latitude = centerLatitude + latitudeDelta * Math.cos(angle);
            double longitude = centerLongitude + longitudeDelta * Math.sin(angle);

            points.add(GeoPoint.of(
                round(clamp(latitude, -90, 90)),
                round(clamp(longitude, -180, 180))
            ));
        }

        return GeoRing.of(points);
    }

    private static BigDecimal round(double value) {
        return BigDecimal.valueOf(value).setScale(COORDINATE_SCALE, RoundingMode.HALF_UP);
    }

    private static double clamp(double value, double min, double max) {
        return Math.clamp(value, min, max);
    }
}
