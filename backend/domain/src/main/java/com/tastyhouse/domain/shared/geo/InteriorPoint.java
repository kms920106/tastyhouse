package com.tastyhouse.domain.shared.geo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * 폴리곤 <b>내부가 보장되는</b> 대표점을 구한다.
 *
 * <p><b>centroid(무게중심)를 쓰지 않는 이유</b>: 오목하거나 초승달 모양인 도형은 무게중심이 도형 밖에
 * 떨어진다. 실제 이 프로젝트의 행정동 경계 원천 3,558건을 검사했을 때 <b>47건</b>의 centroid가 자기
 * 경계 밖이었다. 대표점이 경계 밖이면 그 동은 좌표 기준 포함 판정이 뒤집혀, 배달지역에 들어와야 할
 * 동이 빠지거나 그 반대가 된다.
 *
 * <p><b>알고리즘</b>(PostGIS {@code ST_PointOnSurface}·JTS {@code InteriorPoint}와 같은 계열):
 * <ol>
 *   <li>도형의 위도 범위 한가운데에 수평 스캔라인을 긋는다</li>
 *   <li>모든 변과의 교차점 경도를 모은다 — 구멍(hole) 링의 변도 포함해야 내부·외부가 올바로 토글된다</li>
 *   <li>경도로 정렬해 짝을 지으면 각 쌍이 도형 내부 구간이 된다(even-odd 규칙)</li>
 *   <li><b>가장 넓은</b> 구간의 중점을 대표점으로 삼는다 — 가장 좁은 목을 피해 경계에서 멀어진다</li>
 * </ol>
 *
 * <p>스캔라인이 꼭짓점을 정확히 지나면 같은 교차점이 두 번 잡혀 짝이 어긋날 수 있는데, 위 2단계의
 * 부등호를 {@code (y1 > y) != (y2 > y)}로 두어 <b>한쪽 끝점만</b> 세도록 해 이 경우를 배제한다(ray
 * casting의 표준 처리).
 */
public final class InteriorPoint {

    /** 위경도 저장 정밀도({@code DECIMAL(9,6)})와 맞춘 소수 자릿수. */
    private static final int COORDINATE_SCALE = 6;

    private InteriorPoint() {
    }

    /**
     * 링 목록(첫 링이 외곽, 나머지는 구멍)에서 내부 대표점을 구한다.
     *
     * @return 대표점. 링이 비었거나 내부 구간을 찾지 못하면 {@code null}
     */
    public static GeoPoint of(List<GeoRing> rings) {
        if (rings == null || rings.isEmpty()) {
            return null;
        }

        double scanLatitude = midLatitude(rings.getFirst());
        List<Double> crossings = crossingLongitudes(rings, scanLatitude);
        if (crossings.size() < 2) {
            return null;
        }

        crossings.sort(null);

        double widest = -1;
        double bestLongitude = 0;
        for (int i = 0; i + 1 < crossings.size(); i += 2) {
            double width = crossings.get(i + 1) - crossings.get(i);
            if (width > widest) {
                widest = width;
                bestLongitude = (crossings.get(i) + crossings.get(i + 1)) / 2;
            }
        }

        if (widest < 0) {
            return null;
        }
        return GeoPoint.of(scaled(scanLatitude), scaled(bestLongitude));
    }

    private static double midLatitude(GeoRing outerRing) {
        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;
        for (GeoPoint point : outerRing.points()) {
            double latitude = point.latitude().doubleValue();
            min = Math.min(min, latitude);
            max = Math.max(max, latitude);
        }
        return (min + max) / 2;
    }

    /** 스캔라인과 모든 변(구멍 링 포함)의 교차점 경도. */
    private static List<Double> crossingLongitudes(List<GeoRing> rings, double scanLatitude) {
        List<Double> crossings = new ArrayList<>();
        for (GeoRing ring : rings) {
            List<GeoPoint> points = ring.points();
            int size = points.size();
            for (int i = 0; i < size; i++) {
                // 링은 암묵 폐합이므로 마지막 점의 다음은 첫 점이다.
                GeoPoint from = points.get(i);
                GeoPoint to = points.get((i + 1) % size);

                double fromLatitude = from.latitude().doubleValue();
                double toLatitude = to.latitude().doubleValue();
                if ((fromLatitude > scanLatitude) == (toLatitude > scanLatitude)) {
                    continue;
                }

                double fromLongitude = from.longitude().doubleValue();
                double toLongitude = to.longitude().doubleValue();
                double ratio = (scanLatitude - fromLatitude) / (toLatitude - fromLatitude);
                crossings.add(fromLongitude + ratio * (toLongitude - fromLongitude));
            }
        }
        return crossings;
    }

    private static BigDecimal scaled(double value) {
        return BigDecimal.valueOf(value).setScale(COORDINATE_SCALE, RoundingMode.HALF_UP);
    }
}
