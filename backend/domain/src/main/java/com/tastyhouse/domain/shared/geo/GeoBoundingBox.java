package com.tastyhouse.domain.shared.geo;

import java.math.BigDecimal;
import java.util.List;

public record GeoBoundingBox(
    BigDecimal maxLatitude,
    BigDecimal maxLongitude,
    BigDecimal minLatitude,
    BigDecimal minLongitude
) {
    public GeoBoundingBox {
        if (maxLatitude == null || maxLongitude == null || minLatitude == null || minLongitude == null) {
            throw new IllegalArgumentException("바운딩 박스의 네 경계값은 모두 필수입니다.");
        }
        if (minLatitude.compareTo(maxLatitude) > 0) {
            throw new IllegalArgumentException("최소 위도가 최대 위도보다 클 수 없습니다.");
        }
        if (minLongitude.compareTo(maxLongitude) > 0) {
            throw new IllegalArgumentException("최소 경도가 최대 경도보다 클 수 없습니다.");
        }
    }

    public static GeoBoundingBox enclosing(List<GeoPoint> points) {
        if (points == null || points.isEmpty()) {
            throw new IllegalArgumentException("바운딩 박스를 계산할 좌표가 없습니다.");
        }

        BigDecimal minLat = points.getFirst().latitude();
        BigDecimal maxLat = minLat;
        BigDecimal minLng = points.getFirst().longitude();
        BigDecimal maxLng = minLng;

        for (GeoPoint point : points) {
            if (point.latitude().compareTo(minLat) < 0) {
                minLat = point.latitude();
            }
            if (point.latitude().compareTo(maxLat) > 0) {
                maxLat = point.latitude();
            }
            if (point.longitude().compareTo(minLng) < 0) {
                minLng = point.longitude();
            }
            if (point.longitude().compareTo(maxLng) > 0) {
                maxLng = point.longitude();
            }
        }

        return new GeoBoundingBox(maxLat, maxLng, minLat, minLng);
    }

    public GeoBoundingBox expand(BigDecimal degrees) {
        if (degrees == null || degrees.signum() < 0) {
            throw new IllegalArgumentException("확장 각도는 0 이상이어야 합니다.");
        }

        return new GeoBoundingBox(
            clamp(this.maxLatitude.add(degrees), GeoPoint.MIN_LATITUDE, GeoPoint.MAX_LATITUDE),
            clamp(this.maxLongitude.add(degrees), GeoPoint.MIN_LONGITUDE, GeoPoint.MAX_LONGITUDE),
            clamp(this.minLatitude.subtract(degrees), GeoPoint.MIN_LATITUDE, GeoPoint.MAX_LATITUDE),
            clamp(this.minLongitude.subtract(degrees), GeoPoint.MIN_LONGITUDE, GeoPoint.MAX_LONGITUDE)
        );
    }

    public boolean contains(GeoPoint point) {
        return point.latitude().compareTo(this.minLatitude) >= 0
            && point.latitude().compareTo(this.maxLatitude) <= 0
            && point.longitude().compareTo(this.minLongitude) >= 0
            && point.longitude().compareTo(this.maxLongitude) <= 0;
    }

    private static BigDecimal clamp(BigDecimal value, BigDecimal min, BigDecimal max) {
        if (value.compareTo(min) < 0) {
            return min;
        }
        return value.compareTo(max) > 0 ? max : value;
    }
}
