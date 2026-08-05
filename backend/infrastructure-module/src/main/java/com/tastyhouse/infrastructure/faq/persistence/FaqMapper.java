package com.tastyhouse.infrastructure.faq.persistence;

import com.tastyhouse.domain.faq.model.Faq;
import com.tastyhouse.domain.faq.vo.FaqCategoryId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

/**
 * FAQ 도메인 모델 ↔ JPA 엔티티 변환기. 도메인이 프레임워크-프리를 유지하도록 변환 책임을 infrastructure에 둔다.
 */
final class FaqMapper {

    private FaqMapper() {
    }

    /**
     * JPA 엔티티를 도메인 모델로 재구성한다(조회 경로).
     */
    static Faq toDomain(FaqJpaEntity entity) {
        return Faq.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getFaqCategoryId(), FaqCategoryId::of),
            entity.getQuestion(),
            entity.getAnswer(),
            entity.getSort(),
            entity.isVisible(),
            entity.isDeleted(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    /**
     * 신규 도메인 모델을 저장용 JPA 엔티티로 변환한다(식별자 없는 상태).
     */
    static FaqJpaEntity toEntity(Faq domain) {
        return FaqJpaEntity.create(
            IdMapping.raw(domain.getFaqCategoryId(), FaqCategoryId::value),
            domain.getQuestion(),
            domain.getAnswer(),
            domain.getSort(),
            domain.isVisible(),
            domain.isDeleted()
        );
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update 경로, dirty checking 대체).
     */
    static void applyChanges(FaqJpaEntity entity, Faq domain) {
        entity.applyChanges(
            IdMapping.raw(domain.getFaqCategoryId(), FaqCategoryId::value),
            domain.getQuestion(),
            domain.getAnswer(),
            domain.getSort(),
            domain.isVisible(),
            domain.isDeleted()
        );
    }
}
