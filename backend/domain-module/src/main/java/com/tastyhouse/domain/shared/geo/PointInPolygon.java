package com.tastyhouse.domain.shared.geo;

import java.util.List;

/**
 * 점이 폴리곤 내부에 있는지 판정하는 순수 ray-casting(even-odd) 구현.
 *
 * <p><b>even-odd 규칙을 쓰는 이유</b>: 링을 "외곽/구멍"으로 구분해 선언하지 않아도, 어떤 점에서 그은
 * 반직선이 <b>전체 링</b>과 교차한 횟수가 홀수면 내부로 본다. 이러면 외곽 링 안에 든 두 번째 링이
 * 자동으로 구멍(hole)이 되어, 점주가 그린 도형의 링 역할을 서버가 추론할 필요가 없다.
 *
 * <p><b>경계 위의 점은 내부로 판정한다.</b> 배달지역은 "이 동이 열리는가"를 정하는 것이라, 대표점이
 * 경계선에 정확히 얹혔을 때 닫히는 쪽으로 기우는 것보다 열리는 쪽이 점주 의도에 가깝다. 또한 경계를
 * 명시적으로 처리하지 않으면 부동소수 오차에 따라 같은 입력이 실행마다 뒤집힐 수 있다.
 *
 * <p>좌표를 평면(경도=x, 위도=y)으로 취급한다. 지구 곡률을 무시하는 근사지만, 판정 대상이 반경 7km
 * 이내의 도형이라 왜곡이 판정을 뒤집을 수준이 아니다 — 거리 계산만은 곡률을 반영한 하버사인
 * ({@link GeoDistance})을 쓴다.
 */
public final class PointInPolygon {

    private PointInPolygon() {
    }

    /**
     * 점이 폴리곤(모든 링을 even-odd로 합성한 영역) 안에 있는지 판정한다. 경계 위의 점은 {@code true}.
     */
    public static boolean contains(GeoPolygon polygon, GeoPoint point) {
        boolean inside = false;
        for (GeoRing ring : polygon.rings()) {
            if (isOnRingBoundary(ring, point)) {
                return true;
            }
            if (crossesOddTimes(ring, point)) {
                inside = !inside;
            }
        }
        return inside;
    }

    /**
     * 점에서 동쪽(+경도)으로 그은 반직선이 링의 변과 홀수 번 교차하는지.
     *
     * <p>변의 위쪽 끝점만 교차로 세는 반개구간 규칙({@code y1 > y} XOR {@code y2 > y})을 쓴다 — 정점을
     * 정확히 지나는 반직선이 인접한 두 변에서 두 번 세어져 판정이 뒤집히는 고전적 버그를 막는다.
     */
    private static boolean crossesOddTimes(GeoRing ring, GeoPoint point) {
        List<GeoPoint> points = ring.points();
        double x = point.longitude().doubleValue();
        double y = point.latitude().doubleValue();

        boolean odd = false;
        for (int i = 0, j = points.size() - 1; i < points.size(); j = i++) {
            double xi = points.get(i).longitude().doubleValue();
            double yi = points.get(i).latitude().doubleValue();
            double xj = points.get(j).longitude().doubleValue();
            double yj = points.get(j).latitude().doubleValue();

            if ((yi > y) != (yj > y) && x < (xj - xi) * (y - yi) / (yj - yi) + xi) {
                odd = !odd;
            }
        }
        return odd;
    }

    /** 점이 링의 변 위(정점 포함)에 있는지. */
    private static boolean isOnRingBoundary(GeoRing ring, GeoPoint point) {
        List<GeoPoint> points = ring.points();
        for (int i = 0, j = points.size() - 1; i < points.size(); j = i++) {
            if (isOnSegment(points.get(j), points.get(i), point)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 점이 선분 {@code start—end} 위에 있는지. 외적이 0(일직선)이고 점이 두 끝점의 사각 범위 안이면 참이다.
     *
     * <p>{@code EPSILON}은 저장 정밀도({@code DECIMAL(9,6)}, ≈11cm)보다 작은 값이라, 저장 가능한 서로 다른
     * 두 좌표가 이 오차로 같다고 판정되지 않는다.
     */
    private static boolean isOnSegment(GeoPoint start, GeoPoint end, GeoPoint point) {
        double x = point.longitude().doubleValue();
        double y = point.latitude().doubleValue();
        double x1 = start.longitude().doubleValue();
        double y1 = start.latitude().doubleValue();
        double x2 = end.longitude().doubleValue();
        double y2 = end.latitude().doubleValue();

        double crossProduct = (x - x1) * (y2 - y1) - (y - y1) * (x2 - x1);
        if (Math.abs(crossProduct) > EPSILON) {
            return false;
        }

        return x >= Math.min(x1, x2) - EPSILON && x <= Math.max(x1, x2) + EPSILON
            && y >= Math.min(y1, y2) - EPSILON && y <= Math.max(y1, y2) + EPSILON;
    }

    /** 부동소수 비교 허용 오차(도). 저장 정밀도(1e-6도)보다 두 자릿수 작다. */
    private static final double EPSILON = 1e-9;
}
