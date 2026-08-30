package com.tastyhouse.infrastructure.region.persistence;

import java.util.List;

import com.tastyhouse.domain.region.model.AdminDong;
import com.tastyhouse.domain.shared.geo.GeoBoundingBox;
import com.tastyhouse.domain.shared.geo.GeoPoint;
import com.tastyhouse.domain.shared.geo.GeoRing;
import com.tastyhouse.infrastructure.shared.persistence.GeoPolygonTextCodec;

/**
 * 행정동 도메인 모델 ↔ JPA 엔티티 변환기.
 *
 * <p>좌표·경계는 원천이 단계적으로 채우므로 <b>둘 다 없을 수 있다</b>. 대표점은 위경도가 모두 있을
 * 때만 {@code GeoPoint}로 승격하고(하나만 있으면 좌표로서 의미가 없다), 경계는 빈 문자열·null 모두
 * 빈 목록으로 정규화한다.
 *
 * <p>저장 방향({@link #toEntity})은 동기화 배치 전용이다. 마스터는 건별로 갱신되지 않으므로
 * {@code applyChanges}(managed 엔티티 필드 복사)가 없다 — 전량 교체만 존재한다.
 */
final class AdminDongMapper {

    private AdminDongMapper() {
    }

    /**
     * 동기화 배치가 만든 도메인 모델을 신규 영속 엔티티로 변환한다.
     *
     * <p>바운딩박스는 경계 정점 전체를 감싸는 최소 사각형으로 <b>여기서 파생</b>시킨다. 경계가 없으면
     * 박스도 없으며, 그런 행은 좌표 프리필터({@code findAllWithinBoundingBox})에 걸리지 않는 대신
     * 대표점 기준 판정만 받는다.
     */
    static AdminDongJpaEntity toEntity(AdminDong adminDong) {
        GeoPoint center = adminDong.getCenter();
        return AdminDongJpaEntity.create(
            adminDong.getCode(),
            adminDong.getSidoName(),
            adminDong.getSigunguName(),
            adminDong.getDongName(),
            adminDong.isActive(),
            center == null ? null : center.latitude(),
            center == null ? null : center.longitude(),
            toBoundingBox(adminDong.getBoundary()),
            GeoPolygonTextCodec.encodeRings(adminDong.getBoundary())
        );
    }

    /**
     * 원천 값을 managed 엔티티에 복사한다(id 보존 갱신).
     *
     * <p>{@link #toEntity}와 파생 계산(바운딩박스·경계 인코딩)이 동일해야 하므로 두 경로가 같은
     * 헬퍼를 쓴다 — 한쪽만 바뀌면 신규 행과 갱신 행의 저장 형태가 갈린다.
     */
    static void applyChanges(AdminDongJpaEntity entity, AdminDong adminDong) {
        GeoPoint center = adminDong.getCenter();
        entity.applyChanges(
            adminDong.getSidoName(),
            adminDong.getSigunguName(),
            adminDong.getDongName(),
            adminDong.isActive(),
            center == null ? null : center.latitude(),
            center == null ? null : center.longitude(),
            toBoundingBox(adminDong.getBoundary()),
            GeoPolygonTextCodec.encodeRings(adminDong.getBoundary())
        );
    }

    private static GeoBoundingBox toBoundingBox(List<GeoRing> boundary) {
        if (boundary.isEmpty()) {
            return null;
        }

        List<GeoPoint> points = boundary.stream().flatMap(ring -> ring.points().stream()).toList();
        return GeoBoundingBox.enclosing(points);
    }

    static AdminDong toDomain(AdminDongJpaEntity entity) {
        return AdminDong.reconstitute(
            entity.getId(),
            entity.getCode(),
            entity.getSidoName(),
            entity.getSigunguName(),
            entity.getDongName(),
            entity.isActive(),
            toCenter(entity),
            GeoPolygonTextCodec.decodeRings(entity.getBoundary())
        );
    }

    private static GeoPoint toCenter(AdminDongJpaEntity entity) {
        if (entity.getCenterLatitude() == null || entity.getCenterLongitude() == null) {
            return null;
        }
        return GeoPoint.of(entity.getCenterLatitude(), entity.getCenterLongitude());
    }
}
