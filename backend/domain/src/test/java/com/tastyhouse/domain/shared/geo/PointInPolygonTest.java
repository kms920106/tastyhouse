package com.tastyhouse.domain.shared.geo;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ray-casting 포함 판정 단위 테스트.
 *
 * <p>이 판정이 배달지역 환산의 1차 규칙이므로, 뒤집히면 <b>주문 접수 가능 범위가 통째로 달라진다.</b>
 * 그래서 정점 위·변 위처럼 구현이 흔히 틀리는 경계 케이스를 명시적으로 고정한다.
 */
class PointInPolygonTest {

    /** 경도 0~10, 위도 0~10인 정사각형. */
    private static final GeoPolygon SQUARE = polygon(ring(0, 0, 10, 0, 10, 10, 0, 10));

    @Nested
    @DisplayName("볼록 도형")
    class Convex {

        @Test
        @DisplayName("내부의 점은 포함이다")
        void contains_pointInside() {
            assertThat(SQUARE.contains(point(5, 5))).isTrue();
        }

        @Test
        @DisplayName("외부의 점은 미포함이다")
        void contains_pointOutside() {
            assertThat(SQUARE.contains(point(5, 20))).isFalse();
            assertThat(SQUARE.contains(point(-1, 5))).isFalse();
        }
    }

    @Nested
    @DisplayName("경계 위의 점")
    class OnBoundary {

        @Test
        @DisplayName("정점 위의 점은 포함으로 본다")
        void contains_pointOnVertex() {
            assertThat(SQUARE.contains(point(0, 0))).isTrue();
            assertThat(SQUARE.contains(point(10, 10))).isTrue();
        }

        @Test
        @DisplayName("변 위의 점은 포함으로 본다")
        void contains_pointOnEdge() {
            assertThat(SQUARE.contains(point(5, 0))).isTrue();
            assertThat(SQUARE.contains(point(0, 5))).isTrue();
        }

        @Test
        @DisplayName("정점을 지나는 수평 반직선이 판정을 뒤집지 않는다")
        void contains_horizontalRayThroughVertex() {
            // 위도 10에 정점이 두 개 있어, 반개구간 규칙이 없으면 교차가 두 번 세어져 판정이 뒤집힌다.
            assertThat(SQUARE.contains(point(5, 10))).isTrue();
            assertThat(SQUARE.contains(point(20, 10))).isFalse();
        }
    }

    @Nested
    @DisplayName("오목 도형")
    class Concave {

        /** ㄷ 자 모양 — 가운데 홈이 파여 있다. */
        private final GeoPolygon uShape = polygon(ring(
            0, 0, 10, 0, 10, 10, 7, 10, 7, 3, 3, 3, 3, 10, 0, 10
        ));

        @Test
        @DisplayName("홈 안쪽(도형 밖)의 점은 미포함이다")
        void contains_pointInNotch() {
            assertThat(uShape.contains(point(5, 7))).isFalse();
        }

        @Test
        @DisplayName("두 기둥 안의 점은 포함이다")
        void contains_pointInPillars() {
            assertThat(uShape.contains(point(1, 7))).isTrue();
            assertThat(uShape.contains(point(9, 7))).isTrue();
        }
    }

    @Nested
    @DisplayName("여러 링")
    class MultipleRings {

        @Test
        @DisplayName("바깥 링 안의 두 번째 링은 구멍이 된다(even-odd)")
        void contains_innerRingBecomesHole() {
            GeoPolygon withHole = polygon(
                ring(0, 0, 10, 0, 10, 10, 0, 10),
                ring(4, 4, 6, 4, 6, 6, 4, 6)
            );

            assertThat(withHole.contains(point(5, 5))).isFalse();  // 구멍 안
            assertThat(withHole.contains(point(1, 1))).isTrue();   // 구멍 밖·바깥 링 안
        }

        @Test
        @DisplayName("떨어진 두 링은 각각 독립 영역이다")
        void contains_disjointRings() {
            GeoPolygon disjoint = polygon(
                ring(0, 0, 2, 0, 2, 2, 0, 2),
                ring(10, 10, 12, 10, 12, 12, 10, 12)
            );

            assertThat(disjoint.contains(point(1, 1))).isTrue();
            assertThat(disjoint.contains(point(11, 11))).isTrue();
            assertThat(disjoint.contains(point(5, 5))).isFalse();
        }
    }

    @Nested
    @DisplayName("입력 정규화")
    class Normalization {

        @Test
        @DisplayName("명시적으로 폐합된 입력도 같은 도형으로 취급한다")
        void contains_explicitlyClosedRing() {
            GeoPolygon closed = polygon(ring(0, 0, 10, 0, 10, 10, 0, 10, 0, 0));

            assertThat(closed.rings().getFirst().vertexCount()).isEqualTo(4);
            assertThat(closed.contains(point(5, 5))).isTrue();
        }

        @Test
        @DisplayName("연속 중복점은 제거된다")
        void ring_removesConsecutiveDuplicates() {
            GeoRing ring = ring(0, 0, 0, 0, 10, 0, 10, 10, 10, 10, 0, 10);

            assertThat(ring.vertexCount()).isEqualTo(4);
        }
    }

    @Nested
    @DisplayName("소수 6자리 경계값")
    class Precision {

        @Test
        @DisplayName("저장 정밀도(1e-6도) 차이는 서로 다른 점으로 구분된다")
        void contains_distinguishesSixthDecimal() {
            GeoPolygon tiny = polygon(GeoRing.of(List.of(
                GeoPoint.of(new BigDecimal("37.500000"), new BigDecimal("127.000000")),
                GeoPoint.of(new BigDecimal("37.500000"), new BigDecimal("127.000010")),
                GeoPoint.of(new BigDecimal("37.500010"), new BigDecimal("127.000010")),
                GeoPoint.of(new BigDecimal("37.500010"), new BigDecimal("127.000000"))
            )));

            assertThat(tiny.contains(GeoPoint.of(new BigDecimal("37.500005"), new BigDecimal("127.000005")))).isTrue();
            assertThat(tiny.contains(GeoPoint.of(new BigDecimal("37.500020"), new BigDecimal("127.000005")))).isFalse();
        }
    }

    /** {@code lng, lat} 쌍을 나열해 링을 만든다(저장 형식과 같은 순서라 테스트가 읽기 쉽다). */
    private static GeoRing ring(double... lngLatPairs) {
        List<GeoPoint> points = new java.util.ArrayList<>();
        for (int i = 0; i < lngLatPairs.length; i += 2) {
            points.add(GeoPoint.of(lngLatPairs[i + 1], lngLatPairs[i]));
        }
        return GeoRing.of(points);
    }

    private static GeoPolygon polygon(GeoRing... rings) {
        return GeoPolygon.of(List.of(rings));
    }

    private static GeoPoint point(double longitude, double latitude) {
        return GeoPoint.of(latitude, longitude);
    }
}
