package com.tastyhouse.infrastructure.faq.persistence;

import com.tastyhouse.application.faq.port.out.write.FaqCategoryState;

final class FaqCategoryMapper {
    private FaqCategoryMapper() {
    }

    static FaqCategoryState toState(FaqCategoryJpaEntity entity) {
        return new FaqCategoryState(
            entity.getId(),
            entity.getName(),
            entity.getSort(),
            entity.isVisible(),
            entity.isDeleted(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static FaqCategoryJpaEntity toEntity(FaqCategoryState state) {
        return FaqCategoryJpaEntity.create(
            state.name(),
            state.sort(),
            state.visible(),
            state.deleted()
        );
    }

    static void applyChanges(FaqCategoryJpaEntity entity, FaqCategoryState state) {
        entity.applyChanges(
            state.name(),
            state.sort(),
            state.visible(),
            state.deleted()
        );
    }
}
