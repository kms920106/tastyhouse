package com.tastyhouse.domain.shared.geo;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class GeoDistanceTest {
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
            assertThat(GeoDistance.distanceMeters(
                BASE_LATITUDE, BASE_LONGITUDE, new BigDecimal("37.509"), BASE_LONGITUDE
            )).isCloseTo(1000.75, within(1.0));

            assertThat(GeoDistance.distanceMeters(
                BASE_LATITUDE, BASE_LONGITUDE, BASE_LATITUDE, new BigDecimal("127.01")
            )).isCloseTo(882.17, within(1.0));

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
