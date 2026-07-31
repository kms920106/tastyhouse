package com.tastyhouse.infrastructure.policy.persistence;

import com.tastyhouse.domain.policy.domain.model.PolicyDocument;

/**
 * 정책 문서 도메인 모델 ↔ JPA 엔티티 변환기. 도메인이 프레임워크-프리를 유지하도록 변환 책임을 infrastructure에 둔다.
 */
final class PolicyDocumentMapper {

    private PolicyDocumentMapper() {
    }

    /**
     * JPA 엔티티를 도메인 모델로 재구성한다(조회 경로).
     */
    static PolicyDocument toDomain(PolicyDocumentJpaEntity entity) {
        return PolicyDocument.reconstitute(
            entity.getId(),
            entity.getType(),
            entity.getVersion(),
            entity.getTitle(),
            entity.getContent(),
            entity.isCurrent(),
            entity.isMandatory(),
            entity.getEffectiveDate(),
            entity.getCreatedBy(),
            entity.getUpdatedBy(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    /**
     * 신규 도메인 모델을 저장용 JPA 엔티티로 변환한다(식별자 없는 상태).
     */
    static PolicyDocumentJpaEntity toEntity(PolicyDocument domain) {
        return PolicyDocumentJpaEntity.create(
            domain.getType(),
            domain.getVersion(),
            domain.getTitle(),
            domain.getContent(),
            domain.isCurrent(),
            domain.isMandatory(),
            domain.getEffectiveDate(),
            domain.getCreatedBy(),
            domain.getUpdatedBy()
        );
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update 경로, dirty checking 대체).
     */
    static void applyChanges(PolicyDocumentJpaEntity entity, PolicyDocument domain) {
        entity.applyChanges(
            domain.getTitle(),
            domain.getContent(),
            domain.isMandatory(),
            domain.getEffectiveDate(),
            domain.getUpdatedBy(),
            domain.isCurrent()
        );
    }
}
