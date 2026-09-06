package com.tastyhouse.domain.shared.geo;

import java.math.BigDecimal;
import java.util.List;

/**
 * 위경도 바운딩 박스(경계 사각형).
 *
 * <p>폴리곤 환산에서 <b>후보 행정동을 좁히는 프리필터</b>로 쓴다 — 전국 3,600여 개 행정동을 전부
 * ray-casting 하지 않고, 인덱스({@code idx_admin_dong_center})가 걸린 좌표 범위 질의로 수십~수백 건까지
 * 줄인 뒤 정밀 판정에 넘긴다.
 *
 * <p><b>컴포넌트 선언 순서는 알파벳순</b>({@code maxLatitude} → {@code maxLongitude} → {@code minLatitude}
 * → {@code minLongitude})이다. 네 컴포넌트가 전부 같은 타입이라 순서가 어긋나면 값이 조용히 뒤바뀐다.
 */
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

    /** 점들을 모두 감싸는 최소 바운딩 박스. */
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

    /**
     * 네 방향으로 같은 각도만큼 넓힌 박스를 반환한다.
     *
     * <p>행정동 <b>대표점</b>은 동의 중심 부근이라, 폴리곤 경계에 걸친 동은 대표점이 폴리곤 bbox 밖에
     * 있을 수 있다. 프리필터에서 그런 동이 탈락하지 않도록 동 반지름만큼 여유를 준다.
     *
     * <p>확장 후 위경도 유효 범위를 벗어나면 범위 끝으로 잘라낸다(clamp) — 극지방·날짜변경선 근처에서
     * {@link GeoPoint}의 범위 검증에 걸리지 않게 하기 위한 것이며, 국내 좌표에서는 발생하지 않는다.
     */
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

    /** 점이 박스 안(경계 포함)에 있는지. */
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
