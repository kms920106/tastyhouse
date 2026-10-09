package com.tastyhouse.infrastructure.jpa.shop.persistence;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.shared.geo.GeoPoint;
import com.tastyhouse.domain.shared.geo.GeoPolygon;
import com.tastyhouse.domain.shared.geo.GeoPolygonTextCodec;
import com.tastyhouse.domain.shared.geo.GeoRing;
import com.tastyhouse.domain.shop.model.ShopDeliveryAreaPolygon;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;

class ShopDeliveryAreaPolygonMapperTest {

    @Test
    @DisplayName("ShopDeliveryAreaPolygon 도메인 → 엔티티 변환 시 도형·기준점을 포함한 컬럼 값이 보존된다")
    void domainToEntity() {
        ShopDeliveryAreaPolygon original = twoRingPolygon();

        ShopDeliveryAreaPolygonJpaEntity entity = ShopDeliveryAreaPolygonMapper.toEntity(original);

        assertThat(entity.getShopId()).isEqualTo(32L);
        assertThat(entity.getRings()).isEqualTo(GeoPolygonTextCodec.encode(original.getPolygon()));
        assertThat(entity.getCenterLatitude()).isEqualByComparingTo("37.500000");
        assertThat(entity.getCenterLongitude()).isEqualByComparingTo("127.000000");
        assertThat(entity.getMaxRadiusMeters()).isEqualTo(733);
        assertThat(entity.getRingCount()).isEqualTo(original.getRingCount());
        assertThat(entity.getVertexCount()).isEqualTo(original.getVertexCount());
    }

    @Test
    @DisplayName("엔티티의 도형 컬럼에 WKT 문자열과 고리·꼭짓점 개수가 담긴다")
    void entityCarriesEncodedRingsAndCounts() {
        ShopDeliveryAreaPolygon polygon = ShopDeliveryAreaPolygon.reconstitute(
            33L,
            ShopId.of(34L),
            GeoPolygon.of(List.of(
                ring(point("37.495000", "126.995000"), point("37.495000", "127.005000"), point("37.505000", "127.005000"))
            )),
            point("37.500000", "127.000000"),
            512
        );

        ShopDeliveryAreaPolygonJpaEntity entity = ShopDeliveryAreaPolygonMapper.toEntity(polygon);

        assertThat(entity.getRings())
            .isEqualTo("126.995000 37.495000,127.005000 37.495000,127.005000 37.505000");
        assertThat(entity.getRingCount()).isEqualTo(polygon.getRingCount());
        assertThat(entity.getVertexCount()).isEqualTo(polygon.getVertexCount());
        assertThat(entity.getCenterLatitude()).isEqualByComparingTo("37.500000");
        assertThat(entity.getCenterLongitude()).isEqualByComparingTo("127.000000");
    }

    @Test
    @DisplayName("ShopDeliveryAreaPolygon 엔티티 → 도메인 변환 시 도형·기준점을 포함한 모든 필드가 보존된다")
    void entityToDomain() {
        ShopDeliveryAreaPolygon original = twoRingPolygon();

        ShopDeliveryAreaPolygonJpaEntity entity = ShopDeliveryAreaPolygonJpaEntity.create(
            32L,
            GeoPolygonTextCodec.encode(original.getPolygon()),
            new BigDecimal("37.500000"),
            new BigDecimal("127.000000"),
            733,
            original.getRingCount(),
            original.getVertexCount()
        );
        ReflectionTestUtils.setField(entity, "id", 31L);
        ReflectionTestUtils.setField(entity, "createdAt", LocalDateTime.of(2026, 2, 1, 0, 0));
        ReflectionTestUtils.setField(entity, "updatedAt", LocalDateTime.of(2026, 2, 1, 0, 0));

        assertThat(ShopDeliveryAreaPolygonMapper.toDomain(entity)).usingRecursiveComparison().isEqualTo(original);
    }

    private static ShopDeliveryAreaPolygon twoRingPolygon() {
        return ShopDeliveryAreaPolygon.reconstitute(
            31L,
            ShopId.of(32L),
            GeoPolygon.of(List.of(
                ring(point("37.495000", "126.995000"), point("37.495000", "127.005000"), point("37.505000", "127.005000")),
                ring(point("37.498000", "126.998000"), point("37.498000", "127.001000"), point("37.501000", "127.001000"))
            )),
            point("37.500000", "127.000000"),
            733
        );
    }

    private static GeoPoint point(String latitude, String longitude) {
        return GeoPoint.of(new BigDecimal(latitude), new BigDecimal(longitude));
    }

    private static GeoRing ring(GeoPoint... points) {
        return GeoRing.of(List.of(points));
    }
}
