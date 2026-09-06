package com.tastyhouse.domain.shared.geo;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * 하버사인 직선거리 유틸 단위 테스트.
 *
 * <p>{@code ShopConvenienceInfoService}의 private static 메서드에서 공용 위치로 승격된 계산식이므로,
 * 승격 후에도 기존 소비자의 1km 반경 판정이 그대로 성립하는지를 함께 본다(계산식 무변경 회귀 방어).
 */
class GeoDistanceTest {

    /** {@code ShopConvenienceInfoService}의 표시위치 반경 상한과 같은 값. */
    private static final double MAX_DISPLAY_LOCATION_DISTANCE_METERS = 1000;

    private static final BigDecimal BASE_LATITUDE = new BigDecimal("37.5");
    private static final BigDecimal BASE_LONGITUDE = new BigDecimal("127.0");

    @Nested
    @DisplayName("distanceMeters")
    class DistanceMeters {

        @Test
        @DisplayName("같은 좌표의 거리는 0m다")
        void distanceMeters_zeroForSameCoordinates() {
            double distance = GeoDistance.distanceMeters(
                BASE_LATITUDE, BASE_LONGITUDE, BASE_LATITUDE, BASE_LONGITUDE
            );

            assertThat(distance).isZero();
        }

        @Test
        @DisplayName("알려진 좌표쌍의 거리를 하버사인 기준값과 일치시킨다")
        void distanceMeters_matchesKnownPairs() {
            // 위도 0.009도 차이 ≈ 1,000.75m
            assertThat(GeoDistance.distanceMeters(
                BASE_LATITUDE, BASE_LONGITUDE, new BigDecimal("37.509"), BASE_LONGITUDE
            )).isCloseTo(1000.75, within(1.0));

            // 위도 37.5에서 경도 0.01도 차이 ≈ 882.17m
            assertThat(GeoDistance.distanceMeters(
                BASE_LATITUDE, BASE_LONGITUDE, BASE_LATITUDE, new BigDecimal("127.01")
            )).isCloseTo(882.17, within(1.0));

            // 서울시청 ~ 남산 ≈ 1,924m
            assertThat(GeoDistance.distanceMeters(
                new BigDecimal("37.5665"), new BigDecimal("126.9780"),
                new BigDecimal("37.5512"), new BigDecimal("126.9882")
            )).isCloseTo(1924.25, within(1.0));
        }

        @Test
        @DisplayName("거리는 대칭이다 — a→b와 b→a가 같다")
        void distanceMeters_isSymmetric() {
            BigDecimal otherLatitude = new BigDecimal("37.5512");
            BigDecimal otherLongitude = new BigDecimal("126.9882");

            double forward = GeoDistance.distanceMeters(BASE_LATITUDE, BASE_LONGITUDE, otherLatitude, otherLongitude);
            double backward = GeoDistance.distanceMeters(otherLatitude, otherLongitude, BASE_LATITUDE, BASE_LONGITUDE);

            assertThat(forward).isCloseTo(backward, within(1e-9));
        }
    }

    @Nested
    @DisplayName("승격 후 1km 판정 무변경")
    class DisplayLocationRadius {

        @Test
        @DisplayName("1km 이내 좌표는 기존 ShopConvenienceInfoService 판정대로 통과한다")
        void distanceMeters_withinOneKilometerPasses() {
            double distance = GeoDistance.distanceMeters(
                BASE_LATITUDE, BASE_LONGITUDE, BASE_LATITUDE, new BigDecimal("127.01")
            );

            assertThat(distance).isLessThanOrEqualTo(MAX_DISPLAY_LOCATION_DISTANCE_METERS);
        }

        @Test
        @DisplayName("1km를 넘는 좌표는 기존 ShopConvenienceInfoService 판정대로 반경 밖이다")
        void distanceMeters_beyondOneKilometerFails() {
            double distance = GeoDistance.distanceMeters(
                BASE_LATITUDE, BASE_LONGITUDE, new BigDecimal("37.509"), BASE_LONGITUDE
            );

            assertThat(distance).isGreaterThan(MAX_DISPLAY_LOCATION_DISTANCE_METERS);
        }
    }
}
