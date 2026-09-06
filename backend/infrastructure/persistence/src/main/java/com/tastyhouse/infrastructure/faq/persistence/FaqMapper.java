package com.tastyhouse.infrastructure.faq.persistence;

import com.tastyhouse.domain.faq.model.Faq;
import com.tastyhouse.domain.faq.vo.FaqCategoryId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class FaqMapper {
    private FaqMapper() {
    }

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
