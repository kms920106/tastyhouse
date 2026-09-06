package com.tastyhouse.domain.shared.geo;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("InteriorPoint")
class InteriorPointTest {
    @Test
    @DisplayName("사각형의 대표점은 내부에 있다")
    void squareRepresentativePointIsInside() {
        GeoRing square = ring(
            point(0, 0),
            point(0, 10),
            point(10, 10),
            point(10, 0)
        );

        GeoPoint representative = InteriorPoint.of(List.of(square));

        assertThat(representative).isNotNull();
        assertThat(PointInPolygon.contains(GeoPolygon.of(List.of(square)), representative)).isTrue();
    }

    @Test
    @DisplayName("오목한 ㄷ자 도형에서도 대표점이 내부에 있다(centroid는 밖으로 나간다)")
    void concaveRepresentativePointIsStillInside() {
        GeoRing concave = ring(
            point(0, 0),
            point(0, 10),
            point(4, 10),
            point(4, 3),
            point(6, 3),
            point(6, 10),
            point(10, 10),
            point(10, 0)
        );

        GeoPoint representative = InteriorPoint.of(List.of(concave));

        assertThat(representative).isNotNull();
        assertThat(PointInPolygon.contains(GeoPolygon.of(List.of(concave)), representative)).isTrue();
    }

    @Test
    @DisplayName("구멍이 있는 도형의 대표점은 구멍 안이 아니다")
    void holeIsNotChosenAsRepresentativePoint() {
        GeoRing outer = ring(
            point(0, 0),
            point(0, 10),
            point(10, 10),
            point(10, 0)
        );

        GeoRing hole = ring(
            point(3, 3),
            point(3, 7),
            point(7, 7),
            point(7, 3)
        );

        GeoPoint representative = InteriorPoint.of(List.of(outer, hole));

        assertThat(representative).isNotNull();
        assertThat(PointInPolygon.contains(GeoPolygon.of(List.of(outer, hole)), representative)).isTrue();
    }

    @Test
    @DisplayName("링이 없으면 대표점도 없다")
    void emptyRingsProduceNoPoint() {
        assertThat(InteriorPoint.of(List.of())).isNull();
        assertThat(InteriorPoint.of(null)).isNull();
    }

    private static GeoRing ring(GeoPoint... points) {
        return GeoRing.of(List.of(points));
    }

    private static GeoPoint point(double latitude, double longitude) {
        return GeoPoint.of(BigDecimal.valueOf(latitude), BigDecimal.valueOf(longitude));
    }
}
