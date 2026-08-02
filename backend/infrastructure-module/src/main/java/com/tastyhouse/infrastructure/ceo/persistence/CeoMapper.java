package com.tastyhouse.infrastructure.ceo.persistence;

import com.tastyhouse.domain.ceo.model.Ceo;

/**
 * 점주 계정 도메인 모델 ↔ JPA 엔티티 변환기. 도메인이 프레임워크-프리를 유지하도록 변환 책임을 infrastructure에 둔다.
 */
final class CeoMapper {

    private CeoMapper() {
    }

    /**
     * JPA 엔티티를 도메인 모델로 재구성한다(조회 경로).
     */
    static Ceo toDomain(CeoJpaEntity entity) {
        return Ceo.reconstitute(
            entity.getId(),
            entity.getUsername(),
            entity.getPassword(),
            entity.getName(),
            entity.getBusinessRegistrationNumber(),
            entity.getPhoneNumber(),
            entity.getEmail(),
            entity.getStatus()
        );
    }

    /**
     * 신규 도메인 모델을 저장용 JPA 엔티티로 변환한다(식별자 없는 상태).
     */
    static CeoJpaEntity toEntity(Ceo domain) {
        return CeoJpaEntity.create(
            domain.getUsername(),
            domain.getPassword(),
            domain.getName(),
            domain.getBusinessRegistrationNumber(),
            domain.getPhoneNumber(),
            domain.getEmail(),
            domain.getStatus()
        );
    }
}
