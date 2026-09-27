package com.tastyhouse.application.region.store;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.application.region.port.out.write.AdminDongBoundarySnapshot;
import com.tastyhouse.application.region.port.out.write.AdminDongState;
import com.tastyhouse.domain.region.model.AdminDong;
import com.tastyhouse.domain.shared.geo.GeoPoint;
import com.tastyhouse.domain.shared.geo.GeoRing;

import static org.assertj.core.api.Assertions.assertThat;

class AdminDongStateMapperTest {

    @Test
    @DisplayName("AdminDong → AdminDongState → AdminDong 왕복 시 중심점·경계를 포함한 모든 필드가 보존된다")
    void roundTrip() {
        AdminDong original = AdminDong.reconstitute(
            21L, "1168010100", "서울특별시", "강남구", "역삼1동", true,
            point("37.500000", "127.036000"),
            List.of(
                ring(point("37.495000", "127.030000"), point("37.495000", "127.045000"), point("37.506000", "127.045000")),
                ring(point("37.498000", "127.034000"), point("37.498000", "127.038000"), point("37.501000", "127.038000"))
            ));

        AdminDong restored = AdminDongStateMapper.toDomain(AdminDongStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
        assertThat(restored.getCenter()).isEqualTo(original.getCenter());
    }

    @Test
    @DisplayName("경계에서 계산한 바운딩 박스가 State에 담긴다")
    void boundingBoxIsComputedFromBoundary() {
        AdminDong adminDong = AdminDong.reconstitute(
            22L, "1168010200", "서울특별시", "강남구", "역삼2동", true,
            point("37.500000", "127.036000"),
            List.of(ring(point("37.495000", "127.030000"), point("37.495000", "127.045000"), point("37.506000", "127.040000"))));

        AdminDongBoundarySnapshot boundary = AdminDongStateMapper.toState(adminDong).boundary();

        assertThat(boundary.encodedRings()).isEqualTo("127.030000 37.495000,127.045000 37.495000,127.040000 37.506000");
        assertThat(boundary.minLatitude()).isEqualByComparingTo("37.495000");
        assertThat(boundary.maxLatitude()).isEqualByComparingTo("37.506000");
        assertThat(boundary.minLongitude()).isEqualByComparingTo("127.030000");
        assertThat(boundary.maxLongitude()).isEqualByComparingTo("127.045000");
    }

    @Test
    @DisplayName("중심점·경계가 없어도 왕복되고 State의 두 스냅샷은 null이다")
    void roundTripWithoutCenterAndBoundary() {
        AdminDong original = AdminDong.reconstitute(
            23L, "1168010300", "서울특별시", "서초구", "서초1동", false, null, List.of());

        AdminDongState state = AdminDongStateMapper.toState(original);
        AdminDong restored = AdminDongStateMapper.toDomain(state);

        assertThat(state.center()).isNull();
        assertThat(state.boundary()).isNull();
        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    private static GeoPoint point(String latitude, String longitude) {
        return GeoPoint.of(new BigDecimal(latitude), new BigDecimal(longitude));
    }

    private static GeoRing ring(GeoPoint... points) {
        return GeoRing.of(List.of(points));
    }
}
