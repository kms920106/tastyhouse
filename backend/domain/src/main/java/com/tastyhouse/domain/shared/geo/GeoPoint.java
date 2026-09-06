package com.tastyhouse.domain.shared.geo;

import java.math.BigDecimal;

public record GeoPoint(
    BigDecimal latitude,
    BigDecimal longitude
) {
    public static final BigDecimal MIN_LATITUDE = BigDecimal.valueOf(-90);

    public static final BigDecimal MAX_LATITUDE = BigDecimal.valueOf(90);

    public static final BigDecimal MIN_LONGITUDE = BigDecimal.valueOf(-180);

    public static final BigDecimal MAX_LONGITUDE = BigDecimal.valueOf(180);

    public GeoPoint {
        if (latitude == null || longitude == null) {
            throw new IllegalArgumentException("좌표의 위도·경도는 필수입니다.");
        }
        if (latitude.compareTo(MIN_LATITUDE) < 0 || latitude.compareTo(MAX_LATITUDE) > 0) {
            throw new IllegalArgumentException("위도는 -90 이상 90 이하여야 합니다: " + latitude);
        }
        if (longitude.compareTo(MIN_LONGITUDE) < 0 || longitude.compareTo(MAX_LONGITUDE) > 0) {
            throw new IllegalArgumentException("경도는 -180 이상 180 이하여야 합니다: " + longitude);
        }
    }

    public static GeoPoint of(BigDecimal latitude, BigDecimal longitude) {
        return new GeoPoint(latitude, longitude);
    }

    public static GeoPoint of(double latitude, double longitude) {
        return new GeoPoint(BigDecimal.valueOf(latitude), BigDecimal.valueOf(longitude));
    }

    public double distanceMetersTo(GeoPoint other) {
        return GeoDistance.distanceMeters(this.latitude, this.longitude, other.latitude, other.longitude);
    }

    public boolean isSameLocation(GeoPoint other) {
        return other != null
            && this.latitude.compareTo(other.latitude) == 0
            && this.longitude.compareTo(other.longitude) == 0;
    }
}
