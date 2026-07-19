package com.tastyhouse.infrastructure.admin.persistence;

import com.tastyhouse.core.domain.admin.domain.model.Admin;

/**
 * 관리자 계정 도메인 모델 ↔ JPA 엔티티 변환기. 도메인이 프레임워크-프리를 유지하도록 변환 책임을 infrastructure에 둔다.
 */
final class AdminMapper {

    private AdminMapper() {
    }

    /**
     * JPA 엔티티를 도메인 모델로 재구성한다(조회 경로).
     */
    static Admin toDomain(AdminJpaEntity entity) {
        return Admin.reconstitute(
            entity.getId(),
            entity.getUsername(),
            entity.getPassword(),
            entity.getName(),
            entity.getRole(),
            entity.getStatus()
        );
    }

    /**
     * 신규 도메인 모델을 저장용 JPA 엔티티로 변환한다(식별자 없는 상태).
     */
    static AdminJpaEntity toEntity(Admin domain) {
        return AdminJpaEntity.create(
            domain.getUsername(),
            domain.getPassword(),
            domain.getName(),
            domain.getRole(),
            domain.getStatus()
        );
    }
}
