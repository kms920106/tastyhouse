package com.tastyhouse.domain.shop.service;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.shared.geo.GeoCircle;
import com.tastyhouse.domain.shared.geo.GeoDistance;
import com.tastyhouse.domain.shared.geo.GeoPoint;
import com.tastyhouse.domain.shared.geo.GeoPolygon;
import com.tastyhouse.domain.shared.geo.GeoRing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShopDeliveryAreaPolicyTest {
    private static final GeoPoint SHOP = GeoPoint.of(37.5, 127.0);

    @Test
    @DisplayName("정확히 7000m 지점의 정점은 허용한다")
    void validateWithinMaxRadius_allowsExactly7000m() {
        double latitudeDelta = Math.toDegrees(7000.0 / GeoDistance.EARTH_RADIUS_METERS);
        GeoPolygon polygon = GeoPolygon.of(List.of(GeoRing.of(List.of(
            SHOP,
            GeoPoint.of(SHOP.latitude().doubleValue() + latitudeDelta, SHOP.longitude().doubleValue()),
            GeoPoint.of(SHOP.latitude().doubleValue(), SHOP.longitude().doubleValue() + 0.001)
        ))));

        assertThat(polygon.maxDistanceMetersFrom(SHOP)).isCloseTo(7000, org.assertj.core.data.Offset.offset(1.0));
        assertThatCode(() -> ShopDeliveryAreaPolicy.validateWithinMaxRadius(polygon, SHOP))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("7km를 넘는 도형은 거부한다")
    void validateWithinMaxRadius_rejectsBeyondLimit() {
        GeoPolygon polygon = GeoPolygon.of(List.of(GeoCircle.approximate(SHOP, 7100, 72)));

        assertThatThrownBy(() -> ShopDeliveryAreaPolicy.validateWithinMaxRadius(polygon, SHOP))
            .isInstanceOf(BusinessException.class)
            .extracting(e -> ((BusinessException) e).getErrorCode())
            .isEqualTo(ErrorCode.SHOP_DELIVERY_AREA_RADIUS_EXCEEDED);
    }

    @Test
    @DisplayName("정점 하나만 상한을 넘어도 거부한다")
    void validateWithinMaxRadius_rejectsWhenSingleVertexExceeds() {
        List<GeoPoint> points = new ArrayList<>(GeoCircle.approximate(SHOP, 1000, 8).points());
        points.add(GeoPoint.of(37.6, 127.09));

        GeoPolygon polygon = GeoPolygon.of(List.of(GeoRing.of(points)));

        assertThatThrownBy(() -> ShopDeliveryAreaPolicy.validateWithinMaxRadius(polygon, SHOP))
            .isInstanceOf(BusinessException.class)
            .extracting(e -> ((BusinessException) e).getErrorCode())
            .isEqualTo(ErrorCode.SHOP_DELIVERY_AREA_RADIUS_EXCEEDED);
    }

    @Test
    @DisplayName("링이 21개면 거부한다")
    void validateShape_rejectsTooManyRings() {
        List<GeoRing> rings = new ArrayList<>();
        for (int i = 0; i < ShopDeliveryAreaPolicy.MAX_RINGS + 1; i++) {
            rings.add(GeoCircle.approximate(GeoPoint.of(37.5 + i * 0.001, 127.0), 100, 4));
        }

        assertThatThrownBy(() -> ShopDeliveryAreaPolicy.validateShape(GeoPolygon.of(rings)))
            .isInstanceOf(BusinessException.class)
            .extracting(e -> ((BusinessException) e).getErrorCode())
            .isEqualTo(ErrorCode.SHOP_DELIVERY_AREA_POLYGON_INVALID);
    }

    @Test
    @DisplayName("링 20개는 허용한다")
    void validateShape_allowsMaxRings() {
        List<GeoRing> rings = new ArrayList<>();
        for (int i = 0; i < ShopDeliveryAreaPolicy.MAX_RINGS; i++) {
            rings.add(GeoCircle.approximate(GeoPoint.of(37.5 + i * 0.001, 127.0), 100, 4));
        }

        assertThatCode(() -> ShopDeliveryAreaPolicy.validateShape(GeoPolygon.of(rings)))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("총 정점이 5000개를 넘으면 거부한다")
    void validateShape_rejectsTooManyVertices() {
        GeoPolygon polygon = GeoPolygon.of(List.of(GeoCircle.approximate(SHOP, 1000, 5001)));

        assertThatThrownBy(() -> ShopDeliveryAreaPolicy.validateShape(polygon))
            .isInstanceOf(BusinessException.class)
            .extracting(e -> ((BusinessException) e).getErrorCode())
            .isEqualTo(ErrorCode.SHOP_DELIVERY_AREA_POLYGON_INVALID);
    }

    @Test
    @DisplayName("반영 후 총 개수가 500을 넘으면 거부하고, 정확히 500이면 허용한다")
    void validateTotalCount_enforcesLimit() {
        assertThatCode(() -> ShopDeliveryAreaPolicy.validateTotalCount(ShopDeliveryAreaPolicy.MAX_DELIVERY_AREA_COUNT))
            .doesNotThrowAnyException();

        assertThatThrownBy(() -> ShopDeliveryAreaPolicy.validateTotalCount(ShopDeliveryAreaPolicy.MAX_DELIVERY_AREA_COUNT + 1))
            .isInstanceOf(BusinessException.class)
            .extracting(e -> ((BusinessException) e).getErrorCode())
            .isEqualTo(ErrorCode.SHOP_DELIVERY_AREA_COUNT_EXCEEDED);
    }

    @Test
    @DisplayName("반경은 500m 미만·7000m 초과를 거부한다")
    void validateRadius_enforcesRange() {
        assertThatCode(() -> ShopDeliveryAreaPolicy.validateRadius(500)).doesNotThrowAnyException();
        assertThatCode(() -> ShopDeliveryAreaPolicy.validateRadius(7000)).doesNotThrowAnyException();

        assertThatThrownBy(() -> ShopDeliveryAreaPolicy.validateRadius(499))
            .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> ShopDeliveryAreaPolicy.validateRadius(7001))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("부동소수 오차로 아슬아슬하게 넘는 값은 위반으로 보지 않는다")
    void exceedsMaxRadius_toleratesFloatingPointNoise() {
        assertThat(ShopDeliveryAreaPolicy.exceedsMaxRadius(7000.0000001)).isFalse();
        assertThat(ShopDeliveryAreaPolicy.exceedsMaxRadius(7000.5)).isTrue();
    }
}
