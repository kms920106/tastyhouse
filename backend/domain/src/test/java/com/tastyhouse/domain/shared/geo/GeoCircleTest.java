package com.tastyhouse.domain.shared.geo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.shop.service.ShopDeliveryAreaPolicy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GeoCircleTest {
    private static final GeoPoint SEOUL = GeoPoint.of(37.5, 127.0);

    @Test
    @DisplayName("모든 정점이 중심에서 요청 반경만큼 떨어져 있다(하버사인 교차 검증)")
    void approximate_allVerticesAtRequestedRadius() {
        int radiusMeters = 4000;

        GeoRing circle = GeoCircle.approximate(SEOUL, radiusMeters, ShopDeliveryAreaPolicy.CIRCLE_SEGMENTS);

        assertThat(circle.points()).allSatisfy(point ->
            assertThat(SEOUL.distanceMetersTo(point)).isCloseTo(radiusMeters, org.assertj.core.data.Offset.offset(1.0))
        );
    }

    @Test
    @DisplayName("고위도·저위도 양극단에서도 경도 보정이 적용된다")
    void approximate_appliesLongitudeCorrectionAcrossLatitudes() {
        for (double latitude : new double[] {33.0, 38.0}) {
            GeoPoint center = GeoPoint.of(latitude, 127.0);

            GeoRing circle = GeoCircle.approximate(center, 7000, ShopDeliveryAreaPolicy.CIRCLE_SEGMENTS);

            assertThat(circle.points()).allSatisfy(point ->
                assertThat(center.distanceMetersTo(point)).isCloseTo(7000, org.assertj.core.data.Offset.offset(2.0))
            );
        }
    }

    @Test
    @DisplayName("정점 수만큼 좌표를 만든다")
    void approximate_producesRequestedSegmentCount() {
        assertThat(GeoCircle.approximate(SEOUL, 1000, 72).vertexCount()).isEqualTo(72);
    }

    @Test
    @DisplayName("반경이 0 이하이거나 정점이 3개 미만이면 거부한다")
    void approximate_rejectsInvalidArguments() {
        assertThatThrownBy(() -> GeoCircle.approximate(SEOUL, 0, 72))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> GeoCircle.approximate(SEOUL, 1000, 2))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
