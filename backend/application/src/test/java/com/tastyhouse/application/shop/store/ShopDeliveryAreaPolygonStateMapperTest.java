package com.tastyhouse.application.shop.store;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.application.shop.port.out.write.ShopDeliveryAreaPolygonState;
import com.tastyhouse.domain.shared.geo.GeoPoint;
import com.tastyhouse.domain.shared.geo.GeoPolygon;
import com.tastyhouse.domain.shared.geo.GeoRing;
import com.tastyhouse.domain.shop.model.ShopDeliveryAreaPolygon;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;

class ShopDeliveryAreaPolygonStateMapperTest {

    @Test
    @DisplayName("ShopDeliveryAreaPolygon → ShopDeliveryAreaPolygonState → ShopDeliveryAreaPolygon 왕복 시 도형·기준점을 포함한 모든 필드가 보존된다")
    void shopDeliveryAreaPolygonRoundTrip() {
        ShopDeliveryAreaPolygon original = ShopDeliveryAreaPolygon.reconstitute(
            31L,
            ShopId.of(32L),
            GeoPolygon.of(List.of(
                ring(point("37.495000", "126.995000"), point("37.495000", "127.005000"), point("37.505000", "127.005000")),
                ring(point("37.498000", "126.998000"), point("37.498000", "127.001000"), point("37.501000", "127.001000"))
            )),
            point("37.500000", "127.000000"),
            733
        );

        ShopDeliveryAreaPolygon restored =
            ShopDeliveryAreaPolygonStateMapper.toDomain(ShopDeliveryAreaPolygonStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("State의 도형 스냅샷에 WKT 문자열과 고리·꼭짓점 개수가 담긴다")
    void shapeSnapshotCarriesEncodedRingsAndCounts() {
        ShopDeliveryAreaPolygon polygon = ShopDeliveryAreaPolygon.reconstitute(
            33L,
            ShopId.of(34L),
            GeoPolygon.of(List.of(
                ring(point("37.495000", "126.995000"), point("37.495000", "127.005000"), point("37.505000", "127.005000"))
            )),
            point("37.500000", "127.000000"),
            512
        );

        ShopDeliveryAreaPolygonState state = ShopDeliveryAreaPolygonStateMapper.toState(polygon);

        assertThat(state.polygon().encodedRings())
            .isEqualTo("126.995000 37.495000,127.005000 37.495000,127.005000 37.505000");
        assertThat(state.polygon().ringCount()).isEqualTo(polygon.getRingCount());
        assertThat(state.polygon().vertexCount()).isEqualTo(polygon.getVertexCount());
        assertThat(state.center().latitude()).isEqualByComparingTo("37.500000");
        assertThat(state.center().longitude()).isEqualByComparingTo("127.000000");
    }

    private static GeoPoint point(String latitude, String longitude) {
        return GeoPoint.of(new BigDecimal(latitude), new BigDecimal(longitude));
    }

    private static GeoRing ring(GeoPoint... points) {
        return GeoRing.of(List.of(points));
    }
}
