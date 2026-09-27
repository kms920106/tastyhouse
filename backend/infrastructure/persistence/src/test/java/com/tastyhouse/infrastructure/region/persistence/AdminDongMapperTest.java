package com.tastyhouse.infrastructure.region.persistence;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.region.model.AdminDong;
import com.tastyhouse.domain.shared.geo.GeoPoint;
import com.tastyhouse.domain.shared.geo.GeoRing;

import static org.assertj.core.api.Assertions.assertThat;

class AdminDongMapperTest {

    @Test
    @DisplayName("AdminDong → 엔티티 변환 시 중심점은 평탄 컬럼으로, 경계는 인코딩 문자열로 채워진다")
    void toEntity() {
        AdminDong adminDong = AdminDong.reconstitute(
            21L, "1168010100", "서울특별시", "강남구", "역삼1동", true,
            point("37.500000", "127.036000"),
            List.of(
                ring(point("37.495000", "127.030000"), point("37.495000", "127.045000"), point("37.506000", "127.045000")),
                ring(point("37.498000", "127.034000"), point("37.498000", "127.038000"), point("37.501000", "127.038000"))
            ));

        AdminDongJpaEntity entity = AdminDongMapper.toEntity(adminDong);

        assertThat(entity.getCode()).isEqualTo("1168010100");
        assertThat(entity.getSidoName()).isEqualTo("서울특별시");
        assertThat(entity.getSigunguName()).isEqualTo("강남구");
        assertThat(entity.getDongName()).isEqualTo("역삼1동");
        assertThat(entity.isActive()).isTrue();
        assertThat(entity.getCenterLatitude()).isEqualByComparingTo("37.500000");
        assertThat(entity.getCenterLongitude()).isEqualByComparingTo("127.036000");
        assertThat(entity.getBoundary()).isNotNull();
        assertThat(entity.getBoundaryMinLatitude()).isEqualByComparingTo("37.495000");
        assertThat(entity.getBoundaryMaxLatitude()).isEqualByComparingTo("37.506000");
        assertThat(entity.getBoundaryMinLongitude()).isEqualByComparingTo("127.030000");
        assertThat(entity.getBoundaryMaxLongitude()).isEqualByComparingTo("127.045000");
    }

    @Test
    @DisplayName("경계에서 계산한 바운딩 박스가 엔티티 컬럼에 담긴다")
    void boundingBoxIsComputedFromBoundary() {
        AdminDong adminDong = AdminDong.reconstitute(
            22L, "1168010200", "서울특별시", "강남구", "역삼2동", true,
            point("37.500000", "127.036000"),
            List.of(ring(point("37.495000", "127.030000"), point("37.495000", "127.045000"), point("37.506000", "127.040000"))));

        AdminDongJpaEntity entity = AdminDongMapper.toEntity(adminDong);

        assertThat(entity.getBoundary()).isEqualTo("127.030000 37.495000,127.045000 37.495000,127.040000 37.506000");
        assertThat(entity.getBoundaryMinLatitude()).isEqualByComparingTo("37.495000");
        assertThat(entity.getBoundaryMaxLatitude()).isEqualByComparingTo("37.506000");
        assertThat(entity.getBoundaryMinLongitude()).isEqualByComparingTo("127.030000");
        assertThat(entity.getBoundaryMaxLongitude()).isEqualByComparingTo("127.045000");
    }

    @Test
    @DisplayName("applyChanges는 경계에서 계산한 바운딩 박스로 기존 엔티티를 덮어쓴다")
    void applyChangesComputesBoundingBox() {
        AdminDongJpaEntity entity = AdminDongJpaEntity.create(
            "1168010200", "옛시도", "옛시군구", "옛동", false, null, null, null, null, null, null, null);
        AdminDong adminDong = AdminDong.reconstitute(
            22L, "1168010200", "서울특별시", "강남구", "역삼2동", true,
            point("37.500000", "127.036000"),
            List.of(ring(point("37.495000", "127.030000"), point("37.495000", "127.045000"), point("37.506000", "127.040000"))));

        AdminDongMapper.applyChanges(entity, adminDong);

        assertThat(entity.getSidoName()).isEqualTo("서울특별시");
        assertThat(entity.getSigunguName()).isEqualTo("강남구");
        assertThat(entity.getDongName()).isEqualTo("역삼2동");
        assertThat(entity.isActive()).isTrue();
        assertThat(entity.getCenterLatitude()).isEqualByComparingTo("37.500000");
        assertThat(entity.getCenterLongitude()).isEqualByComparingTo("127.036000");
        assertThat(entity.getBoundary()).isEqualTo("127.030000 37.495000,127.045000 37.495000,127.040000 37.506000");
        assertThat(entity.getBoundaryMinLatitude()).isEqualByComparingTo("37.495000");
        assertThat(entity.getBoundaryMaxLatitude()).isEqualByComparingTo("37.506000");
        assertThat(entity.getBoundaryMinLongitude()).isEqualByComparingTo("127.030000");
        assertThat(entity.getBoundaryMaxLongitude()).isEqualByComparingTo("127.045000");
    }

    @Test
    @DisplayName("중심점·경계가 없는 AdminDong은 중심점·경계 컬럼이 모두 null이다")
    void toEntityWithoutCenterAndBoundary() {
        AdminDong adminDong = AdminDong.reconstitute(
            23L, "1168010300", "서울특별시", "서초구", "서초1동", false, null, List.of());

        AdminDongJpaEntity entity = AdminDongMapper.toEntity(adminDong);

        assertThat(entity.isActive()).isFalse();
        assertThat(entity.getCenterLatitude()).isNull();
        assertThat(entity.getCenterLongitude()).isNull();
        assertThat(entity.getBoundary()).isNull();
        assertThat(entity.getBoundaryMinLatitude()).isNull();
        assertThat(entity.getBoundaryMaxLatitude()).isNull();
        assertThat(entity.getBoundaryMinLongitude()).isNull();
        assertThat(entity.getBoundaryMaxLongitude()).isNull();
    }

    @Test
    @DisplayName("엔티티 → AdminDong 변환 시 id·중심점·경계를 포함한 모든 필드가 복원된다")
    void toDomain() {
        AdminDong expected = AdminDong.reconstitute(
            21L, "1168010100", "서울특별시", "강남구", "역삼1동", true,
            point("37.500000", "127.036000"),
            List.of(
                ring(point("37.495000", "127.030000"), point("37.495000", "127.045000"), point("37.506000", "127.045000")),
                ring(point("37.498000", "127.034000"), point("37.498000", "127.038000"), point("37.501000", "127.038000"))
            ));
        AdminDongJpaEntity entity = AdminDongMapper.toEntity(expected);
        ReflectionTestUtils.setField(entity, "id", 21L);

        AdminDong adminDong = AdminDongMapper.toDomain(entity);

        assertThat(adminDong).usingRecursiveComparison().isEqualTo(expected);
        assertThat(adminDong.getCenter()).isEqualTo(expected.getCenter());
    }

    @Test
    @DisplayName("중심점·경계 컬럼이 null인 엔티티는 중심점 null·빈 경계로 복원된다")
    void toDomainWithoutCenterAndBoundary() {
        AdminDongJpaEntity entity = AdminDongJpaEntity.create(
            "1168010300", "서울특별시", "서초구", "서초1동", false, null, null, null, null, null, null, null);
        ReflectionTestUtils.setField(entity, "id", 23L);

        AdminDong adminDong = AdminDongMapper.toDomain(entity);

        assertThat(adminDong).usingRecursiveComparison().isEqualTo(AdminDong.reconstitute(
            23L, "1168010300", "서울특별시", "서초구", "서초1동", false, null, List.of()));
    }

    @Test
    @DisplayName("중심점 좌표 중 하나만 null이어도 중심점은 null로 복원된다")
    void toDomainWithPartialCenter() {
        AdminDongJpaEntity entity = AdminDongJpaEntity.create(
            "1168010400", "서울특별시", "서초구", "서초2동", true,
            new BigDecimal("37.480000"), null, null, null, null, null, null);
        ReflectionTestUtils.setField(entity, "id", 24L);

        AdminDong adminDong = AdminDongMapper.toDomain(entity);

        assertThat(adminDong.getCenter()).isNull();
    }

    private static GeoPoint point(String latitude, String longitude) {
        return GeoPoint.of(new BigDecimal(latitude), new BigDecimal(longitude));
    }

    private static GeoRing ring(GeoPoint... points) {
        return GeoRing.of(List.of(points));
    }
}
