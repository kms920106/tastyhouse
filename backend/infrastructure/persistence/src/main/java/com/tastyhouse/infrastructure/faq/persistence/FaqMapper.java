package com.tastyhouse.infrastructure.faq.persistence;

import com.tastyhouse.domain.faq.model.Faq;
import com.tastyhouse.domain.faq.vo.FaqCategoryId;

final class FaqMapper {
    private FaqMapper() {
    }

    static Faq toDomain(FaqJpaEntity entity) {
        return Faq.reconstitute(
            entity.getId(),
            entity.getFaqCategoryId() == null ? null : FaqCategoryId.of(entity.getFaqCategoryId()),
            entity.getQuestion(),
            entity.getAnswer(),
            entity.getSort(),
            entity.isVisible(),
            entity.isDeleted(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static FaqJpaEntity toEntity(Faq faq) {
        return FaqJpaEntity.create(
            faq.getFaqCategoryId() == null ? null : faq.getFaqCategoryId().value(),
            faq.getQuestion(),
            faq.getAnswer(),
            faq.getSort(),
            faq.isVisible(),
            faq.isDeleted()
        );
    }

    static void applyChanges(FaqJpaEntity entity, Faq faq) {
        entity.applyChanges(
            faq.getFaqCategoryId() == null ? null : faq.getFaqCategoryId().value(),
            faq.getQuestion(),
            faq.getAnswer(),
            faq.getSort(),
            faq.isVisible(),
            faq.isDeleted()
        );
    }
}
