package com.tastyhouse.domain.shared.geo;

import java.util.ArrayList;
import java.util.List;

/**
 * 배달지역 도형 — 링 하나 이상으로 이루어진 폴리곤.
 *
 * <p>여러 링을 담을 수 있어 <b>떨어진 두 구역</b>(예: 하천 건너 별도 배달권)과 <b>구멍</b>(배달 제외 구역)을
 * 모두 표현한다. 어느 쪽인지는 링에 표시하지 않고 {@link PointInPolygon}의 even-odd 규칙이 위상으로
 * 판정한다 — 클래스 Javadoc 참고.
 *
 * <p>이 타입은 <b>편집·표현의 원본</b>이며 주문 배달가능 판정에 직접 참여하지 않는다. 판정은 폴리곤을
 * 환산해 얻은 행정동 집합({@code SHOP_DELIVERY_AREA})만 본다.
 */
public record GeoPolygon(
    List<GeoRing> rings
) {

    public GeoPolygon {
        if (rings == null || rings.isEmpty()) {
            throw new IllegalArgumentException("도형에는 링이 하나 이상 필요합니다.");
        }
        // List.copyOf가 null 원소에 NPE를 던지므로, 그 전에 명시적 메시지로 걸러낸다.
        // (contains(null)은 List.of로 만든 불변 리스트에서 그 자체로 NPE라 검사에 쓸 수 없다.)
        for (GeoRing ring : rings) {
            if (ring == null) {
                throw new IllegalArgumentException("도형에 빈 링이 포함될 수 없습니다.");
            }
        }
        rings = List.copyOf(rings);
    }

    public static GeoPolygon of(List<GeoRing> rings) {
        return new GeoPolygon(rings);
    }

    /** 점이 이 도형 안(경계 포함)에 있는지. */
    public boolean contains(GeoPoint point) {
        return PointInPolygon.contains(this, point);
    }

    /** 전 링을 감싸는 최소 바운딩 박스. */
    public GeoBoundingBox boundingBox() {
        return GeoBoundingBox.enclosing(allPoints());
    }

    /**
     * 기준점으로부터 가장 먼 정점까지의 거리(m).
     *
     * <p>배달지역 7km 상한은 <b>정점 하나라도</b> 넘으면 위반이므로, 최원거리 정점 하나로 판정할 수 있다.
     */
    public double maxDistanceMetersFrom(GeoPoint center) {
        double max = 0;
        for (GeoRing ring : this.rings) {
            max = Math.max(max, ring.maxDistanceMetersFrom(center));
        }
        return max;
    }

    public int ringCount() {
        return this.rings.size();
    }

    /** 전 링의 정점 수 합계. */
    public int vertexCount() {
        int total = 0;
        for (GeoRing ring : this.rings) {
            total += ring.vertexCount();
        }
        return total;
    }

    /** 전 링의 정점을 한 리스트로 펼친다. */
    public List<GeoPoint> allPoints() {
        List<GeoPoint> points = new ArrayList<>(vertexCount());
        for (GeoRing ring : this.rings) {
            points.addAll(ring.points());
        }
        return points;
    }
}
