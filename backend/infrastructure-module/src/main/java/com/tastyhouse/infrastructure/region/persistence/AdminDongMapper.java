package com.tastyhouse.infrastructure.region.persistence;

import com.tastyhouse.domain.region.model.AdminDong;

/**
 * 행정동 JPA 엔티티 → 도메인 모델 변환기. read-only 마스터라 저장 방향({@code toEntity})이 없다.
 */
final class AdminDongMapper {

    private AdminDongMapper() {
    }

    static AdminDong toDomain(AdminDongJpaEntity entity) {
        return AdminDong.reconstitute(
            entity.getId(),
            entity.getCode(),
            entity.getSidoName(),
            entity.getSigunguName(),
            entity.getDongName(),
            entity.isActive()
        );
    }
}
