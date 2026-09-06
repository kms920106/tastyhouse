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

    static FaqCategoryJpaEntity toEntity(FaqCategory domain) {
        return FaqCategoryJpaEntity.create(
            domain.getName(),
            domain.getSort(),
            domain.isVisible(),
            domain.isDeleted()
        );
    }

    static void applyChanges(FaqCategoryJpaEntity entity, FaqCategory domain) {
        entity.applyChanges(
            domain.getName(),
            domain.getSort(),
            domain.isVisible(),
            domain.isDeleted()
        );
    }
}
