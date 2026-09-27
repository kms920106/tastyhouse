package com.tastyhouse.infrastructure.faq.persistence;

import com.tastyhouse.domain.faq.model.FaqCategory;

final class FaqCategoryMapper {
    private FaqCategoryMapper() {
    }

    static FaqCategory toDomain(FaqCategoryJpaEntity entity) {
        return FaqCategory.reconstitute(
            entity.getId(),
            entity.getName(),
            entity.getSort(),
            entity.isVisible(),
            entity.isDeleted(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static FaqCategoryJpaEntity toEntity(FaqCategory faqCategory) {
        return FaqCategoryJpaEntity.create(
            faqCategory.getName(),
            faqCategory.getSort(),
            faqCategory.isVisible(),
            faqCategory.isDeleted()
        );
    }

    static void applyChanges(FaqCategoryJpaEntity entity, FaqCategory faqCategory) {
        entity.applyChanges(
            faqCategory.getName(),
            faqCategory.getSort(),
            faqCategory.isVisible(),
            faqCategory.isDeleted()
        );
    }
}
