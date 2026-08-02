package com.tastyhouse.infrastructure.partnership.persistence;

import com.tastyhouse.domain.partnership.model.PartnershipRequest;

/**
 * 제휴 문의 도메인 모델 ↔ JPA 엔티티 변환기. 도메인이 프레임워크-프리를 유지하도록 변환 책임을 infrastructure에 둔다.
 */
final class PartnershipRequestMapper {

    private PartnershipRequestMapper() {
    }

    /**
     * JPA 엔티티를 도메인 모델로 재구성한다(조회 경로).
     */
    static PartnershipRequest toDomain(PartnershipRequestJpaEntity entity) {
        return PartnershipRequest.reconstitute(
            entity.getId(),
            entity.getBusinessName(),
            entity.getAddress(),
            entity.getAddressDetail(),
            entity.getContactName(),
            entity.getContactPhone(),
            entity.getConsultationRequestedAt(),
            entity.getStatus(),
            entity.isDeleted(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    /**
     * 신규 도메인 모델을 저장용 JPA 엔티티로 변환한다(식별자 없는 상태).
     */
    static PartnershipRequestJpaEntity toEntity(PartnershipRequest domain) {
        return PartnershipRequestJpaEntity.create(
            domain.getBusinessName(),
            domain.getAddress(),
            domain.getAddressDetail(),
            domain.getContactName(),
            domain.getContactPhone(),
            domain.getConsultationRequestedAt(),
            domain.getStatus(),
            domain.isDeleted()
        );
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update 경로, dirty checking 대체).
     */
    static void applyChanges(PartnershipRequestJpaEntity entity, PartnershipRequest domain) {
        entity.applyChanges(
            domain.getStatus(),
            domain.isDeleted()
        );
    }
}
