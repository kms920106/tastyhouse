package com.tastyhouse.domain.shared.geo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * 중심·반경으로 정의된 원을 정다각형으로 근사한다.
 *
 * <p>"반경으로 배달지역 추가" 기능이 지도에 원을 그려 보여주고, 그 원 안에 드는 행정동을 골라야 한다.
 * 원은 폴리곤이 아니므로 {@link PointInPolygon}에 바로 넣을 수 없어 정다각형으로 바꾼다 — 프론트도 이
 * 정점 배열을 그대로 받아 폴리곤 union의 재료로 쓴다(서버·클라이언트가 같은 도형을 보게 된다).
 *
 * <p><b>거리 판정에는 이 근사를 쓰지 않는다.</b> 어떤 좌표가 반경 안인지는 하버사인
 * ({@link GeoDistance})으로 직접 재는 쪽이 정확하다. 이 클래스의 산출물은 <b>표시·환산용 도형</b>이다.
 */
public final class GeoCircle {

    /** 위경도 저장 정밀도({@code DECIMAL(9,6)})와 맞춘 소수 자릿수. */
    private static final int COORDINATE_SCALE = 6;

    private GeoCircle() {
    }

    /**
     * 중심에서 반경 {@code radiusMeters}인 원을 {@code segments}각형으로 근사한다.
     *
     * <p>위도 1도의 거리는 어디서나 거의 일정하지만 경도 1도의 거리는 고위도로 갈수록 짧아지므로
     * ({@code cos φ}배), 경도 증분에 {@code 1/cos φ}를 곱해 보정한다. 이 보정을 빼면 위도 37.5°에서
     * 동서 방향이 약 21% 좁은 <b>타원</b>이 된다.
     */
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
        // 극점(cos φ ≈ 0)에서 경도 증분이 발산하는 것을 막는다. 국내 좌표에서는 도달하지 않는 분기다.
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
        return Math.min(Math.max(value, min), max);
    }
}
