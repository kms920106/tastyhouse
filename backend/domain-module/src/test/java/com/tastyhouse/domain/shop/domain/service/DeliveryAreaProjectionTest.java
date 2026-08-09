package com.tastyhouse.domain.shop.domain.service;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.region.model.AdminDong;
import com.tastyhouse.domain.region.vo.AdminDongId;
import com.tastyhouse.domain.shared.geo.GeoPoint;
import com.tastyhouse.domain.shared.geo.GeoPolygon;
import com.tastyhouse.domain.shared.geo.GeoRing;
import com.tastyhouse.domain.shop.service.DeliveryAreaProjection;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 도형 → 행정동 환산 단위 테스트.
 *
 * <p>이 판정이 주문 접수 가능 범위를 정하므로, 임계값 근처의 동작과 <b>판정 불가 처리</b>를 명시적으로
 * 고정한다 — 좌표·경계가 모두 없는 동을 조용히 포함시키면 점주가 의도하지 않은 지역이 열린다.
 */
class DeliveryAreaProjectionTest {

    /** 경도 0~10, 위도 0~10 정사각형. */
    private static final GeoPolygon SQUARE = GeoPolygon.of(List.of(ring(0, 0, 10, 0, 10, 10, 0, 10)));

    /** {@code SQUARE} 밖임이 자명한 좌표값. 대표점을 판정에서 배제할 때 쓴다. */
    private static final double OUTSIDE = 50;

    @Test
    @DisplayName("대표점이 도형 안이면 포함한다(1차 규칙)")
    void project_includesWhenCenterInside() {
        AdminDong inside = dongWithCenter(1L, 5, 5);
        AdminDong outside = dongWithCenter(2L, 50, 50);

        DeliveryAreaProjection.Result result = DeliveryAreaProjection.project(SQUARE, List.of(inside, outside));

        assertThat(result.adminDongIds()).containsExactly(AdminDongId.of(1L));
        assertThat(result.unresolvedCount()).isZero();
    }

    @Test
    @DisplayName("대표점은 밖이지만 경계 정점의 30% 이상이 안이면 포함한다(2차 규칙)")
    void project_includesWhenBoundaryCoverageMeetsThreshold() {
        // 정점 10개 중 4개(40%)가 도형 안, 대표점은 밖.
        AdminDong dong = dongWithBoundary(1L, coverageRing(4, 6));

        DeliveryAreaProjection.Result result = DeliveryAreaProjection.project(SQUARE, List.of(dong));

        assertThat(result.adminDongIds()).containsExactly(AdminDongId.of(1L));
    }

    @Test
    @DisplayName("경계 정점 내부 비율이 30% 미만이면 제외한다")
    void project_excludesWhenBoundaryCoverageBelowThreshold() {
        // 정점 10개 중 2개(20%)만 도형 안.
        AdminDong dong = dongWithBoundary(1L, coverageRing(2, 8));

        DeliveryAreaProjection.Result result = DeliveryAreaProjection.project(SQUARE, List.of(dong));

        assertThat(result.adminDongIds()).isEmpty();
        assertThat(result.unresolvedCount()).isZero();
    }

    @Test
    @DisplayName("대표점도 경계도 없는 동은 판정 불가로 세고 포함하지 않는다")
    void project_countsUnresolvedWithoutIncluding() {
        AdminDong unresolved = AdminDong.reconstitute(9L, "code", "시", "군", "동", true, null, List.of());

        DeliveryAreaProjection.Result result = DeliveryAreaProjection.project(SQUARE, List.of(unresolved));

        assertThat(result.adminDongIds()).isEmpty();
        assertThat(result.unresolvedCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("대표점만 있는 동과 경계까지 있는 동이 섞여도 각각의 규칙으로 판정한다")
    void project_handlesMixedCandidates() {
        AdminDong byCenter = dongWithCenter(1L, 5, 5);
        AdminDong byBoundary = dongWithBoundary(2L, coverageRing(5, 5));
        AdminDong excluded = dongWithCenter(3L, 80, 80);
        AdminDong unresolved = AdminDong.reconstitute(4L, "code", "시", "군", "동", true, null, List.of());

        DeliveryAreaProjection.Result result = DeliveryAreaProjection.project(
            SQUARE, List.of(byCenter, byBoundary, excluded, unresolved)
        );

        assertThat(result.adminDongIds()).containsExactly(AdminDongId.of(1L), AdminDongId.of(2L));
        assertThat(result.unresolvedCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("후보가 0건이면 빈 결과를 돌려준다")
    void project_returnsEmptyForNoCandidates() {
        DeliveryAreaProjection.Result result = DeliveryAreaProjection.project(SQUARE, List.of());

        assertThat(result.isEmpty()).isTrue();
        assertThat(result.count()).isZero();
    }

    /** 도형 안에 {@code inside}개, 밖에 {@code outside}개의 정점을 갖는 경계 링. */
    private static GeoRing coverageRing(int inside, int outside) {
        List<GeoPoint> points = new ArrayList<>();
        for (int i = 0; i < inside; i++) {
            points.add(GeoPoint.of(1 + i * 0.1, 1 + i * 0.1));
        }
        for (int i = 0; i < outside; i++) {
            points.add(GeoPoint.of(OUTSIDE + i * 0.1, OUTSIDE + i * 0.1));
        }
        return GeoRing.of(points);
    }

    private static AdminDong dongWithCenter(long id, double longitude, double latitude) {
        return AdminDong.reconstitute(
            id, "code", "시", "군", "동", true, GeoPoint.of(latitude, longitude), List.of()
        );
    }

    /**
     * 대표점이 {@code SQUARE} <b>밖</b>에 있고 경계만 갖는 행정동. 2차 규칙(경계 정점 내부 비율)만
     * 판정에 관여하도록 대표점을 도형 밖 좌표로 고정한다.
     */
    private static AdminDong dongWithBoundary(long id, GeoRing boundary) {
        return AdminDong.reconstitute(
            id, "code", "시", "군", "동", true, GeoPoint.of(OUTSIDE, OUTSIDE), List.of(boundary)
        );
    }

    private static GeoRing ring(double... lngLatPairs) {
        List<GeoPoint> points = new ArrayList<>();
        for (int i = 0; i < lngLatPairs.length; i += 2) {
            points.add(GeoPoint.of(lngLatPairs[i + 1], lngLatPairs[i]));
        }
        return GeoRing.of(points);
    }
}
